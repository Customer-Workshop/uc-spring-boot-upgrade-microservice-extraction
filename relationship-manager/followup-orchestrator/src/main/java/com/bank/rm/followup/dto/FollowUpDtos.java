package com.bank.rm.followup.dto;

import com.bank.rm.common.dto.CommunicationChannel;
import com.bank.rm.common.dto.FollowUpFrequency;
import com.bank.rm.common.dto.FollowUpStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class FollowUpDtos {

    public record CreateScheduleRequest(UUID customerId, FollowUpFrequency frequency,
                                         CommunicationChannel channel) {}

    public record ScheduleResponse(UUID scheduleId, UUID customerId, FollowUpFrequency frequency,
                                    CommunicationChannel channel, LocalDateTime nextFollowUpAt,
                                    boolean active, int totalCompleted) {}

    public record InstanceResponse(UUID instanceId, UUID customerId, LocalDateTime scheduledAt,
                                    FollowUpStatus status, String agenda) {}

    private FollowUpDtos() {}
}
