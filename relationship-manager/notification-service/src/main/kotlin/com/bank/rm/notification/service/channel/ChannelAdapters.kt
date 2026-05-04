package com.bank.rm.notification.service.channel

import com.bank.rm.notification.domain.Notification
import com.bank.rm.notification.service.ChannelDeliveryResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SmsChannelAdapter(
    @Value("\${notification.twilio.account-sid:}") private val accountSid: String,
    @Value("\${notification.twilio.auth-token:}") private val authToken: String,
    @Value("\${notification.twilio.from-number:}") private val fromNumber: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun send(notification: Notification): ChannelDeliveryResult {
        if (accountSid.isEmpty()) {
            log.info("SMS (mock): To={}, Body={}", notification.recipientAddress, notification.body.take(160))
            return ChannelDeliveryResult(success = true, externalId = "mock-sms-${notification.id}")
        }

        // Production: Twilio API call
        try {
            com.twilio.Twilio.init(accountSid, authToken)
            val message = com.twilio.rest.api.v2010.account.Message.creator(
                com.twilio.type.PhoneNumber(notification.recipientAddress),
                com.twilio.type.PhoneNumber(fromNumber),
                notification.body.take(160)
            ).create()

            return ChannelDeliveryResult(success = true, externalId = message.sid)
        } catch (e: Exception) {
            log.error("Twilio SMS error: ${e.message}")
            throw e
        }
    }
}

@Component
class EmailChannelAdapter(
    @Value("\${notification.sendgrid.api-key:}") private val apiKey: String,
    @Value("\${notification.sendgrid.from-email:noreply@bank.com}") private val fromEmail: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun send(notification: Notification): ChannelDeliveryResult {
        if (apiKey.isEmpty()) {
            log.info("Email (mock): To={}, Subject={}", notification.recipientAddress, notification.subject)
            return ChannelDeliveryResult(success = true, externalId = "mock-email-${notification.id}")
        }

        // Production: SendGrid API call
        try {
            val sg = com.sendgrid.SendGrid(apiKey)
            val from = com.sendgrid.helpers.mail.objects.Email(fromEmail, "Bank RM")
            val to = com.sendgrid.helpers.mail.objects.Email(notification.recipientAddress)
            val content = com.sendgrid.helpers.mail.objects.Content("text/html", notification.body)
            val mail = com.sendgrid.helpers.mail.Mail(from, notification.subject, to, content)

            val request = com.sendgrid.Request()
            request.method = com.sendgrid.Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()

            val response = sg.api(request)
            return ChannelDeliveryResult(success = response.statusCode in 200..299, externalId = "sg-${notification.id}")
        } catch (e: Exception) {
            log.error("SendGrid email error: ${e.message}")
            throw e
        }
    }
}

@Component
class WhatsAppChannelAdapter(
    @Value("\${notification.twilio.account-sid:}") private val accountSid: String,
    @Value("\${notification.twilio.auth-token:}") private val authToken: String,
    @Value("\${notification.twilio.whatsapp-number:}") private val whatsappNumber: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun send(notification: Notification): ChannelDeliveryResult {
        if (accountSid.isEmpty()) {
            log.info("WhatsApp (mock): To={}, Body={}", notification.recipientAddress, notification.body.take(200))
            return ChannelDeliveryResult(success = true, externalId = "mock-wa-${notification.id}")
        }

        try {
            com.twilio.Twilio.init(accountSid, authToken)
            val message = com.twilio.rest.api.v2010.account.Message.creator(
                com.twilio.type.PhoneNumber("whatsapp:${notification.recipientAddress}"),
                com.twilio.type.PhoneNumber("whatsapp:$whatsappNumber"),
                notification.body
            ).create()

            return ChannelDeliveryResult(success = true, externalId = message.sid)
        } catch (e: Exception) {
            log.error("Twilio WhatsApp error: ${e.message}")
            throw e
        }
    }
}

@Component
class PushNotificationAdapter {
    private val log = LoggerFactory.getLogger(javaClass)

    fun send(notification: Notification): ChannelDeliveryResult {
        log.info("Push (mock): To={}, Title={}", notification.customerId, notification.subject)
        return ChannelDeliveryResult(success = true, externalId = "mock-push-${notification.id}")
    }
}
