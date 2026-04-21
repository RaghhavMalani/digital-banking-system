package com.banking.digital.model;

import com.banking.digital.model.enums.DepositStatus;
import com.banking.digital.model.enums.DepositType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposits")
public class Deposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String depositNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepositType depositType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate;

    private int tenureMonths;

    @Column(precision = 15, scale = 2)
    private BigDecimal maturityAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyInstallment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepositStatus status = DepositStatus.ACTIVE;

    private LocalDate startDate;
    private LocalDate maturityDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account linkedAccount;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.startDate == null) {
            this.startDate = LocalDate.now();
        }
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDepositNumber() { return depositNumber; }
    public void setDepositNumber(String depositNumber) { this.depositNumber = depositNumber; }

    public DepositType getDepositType() { return depositType; }
    public void setDepositType(DepositType depositType) { this.depositType = depositType; }

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public int getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(int tenureMonths) { this.tenureMonths = tenureMonths; }

    public BigDecimal getMaturityAmount() { return maturityAmount; }
    public void setMaturityAmount(BigDecimal maturityAmount) { this.maturityAmount = maturityAmount; }

    public BigDecimal getMonthlyInstallment() { return monthlyInstallment; }
    public void setMonthlyInstallment(BigDecimal monthlyInstallment) { this.monthlyInstallment = monthlyInstallment; }

    public DepositStatus getStatus() { return status; }
    public void setStatus(DepositStatus status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Account getLinkedAccount() { return linkedAccount; }
    public void setLinkedAccount(Account linkedAccount) { this.linkedAccount = linkedAccount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
