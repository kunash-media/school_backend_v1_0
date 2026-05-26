package com.sc.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, length = 2000)
    private String message;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType; // FEE_REMINDER, INSTALLMENT_DUE, OVERDUE_REMINDER, PAYMENT_CONFIRMATION, CUSTOM

    @Column(name = "channel", length = 50)
    private String channel; // IN_APP, EMAIL, SMS, PUSH, ALL

    @Column(name = "status", length = 20)
    private String status; // PENDING, SENT, DELIVERED, READ, FAILED

    @Column(name = "priority", length = 20)
    private String priority; // HIGH, MEDIUM, LOW


    @Column(name = "sent_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentDate;

    @Column(name = "delivered_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deliveredDate;

    @Column(name = "due_date")
    @Temporal(TemporalType.DATE)
    private Date dueDate;

    @Column(name = "amount_due")
    private Integer amountDue;

    @Column(name = "installment_id")
    private Long installmentId;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "payment_amount")
    private Integer paymentAmount;

    @Column(name = "payment_mode", length = 50)
    private String paymentMode;

    @Column(name = "late_fee")
    private Integer lateFee;

    @Column(name = "days_overdue")
    private Integer daysOverdue;

    @Column(name = "recipient_email", length = 100)
    private String recipientEmail;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "action_button_text", length = 50)
    private String actionButtonText;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "read_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date readAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
        if (status == null) status = "PENDING";
        if (priority == null) priority = "MEDIUM";
        if (retryCount == null) retryCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    // ============= CONSTRUCTORS =============

    public NotificationEntity() {}

    public NotificationEntity(StudentEntity student, String title, String message, String notificationType) {
        this.student = student;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.status = "PENDING";
        this.priority = "MEDIUM";
        this.channel = "IN_APP";
        this.retryCount = 0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // ============= GETTERS AND SETTERS =============

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StudentEntity getStudent() { return student; }
    public void setStudent(StudentEntity student) { this.student = student; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Date getSentDate() { return sentDate; }
    public void setSentDate(Date sentDate) { this.sentDate = sentDate; }

    public Date getDeliveredDate() { return deliveredDate; }
    public void setDeliveredDate(Date deliveredDate) { this.deliveredDate = deliveredDate; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Integer getAmountDue() { return amountDue; }
    public void setAmountDue(Integer amountDue) { this.amountDue = amountDue; }

    public Long getInstallmentId() { return installmentId; }
    public void setInstallmentId(Long installmentId) { this.installmentId = installmentId; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Integer getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(Integer paymentAmount) { this.paymentAmount = paymentAmount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public Integer getLateFee() { return lateFee; }
    public void setLateFee(Integer lateFee) { this.lateFee = lateFee; }

    public Integer getDaysOverdue() { return daysOverdue; }
    public void setDaysOverdue(Integer daysOverdue) { this.daysOverdue = daysOverdue; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

    public String getActionButtonText() { return actionButtonText; }
    public void setActionButtonText(String actionButtonText) { this.actionButtonText = actionButtonText; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Date getReadAt() { return readAt; }
    public void setReadAt(Date readAt) { this.readAt = readAt; }

    // ============= HELPER METHODS =============

    public void markAsSent() {
        this.status = "SENT";
        this.sentDate = new Date();
        this.updatedAt = new Date();
    }

    public void markAsDelivered() {
        this.status = "DELIVERED";
        this.deliveredDate = new Date();
        this.updatedAt = new Date();
    }

    public void markAsRead() {
        this.status = "READ";
        this.readAt = new Date();
        this.updatedAt = new Date();
    }

    public void markAsFailed(String error) {
        this.status = "FAILED";
        this.errorMessage = error;
        this.retryCount = this.retryCount + 1;
        this.updatedAt = new Date();
    }

    public void incrementRetryCount() {
        this.retryCount = this.retryCount + 1;
    }

    @Override
    public String toString() {
        return "NotificationEntity{" +
                "id=" + id +
                ", studentId=" + (student != null ? student.getStdId() : null) +
                ", title='" + title + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
