package com.bank.rm.notification.controller;

import com.bank.rm.common.dto.ApiResponse;
import com.bank.rm.notification.dto.NotificationDtos.*;
import com.bank.rm.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> send(@RequestBody SendNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(notificationService.send(request)));
    }
}
