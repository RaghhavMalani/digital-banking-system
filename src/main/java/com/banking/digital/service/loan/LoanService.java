package com.banking.digital.service.loan;

import com.banking.digital.model.Account;
import com.banking.digital.model.Loan;
import com.banking.digital.model.User;
import com.banking.digital.model.enums.LoanStatus;
import com.banking.digital.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanApprovalFacade loanApprovalFacade;
    private final com.banking.digital.repository.TransactionRepository transactionRepository;
    private final com.banking.digital.repository.AccountRepository accountRepository;

    public LoanService(LoanRepository loanRepository, LoanApprovalFacade loanApprovalFacade,
                       com.banking.digital.repository.TransactionRepository transactionRepository,
                       com.banking.digital.repository.AccountRepository accountRepository) {
        this.loanRepository = loanRepository;
        this.loanApprovalFacade = loanApprovalFacade;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Loan applyForLoan(User user, Account disbursementAccount,
                             String loanType, BigDecimal amount,
                             int tenureMonths, BigDecimal monthlyIncome) {
        Loan loan = new Loan();
        loan.setLoanNumber("LN" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        loan.setUser(user);
        loan.setDisbursementAccount(disbursementAccount);
        loan.setLoanType(loanType);
        loan.setPrincipalAmount(amount);
        loan.setTenureMonths(tenureMonths);

        Loan processed = loanApprovalFacade.processApplication(user, loan, monthlyIncome);

        if (processed.getStatus() == LoanStatus.APPROVED) {
            processed.setApprovedAt(LocalDateTime.now());
            // Set first EMI due date to 1 month from now
            processed.setNextEmiDueDate(LocalDateTime.now().plusMonths(1));
            
            // Disburse funds
            disbursementAccount.credit(amount);
            accountRepository.save(disbursementAccount);
            
            com.banking.digital.model.Transaction txn = new com.banking.digital.model.Transaction();
            txn.setReferenceId(processed.getLoanNumber());
            txn.setType(com.banking.digital.model.enums.TransactionType.TRANSFER);
            txn.setAmount(amount);
            txn.setDescription("Loan Disbursement - " + processed.getLoanNumber());
            txn.setDestinationAccount(disbursementAccount);
            transactionRepository.save(txn);
        }

        return loanRepository.save(processed);
    }

    public List<Loan> getLoansByUser(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    public Loan getLoanByNumber(String loanNumber) {
        return loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanNumber));
    }

    @Transactional
    public void processEmiPayment(Loan loan, Account paymentAccount) {
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("Only approved loans accept EMI payments.");
        }
        
        if (!paymentAccount.hasSufficientFunds(loan.getEmiAmount())) {
            throw new IllegalArgumentException("Insufficient funds in selected account to pay EMI.");
        }
        
        paymentAccount.debit(loan.getEmiAmount());
        accountRepository.save(paymentAccount);
        
        com.banking.digital.model.Transaction txn = new com.banking.digital.model.Transaction();
        txn.setReferenceId("EMI-" + loan.getLoanNumber() + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        txn.setType(com.banking.digital.model.enums.TransactionType.TRANSFER);
        txn.setAmount(loan.getEmiAmount());
        txn.setDescription("EMI Payment - " + loan.getLoanNumber());
        txn.setSourceAccount(paymentAccount);
        transactionRepository.save(txn);
        
        // Let's reduce tenure by 1 simplistically for demonstration 
        // In real systems we'd track a "paidInstallments" counter or calculate principal remaining
        int newTenure = loan.getTenureMonths() - 1;
        if (newTenure <= 0) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setTenureMonths(0);
            loan.setNextEmiDueDate(null);
        } else {
            loan.setTenureMonths(newTenure);
            if (loan.getNextEmiDueDate() != null) {
                loan.setNextEmiDueDate(loan.getNextEmiDueDate().plusMonths(1));
            } else {
                // For legacy loans that didn't have this field, the UI was displaying 
                // "now + 1 month" as the current due date.
                // So after paying, the NEXT due date should be "now + 2 months".
                loan.setNextEmiDueDate(LocalDateTime.now().plusMonths(2));
            }
        }
        loanRepository.save(loan);
    }
}
