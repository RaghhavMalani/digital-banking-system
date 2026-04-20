package com.banking.digital.model;

public class Authentication {

    public boolean validateCredential(String email, String password) {
        // Implementation for credential validation
        return false;
    }

    public String generateOTP() {
        // Implementation to generate OTP
        return "123456";
    }

    public boolean verifyOTP(String otp) {
        // Implementation to verify OTP
        return true;
    }
}
