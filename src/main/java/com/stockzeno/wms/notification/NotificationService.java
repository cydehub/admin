package com.stockzeno.wms.notification;

public interface NotificationService {
    void sendEmail(EmailNotificationRequest request);
    void sendSms(SmsNotificationRequest request);
}
