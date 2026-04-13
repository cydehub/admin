package com.stockzeno.wms.notification;

import jakarta.validation.constraints.NotBlank;

public class SmsNotificationRequest {

    @NotBlank
    private String to;

    @NotBlank
    private String message;

    public SmsNotificationRequest(String to, String message) {
        this.to = to;
        this.message = message;
    }

    public String getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }
}
