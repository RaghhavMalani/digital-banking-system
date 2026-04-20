package com.banking.digital.model;

import com.banking.digital.model.enums.LoanStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String loanNumber;

    private String loanType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate;

    private int tenureMonths;

    @Column(precision = 15, scale = 2)
    private BigDecimal emiAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.PENDING;

    private int creditScore;
    private boolean backgroundCheckPassed;
    private boolean incomeVerified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account disbursementAccount;

    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
    
    private LocalDateTime nextEmiDueDate;

    @PrePersist
    protected void onCreate() {
        this.appliedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLoanNumber() { return loanNumber; }
    public void setLoanNumber(String loanNumber) { this.loanNumber = loanNumber; }

    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public int getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(int tenureMonths) { this.tenureMonths = tenureMonths; }

    public BigDecimal getEmiAmount() { return emiAmount; }
    public void setEmiAmount(BigDecimal emiAmount) { this.emiAmount = emiAmount; }

    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }

    public int getCreditScore() { return creditScore; }
    public void setCreditScore(int creditScore) { this.creditScore = creditScore; }

    public boolean isBackgroundCheckPassed() { return backgroundCheckPassed; }
    public void setBackgroundCheckPassed(boolean backgroundCheckPassed) { this.backgroundCheckPassed = backgroundCheckPassed; }

    public boolean isIncomeVerified() { return incomeVerified; }
    public void setIncomeVerified(boolean incomeVerified) { this.incomeVerified = incomeVerified; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Account getDisbursementAccount() { return disbursementAccount; }
    public void setDisbursementAccount(Account disbursementAccount) { this.disbursementAccount = disbursementAccount; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public LocalDateTime getNextEmiDueDate() { return nextEmiDueDate; }
    public void setNextEmiDueDate(LocalDateTime nextEmiDueDate) { this.nextEmiDueDate = nextEmiDueDate; }
}
