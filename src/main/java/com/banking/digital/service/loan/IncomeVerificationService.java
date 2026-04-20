package com.banking.digital.service.loan;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class IncomeVerificationService {

    private static final BigDecimal INCOME_TO_LOAN_RATIO = new BigDecimal("5");

    /**
     * Simulates income verification. Checks if the declared monthly income
     * supports the requested loan amount.
     */
    public boolean verifyIncome(BigDecimal declaredMonthlyIncome, BigDecimal loanAmount) {
        if (declaredMonthlyIncome == null || declaredMonthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        BigDecimal maxEligibleLoan = declaredMonthlyIncome.multiply(new BigDecimal("60"));
        return loanAmount.compareTo(maxEligibleLoan) <= 0;
    }
}
