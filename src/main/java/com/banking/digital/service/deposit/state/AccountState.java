package com.banking.digital.service.deposit.state;

import com.banking.digital.model.Account;
import java.math.BigDecimal;

/**
 * Design Pattern: State (Behavioral)
 * Defines the interface for account state-specific behavior.
 * Each concrete state (Active, Dormant, Frozen) alters the behavior of
 * deposit, withdrawal, and transfer operations.
 *
 * GRASP: Polymorphism
 * Different states handle the same operations differently at runtime,
 * eliminating conditional logic in the Account class.
 */
public interface AccountState {

    void deposit(Account account, BigDecimal amount);

    void withdraw(Account account, BigDecimal amount);

    boolean canTransfer();

    String getStateName();
}
