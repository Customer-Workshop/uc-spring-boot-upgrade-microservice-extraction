package com.bank.rm.conversation.repository;

import com.bank.rm.conversation.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByCustomerId(UUID customerId);
    List<Conversation> findByAnonymousSessionId(UUID anonymousSessionId);
}
