package com.banking.digital.service.loan;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Component
public class EmiCalculator implements ILoanCalculator {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    @Override
    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int tenureMonths) {
        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(new BigDecimal(tenureMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), MC);
        // EMI = P * r * (1+r)^n / ((1+r)^n - 1)
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = onePlusR.pow(tenureMonths, MC);
        BigDecimal numerator = principal.multiply(monthlyRate, MC).multiply(power, MC);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTotalPayable(BigDecimal emi, int tenureMonths) {
        return emi.multiply(new BigDecimal(tenureMonths));
    }

    @Override
    public BigDecimal calculateTotalInterest(BigDecimal principal, BigDecimal totalPayable) {
        return totalPayable.subtract(principal);
    }
}
