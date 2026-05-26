package com.sc.dto.response;


import com.sc.entity.TransactionEntity;
import java.time.LocalDate;


public class TransactionResponseDto {

    private Long transId;
    private Long studentId;
    private Long installmentId;          // still Long in DTO (API contract unchanged)
    private String transactionId;
    private Double amountPaid;
    private LocalDate paymentDate;
    private String paymentMode;
    private String cashierName;
    private String status;
    private String remarks;

    // Updated constructor
    public TransactionResponseDto(TransactionEntity entity) {
        this.transId = entity.getTransId();
        this.studentId = entity.getStudentId();

        // Fix: get ID from the related Installment entity
        this.installmentId = (entity.getInstallment() != null)
                ? entity.getInstallment().getInstallmentId()
                : null;

        this.transactionId = entity.getTransactionId();
        this.amountPaid = entity.getAmountPaid();
        this.paymentDate = entity.getPaymentDate();
        this.paymentMode = entity.getPaymentMode();
        this.cashierName = entity.getCashierName();
        this.status = entity.getStatus();
        this.remarks = entity.getRemarks();
    }

    // Getters (unchanged)
    public Long getTransId() { return transId; }
    public Long getStudentId() { return studentId; }
    public Long getInstallmentId() { return installmentId; }
    public String getTransactionId() { return transactionId; }
    public Double getAmountPaid() { return amountPaid; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getPaymentMode() { return paymentMode; }
    public String getCashierName() { return cashierName; }
    public String getStatus() { return status; }
    public String getRemarks() { return remarks; }
}