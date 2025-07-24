package com.example.codeeditorservice.config;

import com.example.codeeditorservice.events.WebSocketEventHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

     private final WebSocketEventHandler webSocketEventHandler;

     public WebSocketConfig(WebSocketEventHandler webSocketEventHandler) {
          this.webSocketEventHandler = webSocketEventHandler;
     }

     @Override
     public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
          registry.addHandler(webSocketEventHandler, "/collaborative-editing")
                    .setAllowedOrigins("*"); // Be cautious with this in production
     }
}