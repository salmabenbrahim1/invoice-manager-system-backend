package com.example.backend.controller;

import com.example.backend.model.Notification;
import com.example.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{accountantId}")
    public List<Notification> getNotifications(@PathVariable String accountantId) {
        return notificationService.getNotifications(accountantId);
    }

    @GetMapping("/{accountantId}/unread-count")
    public Long getUnreadCount(@PathVariable String accountantId) {
        return notificationService.getUnreadCount(accountantId);
    }

    @PutMapping("/{accountantId}/read-all")
    public void markAllAsRead(@PathVariable String accountantId) {
        notificationService.markAllAsRead(accountantId);
    }

    @PostMapping("/{accountantId}")
    public void createNotification(@PathVariable String accountantId, @RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        String clientId = payload.get("clientId");
        notificationService.createNotification(accountantId, clientId, message);
    }


}