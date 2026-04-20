package com.banking.digital.repository;

import com.banking.digital.model.Account;
import com.banking.digital.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount = :account OR t.destinationAccount = :account ORDER BY t.timestamp DESC")
    List<Transaction> findByAccount(@Param("account") Account account);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount = :account OR t.destinationAccount = :account) " +
           "AND t.timestamp BETWEEN :start AND :end ORDER BY t.timestamp DESC")
    List<Transaction> findByAccountAndDateRange(
            @Param("account") Account account,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
