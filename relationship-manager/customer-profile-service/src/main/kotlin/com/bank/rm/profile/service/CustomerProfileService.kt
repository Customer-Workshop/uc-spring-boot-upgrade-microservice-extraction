package com.bank.rm.profile.service

import com.bank.rm.common.dto.CommunicationChannel
import com.bank.rm.common.event.DomainEvent
import com.bank.rm.common.event.KafkaTopics
import com.bank.rm.common.event.ProfileUpdatedEvent
import com.bank.rm.common.exception.ResourceNotFoundException
import com.bank.rm.profile.domain.*
import com.bank.rm.profile.dto.*
import com.bank.rm.profile.repository.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalTime
import java.util.UUID

@Service
class CustomerProfileService(
    private val customerRepository: CustomerRepository,
    private val financialProfileRepository: FinancialProfileRepository,
    private val riskProfileRepository: RiskProfileRepository,
    private val communicationPreferenceRepository: CommunicationPreferenceRepository,
    private val kafkaTemplate: KafkaTemplate<String, DomainEvent>,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    fun createCustomer(request: CreateCustomerRequest): CustomerResponse {
        val customer = Customer(
            name = request.name,
            email = request.email,
            phone = request.phone,
            ageGroup = request.ageGroup,
            location = request.location,
            isAnonymous = request.isAnonymous,
            anonymousSessionId = request.anonymousSessionId
        )
        customerRepository.save(customer)
        return customer.toResponse()
    }

    fun getCustomer(customerId: UUID): CustomerResponse {
        val customer = findCustomer(customerId)
        return customer.toResponse()
    }

    fun getFullProfile(customerId: UUID): FullProfileResponse {
        val customer = findCustomer(customerId)
        val financial = financialProfileRepository.findByCustomerId(customerId)
        val risk = riskProfileRepository.findByCustomerIdAndIsCurrentTrue(customerId)
        val commPref = communicationPreferenceRepository.findByCustomerId(customerId)

        return FullProfileResponse(
            customer = customer.toResponse(),
            financialProfile = financial?.toResponse(),
            riskProfile = risk?.toResponse(),
            communicationPreferences = commPref?.toResponse()
        )
    }

    @Transactional
    fun updateCustomer(customerId: UUID, request: UpdateCustomerRequest): CustomerResponse {
        val customer = findCustomer(customerId)
        val updatedFields = mutableListOf<String>()

        request.name?.let { customer.name = it; updatedFields.add("name") }
        request.email?.let { customer.email = it; updatedFields.add("email") }
        request.phone?.let { customer.phone = it; updatedFields.add("phone") }
        request.ageGroup?.let { customer.ageGroup = it; updatedFields.add("ageGroup") }
        request.location?.let { customer.location = it; updatedFields.add("location") }
        customer.updatedAt = Instant.now()

        customerRepository.save(customer)

        if (updatedFields.isNotEmpty()) {
            kafkaTemplate.send(
                KafkaTopics.PROFILE_EVENTS,
                customerId.toString(),
                ProfileUpdatedEvent(customerId = customerId, updatedFields = updatedFields)
            )
        }

        return customer.toResponse()
    }

    @Transactional
    fun saveFinancialProfile(customerId: UUID, request: FinancialProfileRequest): FinancialProfileResponse {
        findCustomer(customerId)
        val existing = financialProfileRepository.findByCustomerId(customerId)

        val investments = request.currentInvestments?.let { objectMapper.writeValueAsString(it) } ?: "{}"

        val profile = existing?.copy(
            incomeSource = request.incomeSource.name,
            incomeRange = request.incomeRange.label,
            currentInvestments = investments,
            currentSavings = request.currentSavings ?: BigDecimal.ZERO,
            monthlyExpenses = request.monthlyExpenses,
            monthlySavingsCapacity = request.monthlySavingsCapacity,
            retirementTargetAmount = request.retirementTargetAmount,
            retirementTargetAge = request.retirementTargetAge,
            updatedAt = Instant.now()
        ) ?: FinancialProfile(
            customerId = customerId,
            incomeSource = request.incomeSource.name,
            incomeRange = request.incomeRange.label,
            currentInvestments = investments,
            currentSavings = request.currentSavings ?: BigDecimal.ZERO,
            monthlyExpenses = request.monthlyExpenses,
            monthlySavingsCapacity = request.monthlySavingsCapacity,
            retirementTargetAmount = request.retirementTargetAmount,
            retirementTargetAge = request.retirementTargetAge
        )

        financialProfileRepository.save(profile)

        kafkaTemplate.send(
            KafkaTopics.PROFILE_EVENTS,
            customerId.toString(),
            ProfileUpdatedEvent(customerId = customerId, updatedFields = listOf("financialProfile"))
        )

        return profile.toResponse()
    }

    @Transactional
    fun saveCommunicationPreference(customerId: UUID, request: CommunicationPreferenceRequest): CommunicationPreferenceResponse {
        findCustomer(customerId)
        val existing = communicationPreferenceRepository.findByCustomerId(customerId)

        val pref = existing?.apply {
            preferredChannel = request.preferredChannel
            preferredTimeStart = request.preferredTimeStart?.let { LocalTime.parse(it) }
            preferredTimeEnd = request.preferredTimeEnd?.let { LocalTime.parse(it) }
            preferredDays = request.preferredDays
            timezone = request.timezone
            optInSms = request.optInSms
            optInWhatsapp = request.optInWhatsapp
            optInEmail = request.optInEmail
            optInPhone = request.optInPhone
            updatedAt = Instant.now()
        } ?: CommunicationPreference(
            customerId = customerId,
            preferredChannel = request.preferredChannel,
            preferredTimeStart = request.preferredTimeStart?.let { LocalTime.parse(it) },
            preferredTimeEnd = request.preferredTimeEnd?.let { LocalTime.parse(it) },
            preferredDays = request.preferredDays,
            timezone = request.timezone,
            optInSms = request.optInSms,
            optInWhatsapp = request.optInWhatsapp,
            optInEmail = request.optInEmail,
            optInPhone = request.optInPhone
        )

        communicationPreferenceRepository.save(pref)
        return pref.toResponse()
    }

    private fun findCustomer(customerId: UUID): Customer =
        customerRepository.findById(customerId).orElseThrow {
            ResourceNotFoundException("Customer not found: $customerId")
        }

    private fun Customer.toResponse() = CustomerResponse(id, name, email, phone, ageGroup, location, status)

    private fun FinancialProfile.toResponse(): FinancialProfileResponse {
        val investments: Map<String, BigDecimal> = try {
            objectMapper.readValue(currentInvestments, objectMapper.typeFactory.constructMapType(Map::class.java, String::class.java, BigDecimal::class.java))
        } catch (e: Exception) { emptyMap() }

        return FinancialProfileResponse(
            incomeSource, incomeRange, investments, currentSavings,
            monthlySavingsCapacity, retirementTargetAmount, retirementTargetAge
        )
    }

    private fun RiskProfile.toResponse() = RiskProfileResponse(riskScore, riskCategory, explanation, assessedAt.toString())

    private fun CommunicationPreference.toResponse() = CommunicationPreferenceResponse(
        preferredChannel, preferredTimeStart?.toString(), preferredTimeEnd?.toString(), preferredDays, timezone
    )
}
