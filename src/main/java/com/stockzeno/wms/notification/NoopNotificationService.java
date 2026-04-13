package com.stockzeno.wms.notification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "stockzeno.notifications.enabled", havingValue = "false", matchIfMissing = true)
public class NoopNotificationService implements NotificationService {

    @Override
    public void sendEmail(EmailNotificationRequest request) {
        // Stub: integrate SendGrid/SES later.
    }

    @Override
    public void sendSms(SmsNotificationRequest request) {
        // Stub: integrate Twilio or SMS provider later.
    }
}
