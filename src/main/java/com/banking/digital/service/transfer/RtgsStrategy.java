package com.banking.digital.service.transfer;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component("RTGS")
public class RtgsStrategy implements TransferStrategy {

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("500000")) <= 0) {
            return new BigDecimal("20.00");
        }
        return new BigDecimal("40.00");
    }

    @Override
    public BigDecimal getMinimumAmount() {
        return new BigDecimal("200000");
    }

    @Override
    public BigDecimal getMaximumAmount() {
        return new BigDecimal("10000000");
    }

    @Override
    public String getTransferModeName() {
        return "RTGS";
    }
}
