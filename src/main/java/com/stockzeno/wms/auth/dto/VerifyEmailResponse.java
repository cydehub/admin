package com.stockzeno.wms.auth.dto;

public class VerifyEmailResponse {

    private final String message;

    public VerifyEmailResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
