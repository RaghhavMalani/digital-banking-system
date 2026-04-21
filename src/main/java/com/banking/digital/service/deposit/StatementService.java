package com.banking.digital.service.deposit;

import com.banking.digital.model.Account;
import com.banking.digital.model.Transaction;
import com.banking.digital.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class StatementService {

    private final TransactionRepository transactionRepository;

    public StatementService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> generateStatement(Account account, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            LocalDate temp = from;
            from = to;
            to = temp;
        }
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return transactionRepository.findByAccountAndDateRange(account, start, end);
    }

    public List<Transaction> getAllTransactions(Account account) {
        return transactionRepository.findByAccount(account);
    }
}
