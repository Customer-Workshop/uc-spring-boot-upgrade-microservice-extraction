package com.bank.rm.conversation.ai;

import com.bank.rm.common.dto.ConversationPhase;
import dev.langchain4j.model.chat.ChatLanguageModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class EntityExtractor {
    private static final Logger log = LoggerFactory.getLogger(EntityExtractor.class);
    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;

    public EntityExtractor(ChatLanguageModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> extract(String userMessage, ConversationPhase phase,
                                        Map<String, Object> existingEntities) {
        Map<String, Object> entities = new HashMap<>(existingEntities);
        try {
            String prompt = String.format(
                "Extract structured data from this customer message in a banking conversation.\n" +
                "Current phase: %s\nCustomer said: \"%s\"\nAlready collected: %s\n\n" +
                "Return ONLY a JSON object with extracted key-value pairs. " +
                "Keys should be: name, ageGroup, location, phone, email, incomeSource, incomeRange, " +
                "savings, investments, retirementTarget, retirementAge, dependents, riskTolerance, " +
                "preferredChannel, followUpFrequency. Only include keys found in the message.",
                phase, userMessage, objectMapper.writeValueAsString(existingEntities));
            String response = chatModel.generate(prompt);
            String jsonStr = response.contains("{") ?
                    response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1) : "{}";
            Map<String, Object> extracted = objectMapper.readValue(jsonStr, Map.class);
            entities.putAll(extracted);
        } catch (Exception e) {
            log.warn("LLM entity extraction failed, using regex fallback: {}", e.getMessage());
            entities.putAll(regexFallback(userMessage, phase));
        }
        return entities;
    }

    private Map<String, Object> regexFallback(String message, ConversationPhase phase) {
        Map<String, Object> result = new HashMap<>();
        String lower = message.toLowerCase();
        if (phase == ConversationPhase.GREETING && message.length() < 100) {
            String cleaned = message.replaceAll("[^a-zA-Z\\s]", "").trim();
            String[] words = cleaned.split("\\s+");
            if (words.length > 0 && words.length <= 4) {
                result.put("name", cleaned);
            }
        }
        if (lower.contains("20-30") || lower.contains("twenties")) result.put("ageGroup", "AGE_20_30");
        if (lower.contains("30-40") || lower.contains("thirties")) result.put("ageGroup", "AGE_30_40");
        if (lower.contains("40-50") || lower.contains("forties")) result.put("ageGroup", "AGE_40_50");
        if (lower.contains("50+") || lower.contains("fifties")) result.put("ageGroup", "AGE_50_PLUS");
        if (lower.contains("salaried")) result.put("incomeSource", "SALARIED");
        if (lower.contains("business")) result.put("incomeSource", "BUSINESS");
        if (lower.contains("retired")) result.put("incomeSource", "RETIRED");
        if (lower.contains("whatsapp")) result.put("preferredChannel", "WHATSAPP");
        if (lower.contains("sms")) result.put("preferredChannel", "SMS");
        if (lower.contains("email") || lower.contains("mail")) result.put("preferredChannel", "EMAIL");
        if (lower.contains("phone") || lower.contains("call")) result.put("preferredChannel", "PHONE");
        if (lower.contains("weekly")) result.put("followUpFrequency", "WEEKLY");
        if (lower.contains("monthly")) result.put("followUpFrequency", "MONTHLY");
        if (lower.contains("quarterly")) result.put("followUpFrequency", "QUARTERLY");
        return result;
    }
}
