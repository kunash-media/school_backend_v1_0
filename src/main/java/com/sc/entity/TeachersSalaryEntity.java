package com.sc.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Entity
@Table(name = "teachers_salary")
public class TeachersSalaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    /**
     * RELATIONSHIP: Many Salary Records -> One Teacher
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false, insertable = false, updatable = false)
    private TeacherEntity teacher;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    // ==================== FIXED: Remove duplicate fields ====================
    // REMOVED: private String department; // Already in TeacherEntity
    // REMOVED: private BigDecimal allowances; // Renamed to otherAllowances (Double)
    // REMOVED: private BigDecimal pfDeductions; // Use professionalTax
    // REMOVED: private BigDecimal taxDeductions; // Use tdsAmount
    // REMOVED: private BigDecimal totalEarnings; // Duplicate - using Double version
    // REMOVED: private BigDecimal totalDeductions; // Duplicate - using Double version
    // REMOVED: private BigDecimal advanceSalary; // Renamed to advanceDeduction
    // REMOVED: private LocalDate paymentDate; // Use paidDate

    @Column(name = "salary_month", nullable = false)
    private YearMonth salaryMonth;

    // Basic Salary Components
    @Column(name = "basic_salary", nullable = false)
    private Double basicSalary;

    @Column(name = "hra_amount")
    private Double hraAmount = 0.0;

    @Column(name = "other_allowances")
    private Double otherAllowances = 0.0;

    // FIXED: Changed to Double (not BigDecimal)
    @Column(name = "total_earnings")
    private Double totalEarnings = 0.0;

    // Deductions
    @Column(name = "advance_deduction")
    private Double advanceDeduction = 0.0;

    @Column(name = "other_deductions")
    private Double otherDeductions = 0.0;

    @Column(name = "professional_tax")
    private Double professionalTax = 0.0;

    @Column(name = "tds_amount")
    private Double tdsAmount = 0.0;

    // FIXED: Changed to Double (not BigDecimal)
    @Column(name = "total_deductions")
    private Double totalDeductions = 0.0;

    // Attendance Statistics
    @Column(name = "working_days")
    private Integer workingDays = 26;

    @Column(name = "present_days")
    private Integer presentDays = 0;

    @Column(name = "absent_days")
    private Integer absentDays = 0;

    @Column(name = "leave_days")
    private Integer leaveDays = 0;

    @Column(name = "half_days")
    private Integer halfDays = 0;

    @Column(name = "late_days")
    private Integer lateDays = 0;

    @Column(name = "payable_days")
    private Integer payableDays = 0;

    @Column(name = "daily_rate")
    private Double dailyRate = 0.0;

    @Column(name = "attendance_based_salary")
    private Double attendanceBasedSalary = 0.0;

    // Final Salary
    @Column(name = "gross_salary")
    private Double grossSalary = 0.0;

    @Column(name = "net_salary")
    private Double netSalary = 0.0;

    // Status and Metadata
    @Column(name = "salary_status", length = 50)
    private String salaryStatus = "DRAFT"; // DRAFT, PENDING, APPROVED, PAID, CANCELLED

    @Column(name = "generated_by", length = 100)
    private String generatedBy;

    @Column(name = "generated_date")
    private LocalDate generatedDate;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_date")
    private LocalDate approvedDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    /**
     * RELATIONSHIP: One Salary Record -> Many Attendance Records
     */
    @OneToMany(mappedBy = "teachersSalaryEntity", fetch = FetchType.LAZY)
    private List<TeachersAttendanceEntity> attendanceRecords;

    // Constructors
    public TeachersSalaryEntity() {
        this.generatedDate = LocalDate.now();
    }

    // Helper method to sync teacher and teacherId
    public void setTeacher(TeacherEntity teacher) {
        this.teacher = teacher;
        if (teacher != null) {
            this.teacherId = teacher.getId();
        }
    }

    // ==================== HELPER METHODS ====================

    public void calculateSalary() {
        // Calculate daily rate
        this.dailyRate = this.basicSalary / this.workingDays;

        // Calculate attendance-based salary
        this.payableDays = this.presentDays + (this.halfDays / 2);
        this.attendanceBasedSalary = this.dailyRate * this.payableDays;

        // Calculate total earnings
        this.totalEarnings = this.attendanceBasedSalary +
                this.hraAmount +
                this.otherAllowances;

        // Calculate total deductions
        this.totalDeductions = this.advanceDeduction +
                this.otherDeductions +
                this.professionalTax +
                this.tdsAmount;

        // Calculate gross and net salary
        this.grossSalary = this.totalEarnings;
        this.netSalary = this.grossSalary - this.totalDeductions;
    }

    public void updateFromAttendance(List<TeachersAttendanceEntity> attendanceList) {
        this.presentDays = 0;
        this.absentDays = 0;
        this.leaveDays = 0;
        this.halfDays = 0;
        this.lateDays = 0;

        for (TeachersAttendanceEntity attendance : attendanceList) {
            switch (attendance.getStatus()) {
                case "PRESENT":
                    this.presentDays++;
                    break;
                case "ABSENT":
                    this.absentDays++;
                    break;
                case "LEAVE":
                    this.leaveDays++;
                    break;
                case "HALF_DAY":
                    this.halfDays++;
                    break;
                case "LATE":
                    this.lateDays++;
                    break;
            }
        }

        calculateSalary();
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public YearMonth getSalaryMonth() {
        return salaryMonth;
    }

    public void setSalaryMonth(YearMonth salaryMonth) {
        this.salaryMonth = salaryMonth;
    }

    public Double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(Double basicSalary) {
        this.basicSalary = basicSalary;
    }

    public Double getHraAmount() {
        return hraAmount;
    }

    public void setHraAmount(Double hraAmount) {
        this.hraAmount = hraAmount;
    }

    public Double getOtherAllowances() {
        return otherAllowances;
    }

    public void setOtherAllowances(Double otherAllowances) {
        this.otherAllowances = otherAllowances;
    }

    public Double getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(Double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public Double getAdvanceDeduction() {
        return advanceDeduction;
    }

    public void setAdvanceDeduction(Double advanceDeduction) {
        this.advanceDeduction = advanceDeduction;
    }

    public Double getOtherDeductions() {
        return otherDeductions;
    }

    public void setOtherDeductions(Double otherDeductions) {
        this.otherDeductions = otherDeductions;
    }

    public Double getProfessionalTax() {
        return professionalTax;
    }

    public void setProfessionalTax(Double professionalTax) {
        this.professionalTax = professionalTax;
    }

    public Double getTdsAmount() {
        return tdsAmount;
    }

    public void setTdsAmount(Double tdsAmount) {
        this.tdsAmount = tdsAmount;
    }

    public Double getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(Double totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public Integer getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(Integer workingDays) {
        this.workingDays = workingDays;
    }

    public Integer getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(Integer presentDays) {
        this.presentDays = presentDays;
    }

    public Integer getAbsentDays() {
        return absentDays;
    }

    public void setAbsentDays(Integer absentDays) {
        this.absentDays = absentDays;
    }

    public Integer getLeaveDays() {
        return leaveDays;
    }

    public void setLeaveDays(Integer leaveDays) {
        this.leaveDays = leaveDays;
    }

    public Integer getHalfDays() {
        return halfDays;
    }

    public void setHalfDays(Integer halfDays) {
        this.halfDays = halfDays;
    }

    public Integer getLateDays() {
        return lateDays;
    }

    public void setLateDays(Integer lateDays) {
        this.lateDays = lateDays;
    }

    public Integer getPayableDays() {
        return payableDays;
    }

    public void setPayableDays(Integer payableDays) {
        this.payableDays = payableDays;
    }

    public Double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(Double dailyRate) {
        this.dailyRate = dailyRate;
    }

    public Double getAttendanceBasedSalary() {
        return attendanceBasedSalary;
    }

    public void setAttendanceBasedSalary(Double attendanceBasedSalary) {
        this.attendanceBasedSalary = attendanceBasedSalary;
    }

    public Double getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(Double grossSalary) {
        this.grossSalary = grossSalary;
    }

    public Double getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(Double netSalary) {
        this.netSalary = netSalary;
    }

    public String getSalaryStatus() {
        return salaryStatus;
    }

    public void setSalaryStatus(String salaryStatus) {
        this.salaryStatus = salaryStatus;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public LocalDate getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDate generatedDate) {
        this.generatedDate = generatedDate;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDate getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDate approvedDate) {
        this.approvedDate = approvedDate;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<TeachersAttendanceEntity> getAttendanceRecords() {
        return attendanceRecords;
    }

    public void setAttendanceRecords(List<TeachersAttendanceEntity> attendanceRecords) {
        this.attendanceRecords = attendanceRecords;
    }

    public TeacherEntity getTeacher() {
        return teacher;
    }
}