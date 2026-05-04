package com.bank.rm.conversation.ai;

import com.bank.rm.common.dto.ConversationPhase;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class ConversationAIAgent {
    private static final Logger log = LoggerFactory.getLogger(ConversationAIAgent.class);
    private final ChatLanguageModel chatModel;
    private final PromptManager promptManager;

    public ConversationAIAgent(ChatLanguageModel chatModel, PromptManager promptManager) {
        this.chatModel = chatModel;
        this.promptManager = promptManager;
    }

    public String generateResponse(String userMessage, ConversationPhase phase,
                                    Map<String, Object> extractedData, String conversationHistory) {
        try {
            String systemPrompt = promptManager.getSystemPrompt(phase);
            String fullPrompt = String.format(
                "System: %s\n\nConversation context:\n%s\n\nCollected data so far: %s\n\nCustomer: %s\n\nAssistant:",
                systemPrompt, conversationHistory, extractedData, userMessage);
            return chatModel.generate(fullPrompt);
        } catch (Exception e) {
            log.warn("LLM response generation failed, using template: {}", e.getMessage());
            return getTemplateFallback(phase, extractedData);
        }
    }

    public String generateSummary(String conversationHistory, Map<String, Object> extractedData) {
        try {
            String prompt = "Summarize this banking relationship manager conversation in 2-3 sentences:\n\n" +
                    conversationHistory + "\n\nCollected data: " + extractedData;
            return chatModel.generate(prompt);
        } catch (Exception e) {
            log.warn("Summary generation failed: {}", e.getMessage());
            return "Conversation completed. Data collected: " + extractedData.keySet();
        }
    }

    private String getTemplateFallback(ConversationPhase phase, Map<String, Object> data) {
        return switch (phase) {
            case GREETING -> "Welcome to our banking services! I'm your Relationship Manager. Could you please share your name?";
            case PERSONAL -> {
                String name = data.getOrDefault("name", "").toString();
                yield "Thank you" + (name.isEmpty() ? "" : ", " + name) +
                        "! Could you share your age group (20-30, 30-40, 40-50, or 50+), location, phone, and email?";
            }
            case FINANCIAL -> "Now, could you tell me about your income source and range, current savings, and investments?";
            case GOALS -> "What are your retirement goals? How much would you like to retire with, and by what age?";
            case RISK_ASSESSMENT -> "Based on your profile, I'll assess your risk tolerance. Would you describe yourself as conservative, moderate, or aggressive with investments?";
            case RECOMMENDATION -> "Based on your risk profile, here are my recommendations. I'll share the details shortly.";
            case CHANNEL_PREF -> "How would you like to stay in touch? SMS, WhatsApp, Email, or Phone?";
            case FOLLOWUP_SCHEDULE -> "Let's schedule regular check-ins. Would you prefer weekly, biweekly, monthly, or quarterly reviews?";
            case COMPLETED -> "Thank you for your time! Your profile is set up and we'll be in touch.";
        };
    }
}
