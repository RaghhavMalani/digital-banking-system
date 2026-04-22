package com.banking.digital.service.transfer;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component("NEFT")
public class NeftStrategy implements TransferStrategy {

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("10000")) <= 0) {
            return new BigDecimal("2.00");
        } else if (amount.compareTo(new BigDecimal("100000")) <= 0) {
            return new BigDecimal("4.00");
        } else if (amount.compareTo(new BigDecimal("200000")) <= 0) {
            return new BigDecimal("12.00");
        }
        return new BigDecimal("20.00");
    }

    @Override
    public BigDecimal getMinimumAmount() {
        return new BigDecimal("1.00");
    }

    @Override
    public BigDecimal getMaximumAmount() {
        return new BigDecimal("1000000");
    }

    @Override
    public String getTransferModeName() {
        return "NEFT";
    }
}
