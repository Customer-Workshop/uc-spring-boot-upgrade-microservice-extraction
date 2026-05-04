package com.bank.rm.conversation.ai;

import com.bank.rm.common.dto.ConversationPhase;
import java.util.Map;

public class PromptManager {

    private static final Map<ConversationPhase, String> SYSTEM_PROMPTS = Map.of(
        ConversationPhase.GREETING,
            "You are a friendly banking relationship manager. Welcome the customer warmly and ask for their name. Keep it brief and professional.",
        ConversationPhase.PERSONAL,
            "Collect the customer's personal details: age group (20-30, 30-40, 40-50, 50+), location, phone number, and email. Ask naturally, one or two at a time.",
        ConversationPhase.FINANCIAL,
            "Ask about the customer's financial situation: income source (salaried/business/professional/self-employed/student/retired), income range (60-70K, 70-80K, 80-100K, 100-150K, 150K+), current savings, and investments. Be respectful about financial questions.",
        ConversationPhase.GOALS,
            "Ask about retirement goals: how much they want to retire with and when (based on their age group). Also ask about their dependents and monthly expenses.",
        ConversationPhase.RISK_ASSESSMENT,
            "Explain that you'll now assess their risk profile based on the information provided. Ask about their risk tolerance (conservative, moderate, or aggressive) and investment experience.",
        ConversationPhase.RECOMMENDATION,
            "Present product recommendations based on their risk profile. Explain each product briefly with expected returns and why it suits them.",
        ConversationPhase.CHANNEL_PREF,
            "Ask the customer their preferred communication channel for follow-ups: SMS, WhatsApp, Email, or Phone calls.",
        ConversationPhase.FOLLOWUP_SCHEDULE,
            "Propose a follow-up schedule based on their preferences. Suggest weekly, biweekly, monthly, or quarterly reviews. Confirm the schedule."
    );

    private static final Map<ConversationPhase, String> REQUIRED_ENTITIES = Map.of(
        ConversationPhase.GREETING, "name",
        ConversationPhase.PERSONAL, "ageGroup,location,phone,email",
        ConversationPhase.FINANCIAL, "incomeSource,incomeRange,savings,investments",
        ConversationPhase.GOALS, "retirementTarget,retirementAge,dependents",
        ConversationPhase.RISK_ASSESSMENT, "riskTolerance",
        ConversationPhase.CHANNEL_PREF, "preferredChannel",
        ConversationPhase.FOLLOWUP_SCHEDULE, "followUpFrequency"
    );

    public String getSystemPrompt(ConversationPhase phase) {
        return SYSTEM_PROMPTS.getOrDefault(phase, "Continue the conversation naturally as a banking relationship manager.");
    }

    public String getRequiredEntities(ConversationPhase phase) {
        return REQUIRED_ENTITIES.getOrDefault(phase, "");
    }
}
