package com.stockzeno.wms.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "stockzeno.notifications.enabled", havingValue = "true")
@ConditionalOnProperty(name = "stockzeno.notifications.provider", havingValue = "smtp")
public class SmtpNotificationService implements NotificationService {

    private final JavaMailSender mailSender;
    private final String defaultFrom;

    public SmtpNotificationService(JavaMailSender mailSender,
                                   NotificationProperties properties,
                                   @Value("${spring.mail.username:}") String mailUsername) {
        this.mailSender = mailSender;
        String configuredFrom = properties.getEmailFrom();
        if (configuredFrom != null && !configuredFrom.isBlank()) {
            this.defaultFrom = configuredFrom;
        } else {
            this.defaultFrom = mailUsername;
        }
    }

    @Override
    public void sendEmail(EmailNotificationRequest request) {
        if (defaultFrom == null || defaultFrom.isBlank()) {
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(defaultFrom);
        message.setTo(request.getTo());
        message.setSubject(request.getSubject());
        message.setText(request.getBody());
        mailSender.send(message);
    }

    @Override
    public void sendSms(SmsNotificationRequest request) {
        // SMTP-only provider does not handle SMS.
    }
}
