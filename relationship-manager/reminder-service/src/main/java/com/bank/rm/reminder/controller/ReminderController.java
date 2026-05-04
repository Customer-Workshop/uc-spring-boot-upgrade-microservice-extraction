package com.bank.rm.reminder.controller;

import com.bank.rm.common.dto.ApiResponse;
import com.bank.rm.reminder.dto.ReminderDtos.*;
import com.bank.rm.reminder.service.ReminderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reminders")
public class ReminderController {
    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReminderResponse>> createReminder(@RequestBody CreateReminderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(reminderService.createReminder(request)));
    }
}
