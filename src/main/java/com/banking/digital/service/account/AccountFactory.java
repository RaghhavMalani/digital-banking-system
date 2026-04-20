package com.banking.digital.service.account;

import com.banking.digital.model.Account;
import com.banking.digital.model.User;
import com.banking.digital.model.enums.AccountStatus;
import com.banking.digital.model.enums.AccountType;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Design Pattern: Factory Method (Creational)
 * Creates different Account types with type-specific defaults for interest rate,
 * minimum balance, and other attributes.
 */
public class AccountFactory {

    private AccountFactory() {}

    public static Account createAccount(AccountType type, User user) {
        return switch (type) {
            case SAVINGS -> createSavingsAccount(user);
            case CURRENT -> createCurrentAccount(user);
        };
    }

    private static Account createSavingsAccount(User user) {
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(AccountType.SAVINGS);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);
        account.setInterestRate(new BigDecimal("4.00"));
        account.setMinimumBalance(new BigDecimal("1000.00"));
        account.setUser(user);
        return account;
    }

    private static Account createCurrentAccount(User user) {
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(AccountType.CURRENT);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);
        account.setInterestRate(BigDecimal.ZERO);
        account.setMinimumBalance(new BigDecimal("5000.00"));
        account.setUser(user);
        return account;
    }

    private static String generateAccountNumber() {
        long number = Math.abs(UUID.randomUUID().getMostSignificantBits() % 10000000000000000L);
        return String.format("%016d", number);
    }
}
