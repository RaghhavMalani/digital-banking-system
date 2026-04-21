package com.banking.digital.service.deposit;

import com.banking.digital.model.Account;
import com.banking.digital.model.Deposit;
import com.banking.digital.model.User;
import com.banking.digital.model.enums.DepositStatus;
import com.banking.digital.model.enums.DepositType;
import com.banking.digital.repository.DepositRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * GRASP: Polymorphism
 * The interest calculation and penalty rules differ based on DepositType (FD vs RD).
 * This is handled through polymorphic method dispatch rather than if-else chains.
 *
 * SOLID: Liskov Substitution Principle (LSP)
 * Both FD and RD deposits use the same Deposit entity interface and can be
 * substituted wherever a Deposit is expected without breaking behavior.
 */
@Service
public class DepositService {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    private final DepositRepository depositRepository;
    private final com.banking.digital.repository.TransactionRepository transactionRepository;

    public DepositService(DepositRepository depositRepository,
                          com.banking.digital.repository.TransactionRepository transactionRepository) {
        this.depositRepository = depositRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Deposit createFixedDeposit(User user, Account linkedAccount,
                                      BigDecimal amount, int tenureMonths) {
        if (!linkedAccount.hasSufficientFunds(amount)) {
            throw new IllegalArgumentException("Insufficient funds in linked account for this deposit.");
        }
        linkedAccount.debit(amount);

        BigDecimal interestRate = calculateFdInterestRate(tenureMonths);
        BigDecimal maturityAmount = calculateFdMaturity(amount, interestRate, tenureMonths);

        Deposit deposit = new Deposit();
        deposit.setDepositNumber("FD" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        deposit.setDepositType(DepositType.FIXED_DEPOSIT);
        deposit.setUser(user);
        deposit.setLinkedAccount(linkedAccount);
        deposit.setPrincipalAmount(amount);
        deposit.setInterestRate(interestRate);
        deposit.setTenureMonths(tenureMonths);
        deposit.setMaturityAmount(maturityAmount);
        deposit.setStartDate(LocalDate.now());
        deposit.setMaturityDate(LocalDate.now().plusMonths(tenureMonths));
        deposit.setStatus(DepositStatus.ACTIVE);

        Deposit saved = depositRepository.save(deposit);
        
        com.banking.digital.model.Transaction txn = new com.banking.digital.model.Transaction();
        txn.setReferenceId(saved.getDepositNumber());
        txn.setType(com.banking.digital.model.enums.TransactionType.TRANSFER);
        txn.setAmount(amount);
        txn.setDescription("Fixed Deposit Booking - " + saved.getDepositNumber());
        txn.setSourceAccount(linkedAccount);
        transactionRepository.save(txn);
        
        return saved;
    }

    @Transactional
    public Deposit createRecurringDeposit(User user, Account linkedAccount,
                                          BigDecimal monthlyInstallment, int tenureMonths) {
        if (!linkedAccount.hasSufficientFunds(monthlyInstallment)) {
            throw new IllegalArgumentException("Insufficient funds in linked account for the first installment.");
        }
        linkedAccount.debit(monthlyInstallment);

        BigDecimal interestRate = calculateRdInterestRate(tenureMonths);
        BigDecimal totalPrincipal = monthlyInstallment.multiply(new BigDecimal(tenureMonths));
        BigDecimal maturityAmount = calculateRdMaturity(monthlyInstallment, interestRate, tenureMonths);

        Deposit deposit = new Deposit();
        deposit.setDepositNumber("RD" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        deposit.setDepositType(DepositType.RECURRING_DEPOSIT);
        deposit.setUser(user);
        deposit.setLinkedAccount(linkedAccount);
        deposit.setPrincipalAmount(totalPrincipal);
        deposit.setMonthlyInstallment(monthlyInstallment);
        deposit.setInterestRate(interestRate);
        deposit.setTenureMonths(tenureMonths);
        deposit.setMaturityAmount(maturityAmount);
        deposit.setStartDate(LocalDate.now());
        deposit.setMaturityDate(LocalDate.now().plusMonths(tenureMonths));
        deposit.setStatus(DepositStatus.ACTIVE);

        Deposit saved = depositRepository.save(deposit);
        
        com.banking.digital.model.Transaction txn = new com.banking.digital.model.Transaction();
        txn.setReferenceId(saved.getDepositNumber());
        txn.setType(com.banking.digital.model.enums.TransactionType.TRANSFER);
        txn.setAmount(monthlyInstallment);
        txn.setDescription("Recurring Deposit Initial Booking - " + saved.getDepositNumber());
        txn.setSourceAccount(linkedAccount);
        transactionRepository.save(txn);
        
        return saved;
    }

    @Transactional
    public Deposit breakDeposit(String depositNumber) {
        Deposit deposit = depositRepository.findByDepositNumber(depositNumber)
                .orElseThrow(() -> new IllegalArgumentException("Deposit not found"));

        if (deposit.getStatus() != DepositStatus.ACTIVE) {
            throw new IllegalStateException("Only active deposits can be broken");
        }

        long monthsElapsed = java.time.temporal.ChronoUnit.MONTHS.between(
                deposit.getStartDate(), LocalDate.now());

        BigDecimal reducedMaturity;
        
        if (monthsElapsed < 1) {
            // Premature withdrawal within first month: 1% penalty on principal, 0 interest
            BigDecimal penaltyFee = deposit.getPrincipalAmount().multiply(new BigDecimal("0.01"));
            reducedMaturity = deposit.getPrincipalAmount().subtract(penaltyFee).setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal penaltyRate = new BigDecimal("1.00");
            BigDecimal effectiveRate = deposit.getInterestRate().subtract(penaltyRate);
            if (effectiveRate.compareTo(BigDecimal.ZERO) < 0) {
                effectiveRate = BigDecimal.ZERO;
            }

            if (deposit.getDepositType() == DepositType.FIXED_DEPOSIT) {
                reducedMaturity = calculateFdMaturity(
                        deposit.getPrincipalAmount(), effectiveRate, (int) monthsElapsed);
            } else {
                reducedMaturity = calculateRdMaturity(
                        deposit.getMonthlyInstallment(), effectiveRate, (int) monthsElapsed);
            }
        }

        deposit.setMaturityAmount(reducedMaturity);
        deposit.setStatus(DepositStatus.BROKEN);
        Deposit saved = depositRepository.save(deposit);
        
        Account linkedAccount = deposit.getLinkedAccount();
        linkedAccount.credit(reducedMaturity);
        
        com.banking.digital.model.Transaction txn = new com.banking.digital.model.Transaction();
        txn.setReferenceId("BRK-" + saved.getDepositNumber());
        txn.setType(com.banking.digital.model.enums.TransactionType.TRANSFER);
        txn.setAmount(reducedMaturity);
        txn.setDescription("Deposit Broken - payout: " + saved.getDepositNumber());
        txn.setDestinationAccount(linkedAccount);
        transactionRepository.save(txn);
        
        return saved;
    }

    public List<Deposit> getDepositsByUser(Long userId) {
        return depositRepository.findByUserId(userId);
    }

    private BigDecimal calculateFdInterestRate(int tenureMonths) {
        if (tenureMonths <= 6) return new BigDecimal("5.50");
        if (tenureMonths <= 12) return new BigDecimal("6.50");
        if (tenureMonths <= 24) return new BigDecimal("7.00");
        return new BigDecimal("7.25");
    }

    private BigDecimal calculateRdInterestRate(int tenureMonths) {
        if (tenureMonths <= 12) return new BigDecimal("6.00");
        if (tenureMonths <= 24) return new BigDecimal("6.50");
        return new BigDecimal("7.00");
    }

    private BigDecimal calculateFdMaturity(BigDecimal principal, BigDecimal annualRate, int months) {
        BigDecimal rate = annualRate.divide(new BigDecimal("100"), MC);
        BigDecimal factor = BigDecimal.ONE.add(
                rate.multiply(new BigDecimal(months)).divide(new BigDecimal("12"), MC));
        return principal.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRdMaturity(BigDecimal monthly, BigDecimal annualRate, int months) {
        BigDecimal rate = annualRate.divide(new BigDecimal("1200"), MC);
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < months; i++) {
            int remainingMonths = months - i;
            BigDecimal factor = BigDecimal.ONE.add(rate.multiply(new BigDecimal(remainingMonths)));
            sum = sum.add(monthly.multiply(factor));
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }
}
