package com.banking.digital.service.loan;

import com.banking.digital.model.User;
import org.springframework.stereotype.Service;

@Service
public class CreditScoreService {

    /**
     * Simulates a credit score check. In production this would call an external bureau API.
     */
    public int getCreditScore(User user) {
        int hash = Math.abs(user.getUsername().hashCode());
        return 600 + (hash % 251);
    }

    public boolean isEligible(int creditScore) {
        return creditScore >= 650;
    }
}
