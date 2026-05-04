package com.bank.rm.conversation.ai

import com.bank.rm.common.dto.ConversationPhase
import dev.langchain4j.model.chat.ChatLanguageModel
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EntityExtractor(
    private val chatModel: ChatLanguageModel,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // Regex patterns for quick extraction before LLM fallback
    private val patterns = mapOf(
        "name" to Regex("""(?:my name is|i'm|i am|call me)\s+([A-Z][a-z]+(?:\s+[A-Z][a-z]+)*)""", RegexOption.IGNORE_CASE),
        "age" to Regex("""(?:i'm|i am|age is)\s+(\d{2})""", RegexOption.IGNORE_CASE),
        "ageGroup" to Regex("""(20[s\-]30|30[s\-]40|40[s\-]50|50\+|fifty plus|twenties|thirties|forties|fifties)""", RegexOption.IGNORE_CASE),
        "email" to Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}"""),
        "phone" to Regex("""(?:\+91[\s-]?)?[6-9]\d{9}"""),
        "income" to Regex("""(\d+(?:\.\d+)?)\s*(?:lakh|lac|L|k|K|lpa|per\s*(?:annum|month|year))""", RegexOption.IGNORE_CASE),
        "incomeSource" to Regex("""(?:i (?:am|work as)|working as|profession is)\s+(salaried|business|professional|self[- ]employed|student|retired)""", RegexOption.IGNORE_CASE),
        "retirementAmount" to Regex("""(\d+(?:\.\d+)?)\s*(?:crore|cr|lakh|lac)\s*(?:by|at|when|for retirement)""", RegexOption.IGNORE_CASE)
    )

    fun extract(message: String, phase: ConversationPhase): Map<String, Any> {
        val quickExtracted = quickExtract(message, phase)
        if (quickExtracted.isNotEmpty()) return quickExtracted

        return llmExtract(message, phase)
    }

    private fun quickExtract(message: String, phase: ConversationPhase): Map<String, Any> {
        val result = mutableMapOf<String, Any>()

        when (phase) {
            ConversationPhase.GREETING -> {
                if (message.lowercase().let { it.contains("hi") || it.contains("hello") || it.contains("yes") || it.contains("sure") }) {
                    result["acknowledged"] = true
                }
            }
            ConversationPhase.PERSONAL -> {
                patterns["name"]?.find(message)?.groupValues?.get(1)?.let { result["name"] = it }
                patterns["email"]?.find(message)?.value?.let { result["email"] = it }
                patterns["phone"]?.find(message)?.value?.let { result["phone"] = it }
                extractAgeGroup(message)?.let { result["ageGroup"] = it }
            }
            ConversationPhase.FINANCIAL -> {
                patterns["incomeSource"]?.find(message)?.groupValues?.get(1)?.let {
                    result["incomeSource"] = normalizeIncomeSource(it)
                }
                extractIncomeRange(message)?.let { result["incomeRange"] = it }
            }
            ConversationPhase.GOALS -> {
                patterns["retirementAmount"]?.find(message)?.let {
                    result["retirementTarget"] = it.value
                }
                Regex("""retire\s*(?:by|at|around)\s*(\d{2})""", RegexOption.IGNORE_CASE).find(message)?.groupValues?.get(1)?.let {
                    result["retirementAge"] = it.toInt()
                }
            }
            ConversationPhase.CHANNEL_PREF -> {
                val channelKeywords = mapOf(
                    "sms" to "SMS", "text" to "SMS",
                    "whatsapp" to "WHATSAPP", "wa" to "WHATSAPP",
                    "email" to "EMAIL", "mail" to "EMAIL",
                    "phone" to "PHONE", "call" to "PHONE"
                )
                channelKeywords.entries.find { message.lowercase().contains(it.key) }?.let {
                    result["preferredChannel"] = it.value
                }
            }
            ConversationPhase.FOLLOWUP_SCHEDULE -> {
                val freqKeywords = mapOf(
                    "weekly" to "WEEKLY", "every week" to "WEEKLY",
                    "biweekly" to "BIWEEKLY", "every two weeks" to "BIWEEKLY", "fortnightly" to "BIWEEKLY",
                    "monthly" to "MONTHLY", "every month" to "MONTHLY",
                    "quarterly" to "QUARTERLY", "every three months" to "QUARTERLY", "every quarter" to "QUARTERLY"
                )
                freqKeywords.entries.find { message.lowercase().contains(it.key) }?.let {
                    result["followUpFrequency"] = it.value
                }
            }
            else -> {}
        }

        return result
    }

    private fun llmExtract(message: String, phase: ConversationPhase): Map<String, Any> {
        return try {
            val fields = when (phase) {
                ConversationPhase.PERSONAL -> "name, ageGroup (20-30/30-40/40-50/50+), location, phone, email"
                ConversationPhase.FINANCIAL -> "incomeSource (SALARIED/BUSINESS/PROFESSIONAL/SELF_EMPLOYED/STUDENT/RETIRED), incomeRange (60-70K/70-80K/80-100K/100-150K/150K+), currentSavings"
                ConversationPhase.GOALS -> "retirementTarget (amount), retirementAge"
                ConversationPhase.CHANNEL_PREF -> "preferredChannel (SMS/WHATSAPP/EMAIL/PHONE)"
                ConversationPhase.FOLLOWUP_SCHEDULE -> "followUpFrequency (WEEKLY/BIWEEKLY/MONTHLY/QUARTERLY)"
                else -> return emptyMap()
            }

            val response = chatModel.generate(
                """Extract structured data from this customer message. Return ONLY valid JSON with these possible fields: $fields
If a field is not mentioned, omit it. Do not guess.
Message: "$message"
JSON:"""
            )

            val jsonStr = response.trim().let {
                if (it.startsWith("```")) it.lines().drop(1).dropLast(1).joinToString("\n") else it
            }
            objectMapper.readValue<Map<String, Any>>(jsonStr)
        } catch (e: Exception) {
            log.warn("LLM entity extraction failed for phase $phase: ${e.message}")
            emptyMap()
        }
    }

    private fun extractAgeGroup(message: String): String? {
        val lower = message.lowercase()
        return when {
            lower.contains("20") && lower.contains("30") -> "AGE_20_30"
            lower.contains("30") && lower.contains("40") -> "AGE_30_40"
            lower.contains("40") && lower.contains("50") -> "AGE_40_50"
            lower.contains("50+") || lower.contains("fifty") || lower.contains("above 50") -> "AGE_50_PLUS"
            lower.contains("twenties") -> "AGE_20_30"
            lower.contains("thirties") -> "AGE_30_40"
            lower.contains("forties") -> "AGE_40_50"
            else -> {
                patterns["age"]?.find(message)?.groupValues?.get(1)?.toIntOrNull()?.let { age ->
                    when {
                        age < 30 -> "AGE_20_30"
                        age < 40 -> "AGE_30_40"
                        age < 50 -> "AGE_40_50"
                        else -> "AGE_50_PLUS"
                    }
                }
            }
        }
    }

    private fun extractIncomeRange(message: String): String? {
        val amount = patterns["income"]?.find(message)?.groupValues?.get(1)?.toDoubleOrNull() ?: return null
        val lower = message.lowercase()
        val annualAmount = when {
            lower.contains("month") -> amount * 12
            lower.contains("lakh") || lower.contains("lac") || lower.contains("l") -> amount * 100000
            lower.contains("k") -> amount * 1000
            else -> amount
        }
        return when {
            annualAmount < 70000 -> "60-70K"
            annualAmount < 80000 -> "70-80K"
            annualAmount < 100000 -> "80-100K"
            annualAmount < 150000 -> "100-150K"
            else -> "150K+"
        }
    }

    private fun normalizeIncomeSource(raw: String): String = when (raw.lowercase().trim()) {
        "salaried" -> "SALARIED"
        "business" -> "BUSINESS"
        "professional" -> "PROFESSIONAL"
        "self-employed", "self employed", "selfemployed" -> "SELF_EMPLOYED"
        "student" -> "STUDENT"
        "retired" -> "RETIRED"
        else -> raw.uppercase()
    }
}
