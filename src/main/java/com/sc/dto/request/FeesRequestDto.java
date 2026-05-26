package com.sc.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.*;

public class FeesRequestDto {

    private Long studentId;
    private Integer admissionFees;
    private Integer uniformFees;
    private Integer bookFees;
    private Integer tuitionFees;
    private Map<String, Integer> additionalFeesList;
    private Integer initialAmount;
    private String paymentMode;
    private List<InstallmentDto> installmentsList;
    private String cashierName;
    private String transactionId;

    // ============= 🆕 MISSING FIELD - ADD THIS =============
    private String academicYear;  // 👈 THIS WAS MISSING!

    // ============= 🔄 CONSTRUCTORS =============

    public FeesRequestDto() {
        this.additionalFeesList = new HashMap<>();
        this.installmentsList = new ArrayList<>();
    }

    public FeesRequestDto(Long studentId, String academicYear) {
        this.studentId = studentId;
        this.academicYear = academicYear;
        this.additionalFeesList = new HashMap<>();
        this.installmentsList = new ArrayList<>();
    }

    // ============= 🎯 GETTERS AND SETTERS =============

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Integer getAdmissionFees() {
        return admissionFees;
    }

    public void setAdmissionFees(Integer admissionFees) {
        this.admissionFees = admissionFees;
    }

    public Integer getUniformFees() {
        return uniformFees;
    }

    public void setUniformFees(Integer uniformFees) {
        this.uniformFees = uniformFees;
    }

    public Integer getBookFees() {
        return bookFees;
    }

    public void setBookFees(Integer bookFees) {
        this.bookFees = bookFees;
    }

    public Integer getTuitionFees() {
        return tuitionFees;
    }

    public void setTuitionFees(Integer tuitionFees) {
        this.tuitionFees = tuitionFees;
    }

    public Map<String, Integer> getAdditionalFeesList() {
        return additionalFeesList;
    }

    public void setAdditionalFeesList(Map<String, Integer> additionalFeesList) {
        this.additionalFeesList = additionalFeesList != null ? additionalFeesList : new HashMap<>();
    }

    public Integer getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(Integer initialAmount) {
        this.initialAmount = initialAmount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public List<InstallmentDto> getInstallmentsList() {
        return installmentsList;
    }

    public void setInstallmentsList(List<InstallmentDto> installmentsList) {
        this.installmentsList = installmentsList != null ? installmentsList : new ArrayList<>();
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    // ============= 🆕 ADD THESE METHODS - THIS FIXES THE ERROR =============

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    // ============= 🎯 HELPER METHODS =============

    /**
     * Add an additional fee
     */
    public void addAdditionalFee(String name, Integer amount) {
        if (this.additionalFeesList == null) {
            this.additionalFeesList = new HashMap<>();
        }
        this.additionalFeesList.put(name, amount);
    }

    /**
     * Add an installment
     */
    public void addInstallment(InstallmentDto installment) {
        if (this.installmentsList == null) {
            this.installmentsList = new ArrayList<>();
        }
        this.installmentsList.add(installment);
    }

    /**
     * Check if fees data exists
     */
    public boolean hasFeesData() {
        return admissionFees != null ||
                uniformFees != null ||
                bookFees != null ||
                tuitionFees != null ||
                initialAmount != null ||
                (additionalFeesList != null && !additionalFeesList.isEmpty()) ||
                (installmentsList != null && !installmentsList.isEmpty());
    }

    /**
     * Calculate total base fees (admission + uniform + book + tuition)
     */
    public Integer getTotalBaseFees() {
        return (admissionFees != null ? admissionFees : 0) +
                (uniformFees != null ? uniformFees : 0) +
                (bookFees != null ? bookFees : 0) +
                (tuitionFees != null ? tuitionFees : 0);
    }

    /**
     * Calculate total additional fees
     */
    public Integer getTotalAdditionalFees() {
        if (additionalFeesList == null || additionalFeesList.isEmpty()) {
            return 0;
        }
        return additionalFeesList.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Calculate total fees
     */
    public Integer getTotalFees() {
        return getTotalBaseFees() + getTotalAdditionalFees();
    }

    // ============= 📝 INNER CLASS FOR INSTALLMENT DTO =============

    public static class InstallmentDto {
        private Long installmentId;
        private Integer amount;
        private Integer addonAmount = 0;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate paidDate;

        private String status = "PENDING";
        private Integer dueAmount;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dueDate;

        private String paymentMode;
        private String transactionReference;
        private String remarks;

        // Constructors
        public InstallmentDto() {}

        public InstallmentDto(Long installmentId, Integer amount, LocalDate dueDate) {
            this.installmentId = installmentId;
            this.amount = amount;
            this.dueDate = dueDate;
            this.dueAmount = amount;
            this.status = "PENDING";
            this.addonAmount = 0;
        }

        public InstallmentDto(Long installmentId, Integer amount, Integer addonAmount,
                              LocalDate paidDate, String status, Integer dueAmount,
                              LocalDate dueDate, String paymentMode, String transactionReference) {
            this.installmentId = installmentId;
            this.amount = amount;
            this.addonAmount = addonAmount != null ? addonAmount : 0;
            this.paidDate = paidDate;
            this.status = status != null ? status : "PENDING";
            this.dueAmount = dueAmount;
            this.dueDate = dueDate;
            this.paymentMode = paymentMode;
            this.transactionReference = transactionReference;
        }

        // Getters and Setters
        public Long getInstallmentId() { return installmentId; }
        public void setInstallmentId(Long installmentId) { this.installmentId = installmentId; }

        public Integer getAmount() { return amount; }
        public void setAmount(Integer amount) { this.amount = amount; }

        public Integer getAddonAmount() { return addonAmount; }
        public void setAddonAmount(Integer addonAmount) {
            this.addonAmount = addonAmount != null ? addonAmount : 0;
        }

        public LocalDate getPaidDate() { return paidDate; }
        public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Integer getDueAmount() { return dueAmount; }
        public void setDueAmount(Integer dueAmount) { this.dueAmount = dueAmount; }

        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

        public String getPaymentMode() { return paymentMode; }
        public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) {
            this.transactionReference = transactionReference;
        }

        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }

        // Business Methods
        public Integer getTotalAmount() {
            return (amount != null ? amount : 0) + (addonAmount != null ? addonAmount : 0);
        }

        public boolean isPaid() {
            return "PAID".equalsIgnoreCase(status);
        }

        public boolean isPending() {
            return "PENDING".equalsIgnoreCase(status);
        }

        public boolean isOverdue() {
            return "OVERDUE".equalsIgnoreCase(status);
        }

        @Override
        public String toString() {
            return "InstallmentDto{" +
                    "installmentId=" + installmentId +
                    ", amount=" + amount +
                    ", status='" + status + '\'' +
                    ", dueDate=" + dueDate +
                    '}';
        }
    }

    // ============= 📝 TOSTRING METHOD =============

    @Override
    public String toString() {
        return "FeesRequestDto{" +
                "studentId=" + studentId +
                ", academicYear='" + academicYear + '\'' +
                ", totalFees=" + getTotalFees() +
                ", initialAmount=" + initialAmount +
                ", paymentMode='" + paymentMode + '\'' +
                ", installmentsCount=" + (installmentsList != null ? installmentsList.size() : 0) +
                '}';
    }
}