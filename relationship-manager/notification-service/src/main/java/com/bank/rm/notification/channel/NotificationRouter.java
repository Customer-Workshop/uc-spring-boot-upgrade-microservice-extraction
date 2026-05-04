package com.bank.rm.notification.channel;

import com.bank.rm.common.dto.CommunicationChannel;
import com.bank.rm.common.dto.NotificationStatus;
import com.bank.rm.notification.domain.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;

public class NotificationRouter {
    private static final Logger log = LoggerFactory.getLogger(NotificationRouter.class);

    public DeliveryResult route(Notification notification) {
        return switch (notification.getChannel()) {
            case SMS -> sendSms(notification);
            case WHATSAPP -> sendWhatsApp(notification);
            case EMAIL -> sendEmail(notification);
            case PUSH -> sendPush(notification);
            default -> {
                log.warn("Channel {} not supported for automated delivery", notification.getChannel());
                yield new DeliveryResult(false, null, "Channel not supported");
            }
        };
    }

    private DeliveryResult sendSms(Notification notification) {
        log.info("Sending SMS to {}: {}", notification.getRecipientAddress(), notification.getSubject());
        // Twilio integration stub
        return new DeliveryResult(true, "sms-" + notification.getId(), null);
    }

    private DeliveryResult sendWhatsApp(Notification notification) {
        log.info("Sending WhatsApp to {}: {}", notification.getRecipientAddress(), notification.getSubject());
        return new DeliveryResult(true, "wa-" + notification.getId(), null);
    }

    private DeliveryResult sendEmail(Notification notification) {
        log.info("Sending Email to {}: {}", notification.getRecipientAddress(), notification.getSubject());
        // SendGrid integration stub
        return new DeliveryResult(true, "email-" + notification.getId(), null);
    }

    private DeliveryResult sendPush(Notification notification) {
        log.info("Sending Push to customer {}: {}", notification.getCustomerId(), notification.getSubject());
        return new DeliveryResult(true, "push-" + notification.getId(), null);
    }

    public record DeliveryResult(boolean success, String externalId, String failureReason) {}
}
