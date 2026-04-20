package com.banking.digital.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class OtpFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Permit non-authenticated routes and static assets
        if (path.startsWith("/auth/") || path.startsWith("/css/") || 
            path.startsWith("/js/") || path.startsWith("/h2-console") || 
            path.equals("/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // If authenticated but OTP is not verified, redirect to /auth/otp
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
            !SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
            
            HttpSession session = request.getSession(false);
            if (session != null) {
                Boolean otpVerified = (Boolean) session.getAttribute("OTP_VERIFIED");
                if (otpVerified == null || !otpVerified) {
                    response.sendRedirect("/auth/otp");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
