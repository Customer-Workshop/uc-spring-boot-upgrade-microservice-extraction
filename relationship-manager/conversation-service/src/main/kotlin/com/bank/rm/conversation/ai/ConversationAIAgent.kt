package com.bank.rm.conversation.ai

import com.bank.rm.common.dto.ConversationPhase
import com.bank.rm.common.exception.AIServiceException
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.input.Prompt
import dev.langchain4j.model.input.PromptTemplate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ConversationAIAgent(
    private val chatModel: ChatLanguageModel,
    private val promptManager: PromptManager,
    private val entityExtractor: EntityExtractor
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun generateResponse(
        phase: ConversationPhase,
        conversationHistory: List<ChatMessage>,
        extractedData: Map<String, Any>,
        customerMessage: String
    ): AIResponse {
        return try {
            val systemPrompt = promptManager.getSystemPrompt(phase, extractedData)
            val historyContext = conversationHistory.takeLast(10).joinToString("\n") {
                "${it.role}: ${it.content}"
            }

            val prompt = PromptTemplate.from(
                """{{system_prompt}}
                
Conversation so far:
{{history}}

Customer: {{message}}

Respond naturally as a banking relationship manager. Be empathetic, professional, and guide the conversation toward collecting the required information for this phase."""
            ).apply(
                mapOf(
                    "system_prompt" to systemPrompt,
                    "history" to historyContext,
                    "message" to customerMessage
                )
            )

            val response = chatModel.generate(prompt.text())
            val entities = entityExtractor.extract(customerMessage, phase)
            val shouldTransition = shouldTransitionPhase(phase, extractedData + entities)

            AIResponse(
                message = response,
                extractedEntities = entities,
                shouldTransitionPhase = shouldTransition,
                suggestedNextPhase = if (shouldTransition) getNextPhase(phase) else null
            )
        } catch (e: Exception) {
            log.error("AI agent error for phase $phase: ${e.message}", e)
            throw AIServiceException("Failed to generate AI response: ${e.message}", e)
        }
    }

    fun generateSummary(conversationHistory: List<ChatMessage>, extractedData: Map<String, Any>): String {
        return try {
            val prompt = PromptTemplate.from(
                """Summarize this banking relationship conversation concisely. Include:
- Customer profile highlights
- Key financial details discussed
- Risk assessment outcome
- Products recommended
- Follow-up actions agreed

Conversation:
{{history}}

Extracted data:
{{data}}

Summary:"""
            ).apply(
                mapOf(
                    "history" to conversationHistory.joinToString("\n") { "${it.role}: ${it.content}" },
                    "data" to extractedData.toString()
                )
            )
            chatModel.generate(prompt.text())
        } catch (e: Exception) {
            log.warn("Failed to generate AI summary, using template fallback", e)
            generateTemplateSummary(extractedData)
        }
    }

    fun detectHandoffIntent(message: String): Boolean {
        return try {
            val response = chatModel.generate(
                """Analyze if this customer message indicates they want to speak with a human agent.
Reply with only "YES" or "NO".
Message: "$message""""
            )
            response.trim().uppercase().startsWith("YES")
        } catch (e: Exception) {
            log.warn("Handoff detection failed, checking keywords", e)
            val keywords = listOf("human", "agent", "person", "manager", "real person", "speak to someone")
            keywords.any { message.lowercase().contains(it) }
        }
    }

    private fun shouldTransitionPhase(phase: ConversationPhase, data: Map<String, Any>): Boolean {
        val requiredFields = when (phase) {
            ConversationPhase.GREETING -> listOf("acknowledged")
            ConversationPhase.PERSONAL -> listOf("name", "ageGroup")
            ConversationPhase.FINANCIAL -> listOf("incomeSource", "incomeRange")
            ConversationPhase.GOALS -> listOf("retirementTarget")
            ConversationPhase.RISK_ASSESSMENT -> listOf("riskScore")
            ConversationPhase.RECOMMENDATION -> listOf("recommendationsPresented")
            ConversationPhase.CHANNEL_PREF -> listOf("preferredChannel")
            ConversationPhase.FOLLOWUP_SCHEDULE -> listOf("followUpFrequency")
            ConversationPhase.COMPLETED -> emptyList()
        }
        return requiredFields.all { data.containsKey(it) }
    }

    private fun getNextPhase(current: ConversationPhase): ConversationPhase = when (current) {
        ConversationPhase.GREETING -> ConversationPhase.PERSONAL
        ConversationPhase.PERSONAL -> ConversationPhase.FINANCIAL
        ConversationPhase.FINANCIAL -> ConversationPhase.GOALS
        ConversationPhase.GOALS -> ConversationPhase.RISK_ASSESSMENT
        ConversationPhase.RISK_ASSESSMENT -> ConversationPhase.RECOMMENDATION
        ConversationPhase.RECOMMENDATION -> ConversationPhase.CHANNEL_PREF
        ConversationPhase.CHANNEL_PREF -> ConversationPhase.FOLLOWUP_SCHEDULE
        ConversationPhase.FOLLOWUP_SCHEDULE -> ConversationPhase.COMPLETED
        ConversationPhase.COMPLETED -> ConversationPhase.COMPLETED
    }

    private fun generateTemplateSummary(data: Map<String, Any>): String {
        return buildString {
            append("Conversation Summary:\n")
            data["name"]?.let { append("- Customer: $it\n") }
            data["ageGroup"]?.let { append("- Age Group: $it\n") }
            data["incomeSource"]?.let { append("- Income: $it\n") }
            data["riskCategory"]?.let { append("- Risk Profile: $it\n") }
            data["preferredChannel"]?.let { append("- Preferred Contact: $it\n") }
        }
    }
}

data class AIResponse(
    val message: String,
    val extractedEntities: Map<String, Any>,
    val shouldTransitionPhase: Boolean,
    val suggestedNextPhase: ConversationPhase?
)

data class ChatMessage(
    val role: String,
    val content: String
)
