package com.banking.digital.controller;

import com.banking.digital.model.Account;
import com.banking.digital.model.User;
import com.banking.digital.model.enums.AccountType;
import com.banking.digital.service.account.AccountService;
import com.banking.digital.service.auth.UserProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
public class DashboardController {

    private final UserProfileService userProfileService;
    private final AccountService accountService;

    public DashboardController(UserProfileService userProfileService, AccountService accountService) {
        this.userProfileService = userProfileService;
        this.accountService = accountService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = userProfileService.getUserByUsername(principal.getName());
        List<Account> accounts = accountService.getActiveAccountsByUser(user);
        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("totalBalance", totalBalance);
        return "dashboard";
    }

    @GetMapping("/profile")
    public String profilePage(Model model, Principal principal) {
        User user = userProfileService.getUserByUsername(principal.getName());
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") User updatedUser,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            userProfileService.updateProfile(principal.getName(), updatedUser);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/receive")
    public String receivePage(Model model, Principal principal) {
        User user = userProfileService.getUserByUsername(principal.getName());
        List<Account> accounts = accountService.getActiveAccountsByUser(user);
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        return "receive";
    }

    @PostMapping("/accounts/open")
    public String openAccount(@RequestParam("accountType") String accountType,
                              @RequestParam("fundingAccountId") Long fundingAccountId,
                              @RequestParam("initialDeposit") BigDecimal initialDeposit,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            if (initialDeposit.compareTo(new BigDecimal("10000")) < 0) {
                throw new IllegalArgumentException("Minimum initial deposit is ₹10,000.");
            }
            User user = userProfileService.getUserByUsername(principal.getName());
            
            Account fundingAccount = accountService.getActiveAccountsByUser(user).stream()
                .filter(a -> a.getId().equals(fundingAccountId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid funding account."));
                
            if (!fundingAccount.hasSufficientFunds(initialDeposit)) {
                throw new IllegalArgumentException("Insufficient funds in the selected funding account.");
            }
            
            AccountType type = AccountType.valueOf(accountType);
            Account newAccount = accountService.openAccount(user, type);
            
            // Transfer funds
            fundingAccount.debit(initialDeposit);
            accountService.save(fundingAccount);
            
            newAccount.credit(initialDeposit);
            accountService.save(newAccount);
            
            redirectAttributes.addFlashAttribute("success", type + " account opened successfully with ₹" + initialDeposit + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to open account: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/accounts/close/{id}")
    public String closeAccount(@PathVariable("id") Long id,
                               @RequestParam(value = "destinationAccountId", required = false) Long destinationAccountId,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userProfileService.getUserByUsername(principal.getName());
            
            Account accountToClose = accountService.getAccountsByUser(user).stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Account not found."));
            
            BigDecimal balance = accountToClose.getBalance();
            
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                if (destinationAccountId == null) {
                    throw new IllegalArgumentException("Must select a destination account to transfer remaining ₹" + balance + ".");
                }
                Account destinationAccount = accountService.getActiveAccountsByUser(user).stream()
                        .filter(a -> a.getId().equals(destinationAccountId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Invalid destination account."));
                        
                // Transfer remaining funds before closing
                accountToClose.debit(balance);
                accountService.save(accountToClose);
                
                destinationAccount.credit(balance);
                accountService.save(destinationAccount);
            }
            
            boolean success = accountService.closeAccount(user, id);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Account closed successfully." + 
                        (balance.compareTo(BigDecimal.ZERO) > 0 ? " Transferred ₹" + balance + " to selected account." : ""));
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to close account.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error closing account: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }
}
