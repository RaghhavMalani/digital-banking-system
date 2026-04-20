package com.banking.digital.controller;

import com.banking.digital.model.Account;
import com.banking.digital.model.Loan;
import com.banking.digital.model.ServiceRequest;
import com.banking.digital.model.User;
import com.banking.digital.model.enums.ServiceRequestType;
import com.banking.digital.service.account.AccountService;
import com.banking.digital.service.auth.UserProfileService;
import com.banking.digital.service.loan.LoanService;
import com.banking.digital.service.loan.ServiceRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

/**
 * GRASP: Controller
 * Acts as the first object beyond the UI layer that receives and coordinates
 * all loan-related and service-request system operations.
 */
@Controller
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;
    private final ServiceRequestService serviceRequestService;
    private final AccountService accountService;
    private final UserProfileService userProfileService;

    public LoanController(LoanService loanService,
                          ServiceRequestService serviceRequestService,
                          AccountService accountService,
                          UserProfileService userProfileService) {
        this.loanService = loanService;
        this.serviceRequestService = serviceRequestService;
        this.accountService = accountService;
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public String loanListPage(Model model, Principal principal) {
        User user = userProfileService.getUserByUsername(principal.getName());
        List<Loan> loans = loanService.getLoansByUser(user.getId());
        List<Account> accounts = accountService.getActiveAccountsByUser(user);
        model.addAttribute("loans", loans);
        model.addAttribute("accounts", accounts);
        return "loan/list";
    }

    @GetMapping("/apply")
    public String applyLoanPage(Model model, Principal principal) {
        User user = userProfileService.getUserByUsername(principal.getName());
        List<Account> accounts = accountService.getActiveAccountsByUser(user);
        model.addAttribute("accounts", accounts);
        return "loan/apply";
    }

    @PostMapping("/apply")
    public String applyLoan(@RequestParam String loanType,
                            @RequestParam BigDecimal amount,
                            @RequestParam int tenureMonths,
                            @RequestParam BigDecimal monthlyIncome,
                            @RequestParam Long accountId,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = userProfileService.getUserByUsername(principal.getName());
            Account account = accountService.getActiveAccountsByUser(user).stream()
                    .filter(a -> a.getId().equals(accountId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid account"));

            Loan loan = loanService.applyForLoan(user, account, loanType, amount, tenureMonths, monthlyIncome);
            String msg = switch (loan.getStatus()) {
                case APPROVED -> "Loan approved! Loan No: " + loan.getLoanNumber() +
                        " | EMI: ₹" + loan.getEmiAmount() + "/month";
                case REJECTED -> "Loan rejected. Credit Score: " + loan.getCreditScore() +
                        " | Background: " + (loan.isBackgroundCheckPassed() ? "Passed" : "Failed") +
                        " | Income: " + (loan.isIncomeVerified() ? "Verified" : "Insufficient");
                default -> "Loan application submitted. Status: " + loan.getStatus();
            };
            redirectAttributes.addFlashAttribute(
                    loan.getStatus().name().equals("APPROVED") ? "success" : "error", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Application failed: " + e.getMessage());
        }
        return "redirect:/loans";
    }

    @PostMapping("/pay-emi/{loanNumber}")
    public String payEmi(@PathVariable String loanNumber, 
                         @RequestParam Long accountId,
                         Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User user = userProfileService.getUserByUsername(principal.getName());
            Loan loan = loanService.getLoanByNumber(loanNumber);
            if (!loan.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Loan does not belong to user");
            }
            
            Account account = accountService.getActiveAccountsByUser(user).stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid account selected for EMI payment."));
                
            loanService.processEmiPayment(loan, account);
            redirectAttributes.addFlashAttribute("success", "EMI of ₹" + loan.getEmiAmount() + " processed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "EMI Payment failed: " + e.getMessage());
        }
        return "redirect:/loans";
    }

    @GetMapping("/services")
    public String servicesPage(Model model, Principal principal) {
        User user = userProfileService.getUserByUsername(principal.getName());
        List<Account> accounts = accountService.getActiveAccountsByUser(user);
        List<ServiceRequest> requests = serviceRequestService.getRequestsByUser(user.getId());
        model.addAttribute("accounts", accounts);
        model.addAttribute("serviceTypes", ServiceRequestType.values());
        model.addAttribute("requests", requests);
        return "loan/services";
    }

    @PostMapping("/services/request")
    public String createServiceRequest(@RequestParam Long accountId,
                                       @RequestParam String requestType,
                                       @RequestParam(required = false) String remarks,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        try {
            User user = userProfileService.getUserByUsername(principal.getName());
            Account account = accountService.getActiveAccountsByUser(user).stream()
                    .filter(a -> a.getId().equals(accountId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid account"));

            ServiceRequestType type = ServiceRequestType.valueOf(requestType);
            serviceRequestService.createRequest(user, account, type, remarks);
            redirectAttributes.addFlashAttribute("success", "Service request submitted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Request failed: " + e.getMessage());
        }
        return "redirect:/loans/services";
    }
}
