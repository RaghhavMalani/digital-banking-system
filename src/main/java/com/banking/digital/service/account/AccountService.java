package com.banking.digital.service.account;

import com.banking.digital.model.Account;
import com.banking.digital.model.User;
import com.banking.digital.model.enums.AccountType;
import com.banking.digital.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account openAccount(User user, AccountType type) {
        Account account = user.createAccount(type);
        return accountRepository.save(account);
    }

    public List<Account> getAccountsByUser(User user) {
        return accountRepository.findByUser(user);
    }
    
    public List<Account> getActiveAccountsByUser(User user) {
        return accountRepository.findByUser(user).stream()
                .filter(a -> a.getStatus() != com.banking.digital.model.enums.AccountStatus.CLOSED)
                .collect(java.util.stream.Collectors.toList());
    }

    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));
    }

    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    @Transactional
    public boolean closeAccount(User user, Long accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));
            
        if (!account.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Account does not belong to user.");
        }
        
        if (account.getBalance().compareTo(java.math.BigDecimal.ZERO) != 0) {
            return false; // Can't close account with non-zero balance
        }
        
        account.setStatus(com.banking.digital.model.enums.AccountStatus.CLOSED);
        accountRepository.save(account);
        return true;
    }

    @Transactional
    public Account save(Account account) {
        return accountRepository.save(account);
    }
}
