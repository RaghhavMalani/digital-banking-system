package com.banking.digital.service.transfer;

import java.math.BigDecimal;

/**
 * Design Pattern: Strategy (Behavioral)
 * Defines a family of transfer algorithms (IMPS, NEFT, RTGS) that are
 * interchangeable at runtime, each with its own fee structure and limits.
 *
 * SOLID: Open/Closed Principle (OCP)
 * New transfer modes can be added by implementing this interface
 * without modifying the existing TransferService code.
 */
public interface TransferStrategy {

    BigDecimal calculateFee(BigDecimal amount);

    BigDecimal getMinimumAmount();

    BigDecimal getMaximumAmount();

    String getTransferModeName();

    default void validate(BigDecimal amount) {
        if (amount.compareTo(getMinimumAmount()) < 0) {
            throw new IllegalArgumentException(
                    getTransferModeName() + " minimum amount is ₹" + getMinimumAmount());
        }
        if (amount.compareTo(getMaximumAmount()) > 0) {
            throw new IllegalArgumentException(
                    getTransferModeName() + " maximum amount is ₹" + getMaximumAmount());
        }
    }
}
