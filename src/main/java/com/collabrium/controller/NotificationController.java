package com.collabrium.controller;

import com.collabrium.dto.ApiResponse;
import com.collabrium.dto.NotificationDto;
import com.collabrium.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getMyNotifications(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getForUser(auth.getName())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDto>> markRead(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.markRead(id, auth.getName())));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(Authentication auth) {
        notificationService.markAllRead(auth.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearAll(Authentication auth) {
        notificationService.clearAll(auth.getName());
        return ResponseEntity.noContent().build();
    }
}
