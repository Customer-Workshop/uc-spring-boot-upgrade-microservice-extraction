package com.bank.rm.notification.controller

import com.bank.rm.common.dto.ApiResponse
import com.bank.rm.notification.dto.*
import com.bank.rm.notification.service.NotificationRouter
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(private val notificationRouter: NotificationRouter) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun send(@Valid @RequestBody request: SendNotificationRequest): ApiResponse<NotificationResponse> =
        ApiResponse(success = true, data = notificationRouter.send(request))

    @GetMapping("/{customerId}")
    fun getHistory(@PathVariable customerId: UUID): ApiResponse<List<NotificationResponse>> =
        ApiResponse(success = true, data = notificationRouter.getNotificationHistory(customerId))
}
