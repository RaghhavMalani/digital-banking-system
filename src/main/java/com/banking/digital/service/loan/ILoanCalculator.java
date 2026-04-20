package com.banking.digital.service.loan;

import java.math.BigDecimal;

/**
 * SOLID: Dependency Inversion Principle (DIP)
 * High-level modules (LoanService, LoanApprovalFacade) depend on this abstraction
 * rather than concrete EMI calculator implementations.
 */
public interface ILoanCalculator {

    BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int tenureMonths);

    BigDecimal calculateTotalPayable(BigDecimal emi, int tenureMonths);

    BigDecimal calculateTotalInterest(BigDecimal principal, BigDecimal totalPayable);
}
