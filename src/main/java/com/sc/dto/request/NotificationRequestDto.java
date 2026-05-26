package com.sc.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;

public class NotificationRequestDto {

    // ============= REQUIRED FIELDS =============
    private Long studentId;              // Single student ID
    private List<Long> studentIds;       // Multiple student IDs for bulk notifications
    private String title;               // Notification title
    private String message;             // Notification message
    private String notificationType;    // FEE_REMINDER, INSTALLMENT_DUE, OVERDUE_REMINDER, PAYMENT_CONFIRMATION, CUSTOM

    // ============= OPTIONAL FIELDS =============
    private String channel = "IN_APP";   // IN_APP, EMAIL, SMS, PUSH, ALL
    private String priority = "MEDIUM";  // HIGH, MEDIUM, LOW
    private String status = "PENDING";   // PENDING, SENT, DRAFT

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;               // Due date for fee/installment

    private Integer amountDue;          // Amount due
    private Long installmentId;         // Specific installment ID
    private String academicYear;        // Academic year

    // ============= PAYMENT CONFIRMATION FIELDS =============
    private Integer paymentAmount;      // Amount paid
    private String transactionId;       // Transaction ID
    private String paymentMode;         // CASH, ONLINE, CHEQUE, CARD

    // ============= OVERDUE FIELDS =============
    private Integer lateFee;            // Late fee amount
    private Integer daysOverdue;        // Number of days overdue

    // ============= RECIPIENT FIELDS =============
    private String recipientEmail;      // Override recipient email
    private String recipientPhone;      // Override recipient phone
    private String recipientName;       // Override recipient name

    // ============= CUSTOM NOTIFICATION FIELDS =============
    private String actionUrl;           // URL for action button
    private String actionButtonText;    // Text for action button
    private String createdBy;           // Who created this notification

    // ============= SCHEDULING FIELDS =============
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date scheduledTime;         // Schedule notification for later

    private boolean sendEmail = true;   // Whether to send email
    private boolean sendSms = false;    // Whether to send SMS
    private boolean sendInApp = true;   // Whether to send in-app notification

    // ============= CONSTRUCTORS =============

    public NotificationRequestDto() {}

    // Constructor for single student fee reminder
    public NotificationRequestDto(Long studentId, String title, String message, String notificationType) {
        this.studentId = studentId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.channel = "IN_APP";
        this.priority = "MEDIUM";
        this.sendEmail = true;
        this.sendInApp = true;
    }

    // Constructor for bulk notifications
    public NotificationRequestDto(List<Long> studentIds, String title, String message, String notificationType) {
        this.studentIds = studentIds;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.channel = "IN_APP";
        this.priority = "MEDIUM";
        this.sendEmail = true;
        this.sendInApp = true;
    }

    // ============= GETTERS AND SETTERS =============

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public List<Long> getStudentIds() { return studentIds; }
    public void setStudentIds(List<Long> studentIds) { this.studentIds = studentIds; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Integer getAmountDue() { return amountDue; }
    public void setAmountDue(Integer amountDue) { this.amountDue = amountDue; }

    public Long getInstallmentId() { return installmentId; }
    public void setInstallmentId(Long installmentId) { this.installmentId = installmentId; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(Integer paymentAmount) { this.paymentAmount = paymentAmount; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

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

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

    public String getActionButtonText() { return actionButtonText; }
    public void setActionButtonText(String actionButtonText) { this.actionButtonText = actionButtonText; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Date getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(Date scheduledTime) { this.scheduledTime = scheduledTime; }

    public boolean isSendEmail() { return sendEmail; }
    public void setSendEmail(boolean sendEmail) { this.sendEmail = sendEmail; }

    public boolean isSendSms() { return sendSms; }
    public void setSendSms(boolean sendSms) { this.sendSms = sendSms; }

    public boolean isSendInApp() { return sendInApp; }
    public void setSendInApp(boolean sendInApp) { this.sendInApp = sendInApp; }

    // ============= HELPER METHODS =============

    /**
     * Check if this is a single student notification
     */
    public boolean isSingleStudent() {
        return studentId != null && (studentIds == null || studentIds.isEmpty());
    }

    /**
     * Check if this is a bulk notification
     */
    public boolean isBulkNotification() {
        return studentIds != null && !studentIds.isEmpty();
    }

    /**
     * Get all student IDs (combines single and bulk)
     */
    public List<Long> getAllStudentIds() {
        if (isBulkNotification()) {
            return studentIds;
        } else if (isSingleStudent()) {
            return List.of(studentId);
        }
        return List.of();
    }

    @Override
    public String toString() {
        return "NotificationRequestDto{" +
                "studentId=" + studentId +
                ", title='" + title + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", channel='" + channel + '\'' +
                ", priority='" + priority + '\'' +
                ", academicYear='" + academicYear + '\'' +
                '}';
    }
}