package com.banking.digital.controller;

import com.banking.digital.model.Account;
import com.banking.digital.model.Transaction;
import com.banking.digital.model.User;
import com.banking.digital.service.account.AccountService;
import com.banking.digital.service.auth.UserProfileService;
import com.banking.digital.service.deposit.StatementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/statements")
public class StatementController {

    private final StatementService statementService;
    private final AccountService accountService;
    private final UserProfileService userProfileService;

    public StatementController(StatementService statementService,
                               AccountService accountService,
                               UserProfileService userProfileService) {
        this.statementService = statementService;
        this.accountService = accountService;
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public String statementPage(Model model, Principal principal) {
        User user = userProfileService.getUserByUsername(principal.getName());
        List<Account> accounts = accountService.getAccountsByUser(user);
        model.addAttribute("accounts", accounts);
        return "statement/generate";
    }

    @GetMapping("/generate")
    public String generateStatement(@RequestParam Long accountId,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                    Model model, Principal principal) {
        User user = userProfileService.getUserByUsername(principal.getName());
        Account account = accountService.getAccountsByUser(user).stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid account"));

        List<Transaction> transactions = statementService.generateStatement(account, fromDate, toDate);
        List<Account> accounts = accountService.getAccountsByUser(user);

        model.addAttribute("accounts", accounts);
        model.addAttribute("selectedAccount", account);
        model.addAttribute("transactions", transactions);
        model.addAttribute("toDate", toDate);
        return "statement/generate";
    }

    @GetMapping("/all")
    public String viewAllTransactions(Model model, Principal principal) {
        User user = userProfileService.getUserByUsername(principal.getName());
        List<Account> accounts = accountService.getAccountsByUser(user);
        
        List<Transaction> allTransactions = new java.util.ArrayList<>();
        for (Account acc : accounts) {
            allTransactions.addAll(statementService.getAllTransactions(acc));
        }
        
        // Sort by date descending
        allTransactions.sort((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()));
        
        model.addAttribute("accounts", accounts);
        model.addAttribute("transactions", allTransactions);
        return "statement/all";
    }
}
