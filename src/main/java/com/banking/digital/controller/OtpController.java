package com.banking.digital.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class OtpController {

    @GetMapping("/otp")
    public String showOtpPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("OTP_VERIFIED") == null) {
            return "redirect:/auth/login"; // Not in an OTP flow
        }
        
        Boolean verified = (Boolean) session.getAttribute("OTP_VERIFIED");
        if (verified) {
            return "redirect:/dashboard"; // Already verified
        }

        // Show dummy OTP for testing
        String generatedOtp = (String) session.getAttribute("GENERATED_OTP");
        model.addAttribute("generatedOtp", generatedOtp);
        
        return "auth/otp";
    }

    @PostMapping("/otp")
    public String verifyOtp(@RequestParam("otp") String otp, HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/auth/login";
        }

        String expectedOtp = (String) session.getAttribute("GENERATED_OTP");
        
        if (expectedOtp != null && expectedOtp.equals(otp)) {
            session.setAttribute("OTP_VERIFIED", true);
            session.removeAttribute("GENERATED_OTP"); // Clear after use
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Invalid OTP. Please try again.");
            model.addAttribute("generatedOtp", expectedOtp);
            return "auth/otp";
        }
    }
}
