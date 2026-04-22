package com.banking.digital.service.transfer;

import com.banking.digital.model.Account;
import com.banking.digital.model.Transaction;
import com.banking.digital.model.enums.AccountStatus;
import com.banking.digital.model.enums.TransactionType;
import com.banking.digital.model.enums.TransferMode;
import com.banking.digital.repository.AccountRepository;
import com.banking.digital.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * SOLID: Open/Closed Principle (OCP)
 * This service accepts any TransferStrategy implementation. Adding a new transfer mode
 * (e.g., UPI) requires only a new strategy class, not changes to this service.
 *
 * GRASP: Information Expert
 * The Account entity validates its own balance via hasSufficientFunds() and performs
 * debit()/credit() operations, since it possesses the balance information.
 */
@Service
public class TransferService {

    private final Map<String, TransferStrategy> strategies;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(Map<String, TransferStrategy> strategies,
                           AccountRepository accountRepository,
                           TransactionRepository transactionRepository) {
        this.strategies = strategies;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction executeTransfer(String sourceAccountNumber,
                                       String destinationAccountNumber,
                                       BigDecimal amount,
                                       TransferMode mode,
                                       String description) {
        TransferStrategy strategy = strategies.get(mode.name());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported transfer mode: " + mode);
        }

        strategy.validate(amount);

        Account source = accountRepository.findByAccountNumber(sourceAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account destination = accountRepository.findByAccountNumber(destinationAccountNumber).orElse(null);

        if (source.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Source account is not active");
        }

        BigDecimal fee = strategy.calculateFee(amount);
        BigDecimal totalDebit = amount.add(fee);

        source.debit(totalDebit);
        accountRepository.save(source);

        Transaction transaction = new Transaction();
        transaction.setReferenceId("TXN" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setTransferMode(mode);
        transaction.setAmount(amount);
        transaction.setFee(fee);
        transaction.setDescription(description);
        transaction.setSourceAccount(source);

        if (destination != null) {
            if (destination.getStatus() != AccountStatus.ACTIVE) {
                throw new IllegalStateException("Destination account is not active");
            }
            destination.credit(amount);
            accountRepository.save(destination);
            transaction.setDestinationAccount(destination);
        } else {
            transaction.setExternalDestinationAccount(destinationAccountNumber);
            transaction.setDescription((description != null ? description + " | " : "") + "External Transfer");
        }

        return transactionRepository.save(transaction);
    }
}
