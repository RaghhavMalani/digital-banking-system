package com.banking.digital.controller;

import com.banking.digital.model.Account;
import com.banking.digital.model.Deposit;
import com.banking.digital.model.User;
import com.banking.digital.service.account.AccountService;
import com.banking.digital.service.auth.UserProfileService;
import com.banking.digital.service.deposit.DepositService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/deposits")
public class DepositController {

    private final DepositService depositService;
    private final AccountService accountService;
    private final UserProfileService userProfileService;

    public DepositController(DepositService depositService,
                             AccountService accountService,
                             UserProfileService userProfileService) {
        this.depositService = depositService;
        this.accountService = accountService;
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public String depositListPage(Model model, Principal principal) {
        User user = userProfileService.getUserByUsername(principal.getName());
        List<Deposit> deposits = depositService.getDepositsByUser(user.getId());
        model.addAttribute("deposits", deposits);
        return "deposit/list";
    }

    @GetMapping("/create")
    public String createDepositPage(Model model, Principal principal) {
        User user = userProfileService.getUserByUsername(principal.getName());
        List<Account> accounts = accountService.getActiveAccountsByUser(user);
        model.addAttribute("accounts", accounts);
        return "deposit/create";
    }

    @PostMapping("/create/fd")
    public String createFd(@RequestParam Long accountId,
                           @RequestParam BigDecimal amount,
                           @RequestParam int tenureMonths,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userProfileService.getUserByUsername(principal.getName());
            Account account = accountService.getActiveAccountsByUser(user).stream()
                    .filter(a -> a.getId().equals(accountId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid account"));

            Deposit fd = depositService.createFixedDeposit(user, account, amount, tenureMonths);
            redirectAttributes.addFlashAttribute("success",
                    "Fixed Deposit created! No: " + fd.getDepositNumber() +
                    " | Maturity: ₹" + fd.getMaturityAmount() +
                    " on " + fd.getMaturityDate());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed: " + e.getMessage());
        }
        return "redirect:/deposits";
    }

    @PostMapping("/create/rd")
    public String createRd(@RequestParam Long accountId,
                           @RequestParam BigDecimal monthlyAmount,
                           @RequestParam int tenureMonths,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userProfileService.getUserByUsername(principal.getName());
            Account account = accountService.getActiveAccountsByUser(user).stream()
                    .filter(a -> a.getId().equals(accountId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid account"));

            Deposit rd = depositService.createRecurringDeposit(user, account, monthlyAmount, tenureMonths);
            redirectAttributes.addFlashAttribute("success",
                    "Recurring Deposit created! No: " + rd.getDepositNumber() +
                    " | Monthly: ₹" + rd.getMonthlyInstallment() +
                    " | Maturity: ₹" + rd.getMaturityAmount());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed: " + e.getMessage());
        }
        return "redirect:/deposits";
    }

    @PostMapping("/break/{depositNumber}")
    public String breakDeposit(@PathVariable String depositNumber,
                               RedirectAttributes redirectAttributes) {
        try {
            Deposit broken = depositService.breakDeposit(depositNumber);
            redirectAttributes.addFlashAttribute("success",
                    "Deposit " + depositNumber + " broken. Payout: ₹" + broken.getMaturityAmount() +
                    " (after penalty).");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed: " + e.getMessage());
        }
        return "redirect:/deposits";
    }
}
