package com.banking.digital.service.transfer;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component("IMPS")
public class ImpsStrategy implements TransferStrategy {

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("10000")) <= 0) {
            return new BigDecimal("2.50");
        } else if (amount.compareTo(new BigDecimal("100000")) <= 0) {
            return new BigDecimal("5.00");
        }
        return new BigDecimal("15.00");
    }

    @Override
    public BigDecimal getMinimumAmount() {
        return new BigDecimal("1.00");
    }

    @Override
    public BigDecimal getMaximumAmount() {
        return new BigDecimal("500000");
    }

    @Override
    public String getTransferModeName() {
        return "IMPS";
    }
}
