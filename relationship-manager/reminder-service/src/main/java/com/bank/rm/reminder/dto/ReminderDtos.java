package com.bank.rm.reminder.dto;

import com.bank.rm.common.dto.CommunicationChannel;
import java.time.LocalDateTime;
import java.util.UUID;

public class ReminderDtos {

    public record CreateReminderRequest(UUID customerId, UUID followUpInstanceId,
                                         CommunicationChannel channel, LocalDateTime scheduledAt,
                                         String message) {}

    public record ReminderResponse(UUID reminderId, UUID customerId, CommunicationChannel channel,
                                    LocalDateTime scheduledAt, String status) {}

    private ReminderDtos() {}
}
