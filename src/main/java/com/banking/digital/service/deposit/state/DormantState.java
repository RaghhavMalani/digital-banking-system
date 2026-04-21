package com.banking.digital.service.deposit.state;

import com.banking.digital.model.Account;
import java.math.BigDecimal;

public class DormantState implements AccountState {

    @Override
    public void deposit(Account account, BigDecimal amount) {
        account.credit(amount);
    }

    @Override
    public void withdraw(Account account, BigDecimal amount) {
        throw new IllegalStateException("Cannot withdraw from a dormant account. Please reactivate first.");
    }

    @Override
    public boolean canTransfer() {
        return false;
    }

    @Override
    public String getStateName() {
        return "DORMANT";
    }
}
