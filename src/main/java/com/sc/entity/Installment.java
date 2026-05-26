package com.sc.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "installments")
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "installment_id")
    private Long installmentId;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "addon_amount")
    private Integer addonAmount = 0;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "due_amount")
    private Integer dueAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "payment_mode")
    private String paymentMode;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Column(name = "remarks")
    private String remarks;



//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "fees_id")
//    private FeesEntity fees;

    // ============= RELATIONSHIP: One-to-One with Transaction =============
    @OneToOne(mappedBy = "installment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TransactionEntity transaction;

    // ============= CONSTRUCTORS =============
    public Installment() {
        this.addonAmount = 0;
        this.status = "PENDING";
    }

    public Installment(Integer amount, LocalDate dueDate) {
        this.amount = amount;
        this.dueDate = dueDate;
        this.dueAmount = amount;
        this.addonAmount = 0;
        this.status = "PENDING";
    }

    // ============= BUSINESS METHODS (unchanged) =============
    // ============= 🎯 BUSINESS LOGIC METHODS =============

    /**
     * Mark installment as paid
     */
    public void markAsPaid() {
        this.status = "PAID";
        this.paidDate = LocalDate.now();
        this.dueAmount = 0;
    }

    /**
     * Mark installment as paid with payment details
     */
    public void markAsPaid(String paymentMode, String transactionReference) {
        this.status = "PAID";
        this.paidDate = LocalDate.now();
        this.dueAmount = 0;
        this.paymentMode = paymentMode;
        this.transactionReference = transactionReference;
    }

    /**
     * Mark installment as overdue
     */
    public void markAsOverdue() {
        if (!"PAID".equals(this.status) && !"CANCELLED".equals(this.status)) {
            if (LocalDate.now().isAfter(this.dueDate)) {
                this.status = "OVERDUE";
            }
        }
    }

    /**
     * Mark installment as cancelled
     */
    public void markAsCancelled(String reason) {
        this.status = "CANCELLED";
        this.remarks = reason;
        this.dueAmount = 0;
    }

    /**
     * Add late fee / penalty
     */
    public void addLateFee(Integer lateFee) {
        if (lateFee != null && lateFee > 0) {
            this.addonAmount = (this.addonAmount != null ? this.addonAmount : 0) + lateFee;
            this.dueAmount = (this.dueAmount != null ? this.dueAmount : this.amount) + lateFee;
        }
    }

    /**
     * Check if installment is paid
     */
    public boolean isPaid() {
        return "PAID".equalsIgnoreCase(this.status);
    }

    /**
     * Check if installment is overdue
     */
    public boolean isOverdue() {
        if ("PAID".equals(this.status) || "CANCELLED".equals(this.status)) {
            return false;
        }
        return LocalDate.now().isAfter(this.dueDate);
    }

    /**
     * Check if installment is pending
     */
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(this.status);
    }

    /**
     * Get total amount (amount + addon)
     */
    public Integer getTotalAmount() {
        return (amount != null ? amount : 0) + (addonAmount != null ? addonAmount : 0);
    }

    /**
     * Get remaining days until due date
     */
    public Long getRemainingDays() {
        if (dueDate == null) return null;
        return (long) LocalDate.now().until(dueDate).getDays();
    }

    /**
     * Check if installment is due today
     */
    public boolean isDueToday() {
        return dueDate != null && dueDate.equals(LocalDate.now());
    }

    /**
     * Apply discount to installment
     */
    public void applyDiscount(Integer discountAmount) {
        if (discountAmount != null && discountAmount > 0) {
            this.dueAmount = Math.max(0, (this.dueAmount != null ? this.dueAmount : this.amount) - discountAmount);
        }
    }

    // ============= GETTERS & SETTERS =============
    public Long getInstallmentId() {
        return installmentId;
    }

    public void setInstallmentId(Long installmentId) {
        this.installmentId = installmentId;
    }

    public TransactionEntity getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionEntity transaction) {
        this.transaction = transaction;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getAddonAmount() {
        return addonAmount;
    }

    public void setAddonAmount(Integer addonAmount) {
        this.addonAmount = addonAmount;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDueAmount() {
        return dueAmount;
    }

    public void setDueAmount(Integer dueAmount) {
        this.dueAmount = dueAmount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

//    public FeesEntity getFees() {
//        return fees;
//    }
//
//    public void setFees(FeesEntity fees) {
//        this.fees = fees;
//    }


    @Override
    public String toString() {
        return "Installment{" +
                "installmentId=" + installmentId +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", dueDate=" + dueDate +
                '}';
    }
}