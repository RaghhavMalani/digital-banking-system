package com.banking.digital.service.deposit.state;

import com.banking.digital.model.enums.AccountStatus;

public class AccountStateFactory {

    private AccountStateFactory() {}

    public static AccountState getState(AccountStatus status) {
        return switch (status) {
            case ACTIVE -> new ActiveState();
            case DORMANT -> new DormantState();
            case FROZEN, CLOSED -> new FrozenState();
        };
    }
}
