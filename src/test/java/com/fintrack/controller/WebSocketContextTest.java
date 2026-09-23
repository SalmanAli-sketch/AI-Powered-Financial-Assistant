package com.fintrack.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.config.annotation.DelegatingWebSocketMessageBrokerConfiguration;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class WebSocketContextTest {

    @Autowired(required = false)
    private DelegatingWebSocketMessageBrokerConfiguration webSocketConfiguration;

    @Test
    public void testWebSocketConfigLoads() {
        assertNotNull(webSocketConfiguration, "WebSocket message broker configuration should be initialized in Spring Context");
    }
}
