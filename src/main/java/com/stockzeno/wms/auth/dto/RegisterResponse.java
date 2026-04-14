package com.stockzeno.wms.auth.dto;

public class RegisterResponse {

    private final String message;
    private final String email;

    public RegisterResponse(String message, String email) {
        this.message = message;
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public String getEmail() {
        return email;
    }
}
