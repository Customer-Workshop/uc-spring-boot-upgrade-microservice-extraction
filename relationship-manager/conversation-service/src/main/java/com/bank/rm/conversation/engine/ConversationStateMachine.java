package com.bank.rm.conversation.engine;

import com.bank.rm.common.dto.ConversationPhase;
import java.util.Map;
import java.util.Set;

public class ConversationStateMachine {

    private static final Map<ConversationPhase, Set<String>> PHASE_REQUIREMENTS = Map.of(
        ConversationPhase.GREETING, Set.of("name"),
        ConversationPhase.PERSONAL, Set.of("ageGroup", "location"),
        ConversationPhase.FINANCIAL, Set.of("incomeSource", "incomeRange"),
        ConversationPhase.GOALS, Set.of("retirementTarget"),
        ConversationPhase.RISK_ASSESSMENT, Set.of("riskTolerance"),
        ConversationPhase.RECOMMENDATION, Set.of(),
        ConversationPhase.CHANNEL_PREF, Set.of("preferredChannel"),
        ConversationPhase.FOLLOWUP_SCHEDULE, Set.of("followUpFrequency")
    );

    public boolean canTransition(ConversationPhase currentPhase, Map<String, Object> extractedData) {
        Set<String> required = PHASE_REQUIREMENTS.getOrDefault(currentPhase, Set.of());
        return required.stream().allMatch(key -> extractedData.containsKey(key) &&
                extractedData.get(key) != null &&
                !extractedData.get(key).toString().isEmpty());
    }

    public ConversationPhase getNextPhase(ConversationPhase currentPhase) {
        return switch (currentPhase) {
            case GREETING -> ConversationPhase.PERSONAL;
            case PERSONAL -> ConversationPhase.FINANCIAL;
            case FINANCIAL -> ConversationPhase.GOALS;
            case GOALS -> ConversationPhase.RISK_ASSESSMENT;
            case RISK_ASSESSMENT -> ConversationPhase.RECOMMENDATION;
            case RECOMMENDATION -> ConversationPhase.CHANNEL_PREF;
            case CHANNEL_PREF -> ConversationPhase.FOLLOWUP_SCHEDULE;
            case FOLLOWUP_SCHEDULE -> ConversationPhase.COMPLETED;
            case COMPLETED -> ConversationPhase.COMPLETED;
        };
    }
}
