package com.banking.digital.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Require OTP verification upon successful password login
        request.getSession().setAttribute("OTP_VERIFIED", false);
        
        // Use the Authentication model we just created to generate a dummy OTP
        com.banking.digital.model.Authentication authModel = new com.banking.digital.model.Authentication();
        String otp = authModel.generateOTP();
        
        // In a real app we'd SMS/Email this. For testing, we put it in session to show on UI
        request.getSession().setAttribute("GENERATED_OTP", otp);
        
        response.sendRedirect("/auth/otp");
    }
}
