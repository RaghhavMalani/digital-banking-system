package com.banking.digital.controller;

import com.banking.digital.model.Account;
import com.banking.digital.model.Beneficiary;
import com.banking.digital.model.Transaction;
import com.banking.digital.model.User;
import com.banking.digital.model.enums.TransferMode;
import com.banking.digital.service.account.AccountService;
import com.banking.digital.service.auth.UserProfileService;
import com.banking.digital.service.transfer.BeneficiaryService;
import com.banking.digital.service.transfer.TransferService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService transferService;
    private final BeneficiaryService beneficiaryService;
    private final AccountService accountService;
    private final UserProfileService userProfileService;

    public TransferController(TransferService transferService,
                              BeneficiaryService beneficiaryService,
                              AccountService accountService,
                              UserProfileService userProfileService) {
        this.transferService = transferService;
        this.beneficiaryService = beneficiaryService;
        this.accountService = accountService;
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public String transferPage(Model model, Principal principal) {
        User user = userProfileService.getUserByUsername(principal.getName());
        List<Account> accounts = accountService.getActiveAccountsByUser(user);
        List<Beneficiary> beneficiaries = beneficiaryService.getBeneficiariesByUser(user.getId());

        model.addAttribute("accounts", accounts);
        model.addAttribute("beneficiaries", beneficiaries);
        model.addAttribute("transferModes", TransferMode.values());
        return "transfer/transfer";
    }

    @PostMapping("/execute")
    public String executeTransfer(@RequestParam String sourceAccount,
                                  @RequestParam String destinationAccount,
                                  @RequestParam BigDecimal amount,
                                  @RequestParam String transferMode,
                                  @RequestParam(required = false) String description,
                                  RedirectAttributes redirectAttributes) {
        try {
            TransferMode mode = TransferMode.valueOf(transferMode);
            Transaction txn = transferService.executeTransfer(
                    sourceAccount, destinationAccount, amount, mode, description);
            redirectAttributes.addFlashAttribute("success",
                    "Transfer successful! Reference: " + txn.getReferenceId() +
                    " | Fee: ₹" + txn.getFee());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Transfer failed: " + e.getMessage());
        }
        return "redirect:/transfers";
    }

    @GetMapping("/beneficiaries")
    public String beneficiariesPage(Model model, Principal principal) {
        User user = userProfileService.getUserByUsername(principal.getName());
        List<Beneficiary> beneficiaries = beneficiaryService.getBeneficiariesByUser(user.getId());
        model.addAttribute("beneficiaries", beneficiaries);
        model.addAttribute("newBeneficiary", new Beneficiary());
        return "transfer/beneficiaries";
    }

    @PostMapping("/beneficiaries/add")
    public String addBeneficiary(@ModelAttribute("newBeneficiary") Beneficiary beneficiary,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userProfileService.getUserByUsername(principal.getName());
            beneficiaryService.addBeneficiary(user, beneficiary);
            redirectAttributes.addFlashAttribute("success", "Beneficiary added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed: " + e.getMessage());
        }
        return "redirect:/transfers/beneficiaries";
    }

    @PostMapping("/beneficiaries/delete/{id}")
    public String deleteBeneficiary(@PathVariable Long id,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            User user = userProfileService.getUserByUsername(principal.getName());
            beneficiaryService.deleteBeneficiary(id, user.getId());
            redirectAttributes.addFlashAttribute("success", "Beneficiary removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed: " + e.getMessage());
        }
        return "redirect:/transfers/beneficiaries";
    }
}
