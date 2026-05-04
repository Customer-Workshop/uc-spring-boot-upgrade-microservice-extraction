package com.bank.rm.common.dto

enum class AgeGroup(val label: String, val midpoint: Int) {
    AGE_20_30("20-30", 25),
    AGE_30_40("30-40", 35),
    AGE_40_50("40-50", 45),
    AGE_50_PLUS("50+", 55);

    companion object {
        fun fromAge(age: Int): AgeGroup = when {
            age < 30 -> AGE_20_30
            age < 40 -> AGE_30_40
            age < 50 -> AGE_40_50
            else -> AGE_50_PLUS
        }

        fun fromLabel(label: String): AgeGroup =
            entries.first { it.label == label }
    }
}

enum class IncomeSource {
    SALARIED, BUSINESS, PROFESSIONAL, SELF_EMPLOYED, STUDENT, RETIRED
}

enum class IncomeRange(val label: String, val min: Int, val max: Int) {
    RANGE_60_70K("60-70K", 60000, 70000),
    RANGE_70_80K("70-80K", 70000, 80000),
    RANGE_80_100K("80-100K", 80000, 100000),
    RANGE_100_150K("100-150K", 100000, 150000),
    RANGE_150K_PLUS("150K+", 150000, Int.MAX_VALUE);

    companion object {
        fun fromLabel(label: String): IncomeRange =
            entries.first { it.label == label }
    }
}

enum class RiskCategory(val minScore: Double, val maxScore: Double) {
    CONSERVATIVE(1.0, 3.0),
    MODERATE(3.0, 6.0),
    AGGRESSIVE(6.0, 8.0),
    VERY_AGGRESSIVE(8.0, 10.0);

    companion object {
        fun fromScore(score: Double): RiskCategory = when {
            score <= 3.0 -> CONSERVATIVE
            score <= 6.0 -> MODERATE
            score <= 8.0 -> AGGRESSIVE
            else -> VERY_AGGRESSIVE
        }
    }
}

enum class ConversationPhase {
    GREETING, PERSONAL, FINANCIAL, GOALS,
    RISK_ASSESSMENT, RECOMMENDATION, CHANNEL_PREF,
    FOLLOWUP_SCHEDULE, COMPLETED
}

enum class ConversationStatus {
    ACTIVE, PAUSED, COMPLETED, ABANDONED, HANDED_OFF
}

enum class CommunicationChannel {
    SMS, WHATSAPP, EMAIL, PHONE, WEB, MOBILE, PUSH, CALENDAR, IN_APP
}

enum class NotificationType {
    FOLLOWUP_REMINDER, FOLLOWUP_CONFIRMATION, RECOMMENDATION, ACTION_ITEM, WELCOME, GENERAL
}

enum class NotificationStatus {
    PENDING, QUEUED, SENT, DELIVERED, FAILED, BOUNCED, REJECTED
}

enum class FollowUpFrequency {
    WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY
}

enum class FollowUpStatus {
    SCHEDULED, REMINDER_SENT, IN_PROGRESS, COMPLETED, MISSED, RESCHEDULED, CANCELLED
}

enum class ActionItemStatus {
    OPEN, IN_PROGRESS, COMPLETED, CANCELLED
}

enum class SenderType {
    CUSTOMER, AI, HUMAN_RM
}

enum class ContentType {
    TEXT, IMAGE, DOCUMENT, RICH_CARD, QUICK_REPLY, CHART
}

enum class ProductCategory {
    SAVINGS_ACCOUNT, FIXED_DEPOSIT, RECURRING_DEPOSIT,
    MUTUAL_FUND, EQUITY, INSURANCE, PENSION, GOLD, BOND,
    NPS, PPF, TAX_SAVER
}

enum class ProductRiskLevel {
    LOW, MODERATE, HIGH, VERY_HIGH
}
