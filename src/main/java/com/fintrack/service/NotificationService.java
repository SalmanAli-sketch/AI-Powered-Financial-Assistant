package com.fintrack.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(String email, String message) {
        messagingTemplate.convertAndSend("/topic/notifications/" + email, Map.of(
            "message", message,
            "timestamp", System.currentTimeMillis()
        ));
    }
}
