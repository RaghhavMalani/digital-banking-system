package com.banking.digital.service.loan;

import com.banking.digital.model.User;
import org.springframework.stereotype.Service;

@Service
public class BackgroundCheckService {

    /**
     * Simulates a background verification check.
     * Returns true if user has complete KYC details.
     */
    public boolean performCheck(User user) {
        return user.getPanNumber() != null && !user.getPanNumber().isBlank()
                && user.getAadhaarNumber() != null && !user.getAadhaarNumber().isBlank();
    }
}
