package com.sc.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "transaction_table")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transId;

    @Column(name = "student_id")
    private Long studentId;

    // ========= BIDIRECTIONAL ONE-TO-ONE =========
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_id", unique = true, nullable = true)
    private Installment installment;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "amount_paid")
    private Double amountPaid;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_mode")
    private String paymentMode;

    @Column(name = "cashier_name")
    private String cashierName;

    private String status;

    private String remarks;

    // ============= GETTERS & SETTERS =============
    public Long getTransId() {
        return transId;
    }

    public void setTransId(Long transId) {
        this.transId = transId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Installment getInstallment() {
        return installment;
    }

    public void setInstallment(Installment installment) {
        this.installment = installment;
        if (installment != null) {
            installment.setTransaction(this);  // Maintain bidirectional sync
        }
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "TransactionEntity{" +
                "transId=" + transId +
                ", installmentId=" + (installment != null ? installment.getInstallmentId() : null) +
                ", amountPaid=" + amountPaid +
                ", status='" + status + '\'' +
                '}';
    }
}