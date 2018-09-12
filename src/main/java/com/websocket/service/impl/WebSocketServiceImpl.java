package com.websocket.service.impl;

import com.websocket.service.WebSocketServer;
import org.springframework.stereotype.Service;

@Service
public class WebSocketServiceImpl implements WebSocketServer {
    @Override
    public String sayHello() {
        return "hello chenlong";
    }
}
