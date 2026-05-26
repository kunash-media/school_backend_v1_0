package com.sc.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.*;

public class FeesResponseDto {

    // ============= 🆔 BASIC INFORMATION =============
    private Long id;
    private Long studentId;

    // ============= 👨‍🎓 STUDENT DETAILS =============
    private String studentName;
    private String studentRollNumber;
    private String studentClass;
    private String studentSection;

    // ============= 💰 FEE BREAKDOWN =============
    private Integer admissionFees;
    private Integer uniformFees;
    private Integer bookFees;
    private Integer tuitionFees;
    private Map<String, Integer> additionalFeesList;

    // ============= 💵 PAYMENT DETAILS =============
    private Integer totalFees;
    private Integer initialAmount;
    private String paymentMode;
    private Integer remainingFees;
    private String cashierName;
    private String transactionId;
    private String academicYear;

    // ============= 📊 PAYMENT STATUS =============
    private String paymentStatus;
    private boolean isFullyPaid;
    private Integer totalPaidAmount;
    private Double paymentPercentage;

    // ============= 📋 INSTALLMENTS =============
    private List<InstallmentDto> installmentsList;

    // ============= ⏰ TIMESTAMPS =============
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    // ============= 🔄 CONSTRUCTORS =============

    public FeesResponseDto() {
        this.additionalFeesList = new HashMap<>();
        this.installmentsList = new ArrayList<>();
        this.paymentStatus = "PENDING";
        this.isFullyPaid = false;
        this.totalPaidAmount = 0;
        this.paymentPercentage = 0.0;
    }

    public FeesResponseDto(Long id, Long studentId) {
        this.id = id;
        this.studentId = studentId;
        this.additionalFeesList = new HashMap<>();
        this.installmentsList = new ArrayList<>();
        this.paymentStatus = "PENDING";
        this.isFullyPaid = false;
        this.totalPaidAmount = 0;
        this.paymentPercentage = 0.0;
    }

    // ============= 🆔 BASIC INFORMATION GETTERS/SETTERS =============

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    // ============= 👨‍🎓 STUDENT DETAILS GETTERS/SETTERS =============

