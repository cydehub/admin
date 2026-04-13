package com.stockzeno.wms.notification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailNotificationRequest {

    @Email
    @NotBlank
    private String to;

    @NotBlank
    private String subject;

    @NotBlank
    private String body;

    public EmailNotificationRequest(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}
