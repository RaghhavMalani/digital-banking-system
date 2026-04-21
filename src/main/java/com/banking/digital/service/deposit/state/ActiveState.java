package com.banking.digital.service.deposit.state;

import com.banking.digital.model.Account;
import java.math.BigDecimal;

public class ActiveState implements AccountState {

    @Override
    public void deposit(Account account, BigDecimal amount) {
        account.credit(amount);
    }

    @Override
    public void withdraw(Account account, BigDecimal amount) {
        account.debit(amount);
    }

    @Override
    public boolean canTransfer() {
        return true;
    }

    @Override
    public String getStateName() {
        return "ACTIVE";
    }
}
