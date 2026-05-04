package com.bank.rm.conversation.engine

import com.bank.rm.common.dto.ConversationPhase
import com.bank.rm.common.dto.ConversationStatus
import org.springframework.stereotype.Component

@Component
class ConversationStateMachine {

    private val validTransitions = mapOf(
        ConversationPhase.GREETING to setOf(ConversationPhase.PERSONAL),
        ConversationPhase.PERSONAL to setOf(ConversationPhase.FINANCIAL),
        ConversationPhase.FINANCIAL to setOf(ConversationPhase.GOALS),
        ConversationPhase.GOALS to setOf(ConversationPhase.RISK_ASSESSMENT),
        ConversationPhase.RISK_ASSESSMENT to setOf(ConversationPhase.RECOMMENDATION),
        ConversationPhase.RECOMMENDATION to setOf(ConversationPhase.CHANNEL_PREF),
        ConversationPhase.CHANNEL_PREF to setOf(ConversationPhase.FOLLOWUP_SCHEDULE),
        ConversationPhase.FOLLOWUP_SCHEDULE to setOf(ConversationPhase.COMPLETED)
    )

    fun canTransition(from: ConversationPhase, to: ConversationPhase): Boolean =
        validTransitions[from]?.contains(to) == true

    fun getNextPhase(current: ConversationPhase): ConversationPhase? =
        validTransitions[current]?.firstOrNull()

    fun isTerminal(phase: ConversationPhase): Boolean =
        phase == ConversationPhase.COMPLETED

    fun determineStatus(phase: ConversationPhase): ConversationStatus = when (phase) {
        ConversationPhase.COMPLETED -> ConversationStatus.COMPLETED
        else -> ConversationStatus.ACTIVE
    }
}
