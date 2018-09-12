package com.websocket.handler;


import com.websocket.config.MyEndpointConfigure;
import com.websocket.config.NewConfigurator;
import com.websocket.service.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.server.standard.SpringConfigurator;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import java.io.IOException;

import static com.websocket.util.WebSocketUtils.LIVING_SESSIONS_CACHE;
import static com.websocket.util.WebSocketUtils.sendMessage;
import static com.websocket.util.WebSocketUtils.sendMessageAll;

@ServerEndpoint(value = "/chat-room/{username}", configurator= MyEndpointConfigure.class)
@Component
public class ChatRoomServerEndpoint {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private WebSocketServer webSocketServer;

    private static final Logger log = LoggerFactory.getLogger(ChatRoomServerEndpoint.class);

    @OnOpen
    public void openSession(@PathParam("username") String username, Session session) {
        LIVING_SESSIONS_CACHE.put(username, session);
        String message = "欢迎用户[" + username + "] 来到聊天室！";
        log.info(message);
        sendMessageAll(message);

    }

    @OnMessage
    public void onMessage(@PathParam("username") String username, String message) {
        log.info(message);
        System.out.println(webSocketServer.sayHello());
        sendMessageAll("用户[" + username + "] : " + message);
    }

    @OnClose
    public void onClose(@PathParam("username") String username, Session session) {
        //当前的Session 移除
        LIVING_SESSIONS_CACHE.remove(username);
        //并且通知其他人当前用户已经离开聊天室了
        sendMessageAll("用户[" + username + "] 已经离开聊天室了！");
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throwable.printStackTrace();
    }


    @GetMapping("/chat-room/{sender}/to/{receive}")
    public void onMessage(@PathVariable("sender") String sender, @PathVariable("receive") String receive, String message) {
        sendMessage(LIVING_SESSIONS_CACHE.get(receive), "[" + sender + "]" + "-> [" + receive + "] : " + message);
    }
}
