package com.stockzeno.wms.notification;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.io.IOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "stockzeno.notifications.enabled", havingValue = "true")
@ConditionalOnProperty(name = "stockzeno.notifications.provider", havingValue = "sendgrid", matchIfMissing = true)
public class SendgridTwilioNotificationService implements NotificationService {

    private final NotificationProperties properties;

    public SendgridTwilioNotificationService(NotificationProperties properties) {
        this.properties = properties;
        if (hasTwilioConfig()) {
            Twilio.init(properties.getTwilioAccountSid(), properties.getTwilioAuthToken());
        }
    }

    @Override
    public void sendEmail(EmailNotificationRequest request) {
        if (!hasSendgridConfig()) {
            return;
        }
        Email from = new Email(properties.getEmailFrom());
        Email to = new Email(request.getTo());
        Content content = new Content("text/plain", request.getBody());
        Mail mail = new Mail(from, request.getSubject(), to, content);
        SendGrid sendGrid = new SendGrid(properties.getSendgridApiKey());
        Request sgRequest = new Request();
        try {
            sgRequest.setMethod(Method.POST);
            sgRequest.setEndpoint("mail/send");
            sgRequest.setBody(mail.build());
            sendGrid.api(sgRequest);
        } catch (IOException ex) {
            // Intentionally swallow to keep notifications non-blocking.
        }
    }

    @Override
    public void sendSms(SmsNotificationRequest request) {
        if (!hasTwilioConfig()) {
            return;
        }
        Message.creator(
                new PhoneNumber(request.getTo()),
                new PhoneNumber(properties.getSmsFrom()),
                request.getMessage()
        ).create();
    }

    private boolean hasSendgridConfig() {
        return properties.getSendgridApiKey() != null
                && !properties.getSendgridApiKey().isBlank()
                && properties.getEmailFrom() != null
                && !properties.getEmailFrom().isBlank();
    }

    private boolean hasTwilioConfig() {
        return properties.getTwilioAccountSid() != null
                && !properties.getTwilioAccountSid().isBlank()
                && properties.getTwilioAuthToken() != null
                && !properties.getTwilioAuthToken().isBlank()
                && properties.getSmsFrom() != null
                && !properties.getSmsFrom().isBlank();
    }
}
