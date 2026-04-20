package com.banking.digital.controller;

import com.banking.digital.model.Account;
import com.banking.digital.model.User;
import com.banking.digital.model.enums.AccountType;
import com.banking.digital.service.account.AccountService;
import com.banking.digital.service.auth.AuthenticationService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final AccountService accountService;

    public AuthController(AuthenticationService authenticationService, AccountService accountService) {
        this.authenticationService = authenticationService;
        this.accountService = accountService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               @RequestParam(name = "initialDeposit", required = false, defaultValue = "0") BigDecimal initialDeposit,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            if (initialDeposit.compareTo(BigDecimal.valueOf(100000)) < 0) {
                redirectAttributes.addFlashAttribute("error", "Minimum initial deposit of ₹1,00,000 is required.");
                return "redirect:/auth/register";
            }
            User savedUser = authenticationService.registerUser(user);
            Account defaultAccount = accountService.openAccount(savedUser, AccountType.SAVINGS);
            defaultAccount.credit(initialDeposit);
            accountService.save(defaultAccount);
            
            redirectAttributes.addFlashAttribute("success", "Registration successful! Account credited with initial deposit. Please login.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }
}