    public String getStudentName() {
        return studentName;
    }
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }
    public void setStudentRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public String getStudentClass() {
        return studentClass;
    }
    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public String getStudentSection() {
        return studentSection;
    }
    public void setStudentSection(String studentSection) {
        this.studentSection = studentSection;
    }

    // ============= 💰 FEE BREAKDOWN GETTERS/SETTERS =============

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

    // ============= 💵 PAYMENT DETAILS GETTERS/SETTERS =============

    public Integer getTotalFees() {
        return totalFees;
    }
    public void setTotalFees(Integer totalFees) {
        this.totalFees = totalFees;
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

    public Integer getRemainingFees() {
        return remainingFees;
    }
    public void setRemainingFees(Integer remainingFees) {
        this.remainingFees = remainingFees;
        calculatePaymentStatus();
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

    public String getAcademicYear() {
        return academicYear;
    }
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    // ============= 📊 PAYMENT STATUS GETTERS/SETTERS =============

    public String getPaymentStatus() {
        return paymentStatus;
    }
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public boolean isFullyPaid() {
        return isFullyPaid;
    }
    public void setFullyPaid(boolean fullyPaid) {
        isFullyPaid = fullyPaid;
    }

    public Integer getTotalPaidAmount() {
        return totalPaidAmount;
    }
    public void setTotalPaidAmount(Integer totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    public Double getPaymentPercentage() {
        return paymentPercentage;
    }
    public void setPaymentPercentage(Double paymentPercentage) {
        this.paymentPercentage = paymentPercentage;
    }

    // ============= 📋 INSTALLMENTS GETTERS/SETTERS =============

    public List<InstallmentDto> getInstallmentsList() {
        return installmentsList;
    }
    public void setInstallmentsList(List<InstallmentDto> installmentsList) {
        this.installmentsList = installmentsList != null ? installmentsList : new ArrayList<>();
    }

    // ============= ⏰ TIMESTAMPS GETTERS/SETTERS =============

    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
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
     * Calculate total paid amount
     */
    public void calculateTotalPaidAmount() {
        int paidAmount = 0;

        // Add initial amount
        if (initialAmount != null) {
            paidAmount += initialAmount;
        }

        // Add paid installments
        if (installmentsList != null) {
            paidAmount += installmentsList.stream()
                    .filter(InstallmentDto::isPaid)
                    .mapToInt(inst -> {
                        int amount = inst.getAmount() != null ? inst.getAmount() : 0;
                        int addon = inst.getAddonAmount() != null ? inst.getAddonAmount() : 0;
                        return amount + addon;
                    })
                    .sum();
        }

        this.totalPaidAmount = paidAmount;
    }

    /**
     * Calculate payment percentage
     */
    public void calculatePaymentPercentage() {
        if (totalFees != null && totalFees > 0) {
            if (totalPaidAmount == null) {
                calculateTotalPaidAmount();
            }
            this.paymentPercentage = (totalPaidAmount * 100.0) / totalFees;
        } else {
            this.paymentPercentage = 0.0;
        }
    }

    /**
     * Calculate payment status
     */
    public void calculatePaymentStatus() {
        if (totalPaidAmount == null) {
            calculateTotalPaidAmount();
        }

        if (remainingFees == null) {
            if (totalFees != null) {
                this.remainingFees = totalFees - (totalPaidAmount != null ? totalPaidAmount : 0);
            }
        }

        if (remainingFees != null && remainingFees <= 0) {
            this.paymentStatus = "FULLY PAID";
            this.isFullyPaid = true;
            this.remainingFees = 0;
        } else if (totalPaidAmount != null && totalPaidAmount > 0) {
            this.paymentStatus = "PARTIALLY PAID";
            this.isFullyPaid = false;
        } else {
            this.paymentStatus = "PENDING";
            this.isFullyPaid = false;
        }

        calculatePaymentPercentage();
    }

    /**
     * Get total fee breakdown as map
     */
    public Map<String, Integer> getFeeBreakdown() {
        Map<String, Integer> breakdown = new LinkedHashMap<>();

        if (admissionFees != null && admissionFees > 0) {
            breakdown.put("Admission Fee", admissionFees);
        }
        if (uniformFees != null && uniformFees > 0) {
            breakdown.put("Uniform Fee", uniformFees);
        }
        if (bookFees != null && bookFees > 0) {
            breakdown.put("Book Fee", bookFees);
        }
        if (tuitionFees != null && tuitionFees > 0) {
            breakdown.put("Tuition Fee", tuitionFees);
        }
        if (additionalFeesList != null && !additionalFeesList.isEmpty()) {
            breakdown.putAll(additionalFeesList);
        }

        return breakdown;
    }

    /**
     * Get number of paid installments
     */
    public long getPaidInstallmentsCount() {
        if (installmentsList == null) return 0;
        return installmentsList.stream()
                .filter(InstallmentDto::isPaid)
                .count();
    }

    /**
     * Get number of pending installments
     */
    public long getPendingInstallmentsCount() {
        if (installmentsList == null) return 0;
        return installmentsList.stream()
                .filter(i -> !i.isPaid())
                .count();
    }

    /**
     * Get number of overdue installments
     */
    public long getOverdueInstallmentsCount() {
        if (installmentsList == null) return 0;
        return installmentsList.stream()
                .filter(InstallmentDto::isOverdue)
                .count();
    }

    // ============= 📝 INNER CLASS FOR INSTALLMENT DTO =============

    public static class InstallmentDto {
        private Long installmentId;
        private Integer amount;
        private Integer addonAmount;
        private Integer totalAmount;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate paidDate;

        private String status;
        private Integer dueAmount;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dueDate;

        private String paymentMode;
        private String transactionReference;
        private String remarks;
        private Long remainingDays;
        private boolean isOverdue;
        private boolean isPaid;

        // Constructors
        public InstallmentDto() {
            this.addonAmount = 0;
            this.status = "PENDING";
            this.isPaid = false;
            this.isOverdue = false;
        }

        public InstallmentDto(Long installmentId, Integer amount, LocalDate dueDate) {
            this.installmentId = installmentId;
            this.amount = amount;
            this.dueDate = dueDate;
            this.dueAmount = amount;
            this.totalAmount = amount;
            this.status = "PENDING";
            this.addonAmount = 0;
            this.isPaid = false;
            this.remainingDays = calculateRemainingDays();
        }

        // Getters and Setters
        public Long getInstallmentId() {
            return installmentId;
        }
        public void setInstallmentId(Long installmentId) {
            this.installmentId = installmentId;
        }

        public Integer getAmount() {
            return amount;
        }
        public void setAmount(Integer amount) {
            this.amount = amount;
            calculateTotalAmount();
        }

        public Integer getAddonAmount() {
            return addonAmount != null ? addonAmount : 0;
        }
        public void setAddonAmount(Integer addonAmount) {
            this.addonAmount = addonAmount != null ? addonAmount : 0;
            calculateTotalAmount();
        }

        public Integer getTotalAmount() {
            return totalAmount;
        }
        public void setTotalAmount(Integer totalAmount) {
            this.totalAmount = totalAmount;
        }

        public LocalDate getPaidDate() {
            return paidDate;
        }
        public void setPaidDate(LocalDate paidDate) {
            this.paidDate = paidDate;
            if (paidDate != null) {
                this.status = "PAID";
                this.isPaid = true;
                this.dueAmount = 0;
            }
        }

        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
            this.isPaid = "PAID".equalsIgnoreCase(status);
            this.isOverdue = "OVERDUE".equalsIgnoreCase(status);
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
            this.remainingDays = calculateRemainingDays();
            checkOverdue();
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

        public Long getRemainingDays() {
            return remainingDays;
        }
        public void setRemainingDays(Long remainingDays) {
            this.remainingDays = remainingDays;
        }

        public boolean isOverdue() {
            return isOverdue;
        }
        public void setOverdue(boolean overdue) {
            isOverdue = overdue;
        }

        public boolean isPaid() {
            return isPaid;
        }
        public void setPaid(boolean paid) {
            isPaid = paid;
        }

        // Helper Methods
        private void calculateTotalAmount() {
            int amt = amount != null ? amount : 0;
            int addon = addonAmount != null ? addonAmount : 0;
            this.totalAmount = amt + addon;
        }

        private Long calculateRemainingDays() {
            if (dueDate == null) return null;
            return (long) LocalDate.now().until(dueDate).getDays();
        }

        private void checkOverdue() {
            if (!isPaid && dueDate != null && LocalDate.now().isAfter(dueDate)) {
                this.isOverdue = true;
                this.status = "OVERDUE";
            }
        }

        public void markAsPaid() {
            this.status = "PAID";
            this.isPaid = true;
            this.paidDate = LocalDate.now();
            this.dueAmount = 0;
            this.isOverdue = false;
        }

        public void markAsPaid(String paymentMode, String transactionReference) {
            markAsPaid();
            this.paymentMode = paymentMode;
            this.transactionReference = transactionReference;
        }

        @Override
        public String toString() {
            return "InstallmentDto{" +
                    "installmentId=" + installmentId +
                    ", amount=" + amount +
                    ", status='" + status + '\'' +
                    ", dueDate=" + dueDate +
                    ", isPaid=" + isPaid +
                    '}';
        }
    }

    // ============= 📝 OVERRIDE METHODS =============

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeesResponseDto that = (FeesResponseDto) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "FeesResponseDto{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", totalFees=" + totalFees +
                ", remainingFees=" + remainingFees +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", academicYear='" + academicYear + '\'' +
                '}';
    }
}