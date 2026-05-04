package com.bank.rm.common.event;

import java.util.List;
import java.util.UUID;

public class ProfileUpdatedEvent extends DomainEvent {
    private final UUID customerId;
    private final List<String> updatedFields;

    public ProfileUpdatedEvent(UUID customerId, List<String> updatedFields) {
        super("customer-profile-service");
        this.customerId = customerId;
        this.updatedFields = updatedFields;
    }

    public UUID getCustomerId() { return customerId; }
    public List<String> getUpdatedFields() { return updatedFields; }
}
