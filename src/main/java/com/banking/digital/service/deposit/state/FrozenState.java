package com.banking.digital.service.deposit.state;

import com.banking.digital.model.Account;
import java.math.BigDecimal;

public class FrozenState implements AccountState {

    @Override
    public void deposit(Account account, BigDecimal amount) {
        throw new IllegalStateException("Cannot deposit into a frozen account. Contact bank support.");
    }

    @Override
    public void withdraw(Account account, BigDecimal amount) {
        throw new IllegalStateException("Cannot withdraw from a frozen account. Contact bank support.");
    }

    @Override
    public boolean canTransfer() {
        return false;
    }

    @Override
    public String getStateName() {
        return "FROZEN";
    }
}
