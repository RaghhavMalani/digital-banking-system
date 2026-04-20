package com.banking.digital.service.loan;

import com.banking.digital.model.Loan;
import com.banking.digital.model.User;
import com.banking.digital.model.enums.LoanStatus;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Design Pattern: Facade (Structural)
 * Simplifies the complex loan approval subsystem by providing a single entry point
 * that orchestrates CreditScoreService, BackgroundCheckService, and IncomeVerificationService.
 *
 * GRASP: Controller
 * Acts as the coordinating object beyond the UI layer for loan approval operations.
 */
@Component
public class LoanApprovalFacade {

    private final CreditScoreService creditScoreService;
    private final BackgroundCheckService backgroundCheckService;
    private final IncomeVerificationService incomeVerificationService;
    private final ILoanCalculator loanCalculator;

    public LoanApprovalFacade(CreditScoreService creditScoreService,
                              BackgroundCheckService backgroundCheckService,
                              IncomeVerificationService incomeVerificationService,
                              ILoanCalculator loanCalculator) {
        this.creditScoreService = creditScoreService;
        this.backgroundCheckService = backgroundCheckService;
        this.incomeVerificationService = incomeVerificationService;
        this.loanCalculator = loanCalculator;
    }

    public Loan processApplication(User user, Loan loan, BigDecimal declaredMonthlyIncome) {
        int creditScore = creditScoreService.getCreditScore(user);
        loan.setCreditScore(creditScore);

        boolean bgCheck = backgroundCheckService.performCheck(user);
        loan.setBackgroundCheckPassed(bgCheck);

        boolean incomeOk = incomeVerificationService.verifyIncome(
                declaredMonthlyIncome, loan.getPrincipalAmount());
        loan.setIncomeVerified(incomeOk);

        if (!creditScoreService.isEligible(creditScore)) {
            loan.setStatus(LoanStatus.REJECTED);
            return loan;
        }
        if (!bgCheck) {
            loan.setStatus(LoanStatus.REJECTED);
            return loan;
        }
        if (!incomeOk) {
            loan.setStatus(LoanStatus.REJECTED);
            return loan;
        }

        BigDecimal interestRate = determineInterestRate(creditScore, loan.getLoanType());
        loan.setInterestRate(interestRate);

        BigDecimal emi = loanCalculator.calculateEmi(
                loan.getPrincipalAmount(), interestRate, loan.getTenureMonths());
        loan.setEmiAmount(emi);
        loan.setStatus(LoanStatus.APPROVED);

        return loan;
    }

    private BigDecimal determineInterestRate(int creditScore, String loanType) {
        BigDecimal baseRate;
        if ("HOME".equalsIgnoreCase(loanType)) {
            baseRate = new BigDecimal("8.50");
        } else if ("CAR".equalsIgnoreCase(loanType)) {
            baseRate = new BigDecimal("9.50");
        } else if ("EDUCATION".equalsIgnoreCase(loanType)) {
            baseRate = new BigDecimal("7.50");
        } else {
            baseRate = new BigDecimal("12.00");
        }

        if (creditScore >= 800) {
            return baseRate.subtract(new BigDecimal("1.00"));
        } else if (creditScore >= 750) {
            return baseRate.subtract(new BigDecimal("0.50"));
        }
        return baseRate;
    }
}
