package com.sc.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "assignment_notifications")    // Changed from "notifications" to "assignment_notifications"
public class AssignmentNotificationEntity {

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
    private String notificationType;

    @Column(name = "channel", length = 50)
    private String channel;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "sent_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentDate;

    @Column(name = "delivered_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deliveredDate;

    @Column(name = "read_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date readAt;

    // ============= ASSIGNMENT SPECIFIC FIELDS =============
    @Column(name = "assignment_id")
    private Long assignmentId;

    @Column(name = "assignment_title")
    private String assignmentTitle;

    @Column(name = "due_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dueDate;

    @Column(name = "submission_status")
    private String submissionStatus;

    // ============= COMMON NOTIFICATION FIELDS =============
    @Column(name = "scheduled_for")
    @Temporal(TemporalType.TIMESTAMP)
    private Date scheduledFor;

    @Column(name = "action_required")
    private Boolean actionRequired = false;

    @Column(name = "action_completed")
    private Boolean actionCompleted = false;

    @Column(name = "expiry_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;

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

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    // Constructors
    public AssignmentNotificationEntity() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.status = "PENDING";
        this.priority = "MEDIUM";
        this.channel = "IN_APP";
        this.retryCount = 0;
    }

    public AssignmentNotificationEntity(StudentEntity student, String title, String message, String notificationType) {
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

        if (student != null) {
            this.recipientEmail = student.getFatherEmail();
            this.recipientPhone = student.getFatherPhone();
            this.recipientName = student.getFirstName() + " " + student.getLastName();
        }
    }

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

    // Helper methods
    public void markAsSent() {
        this.status = "SENT";
        this.sentDate = new Date();
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

    // ============= GETTERS AND SETTERS =============

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StudentEntity getStudent() { return student; }
    public void setStudent(StudentEntity student) {
        this.student = student;
        if (student != null) {
            this.recipientEmail = student.getFatherEmail();
            this.recipientPhone = student.getFatherPhone();
            this.recipientName = student.getFirstName() + " " + student.getLastName();
        }
    }

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

    public Date getReadAt() { return readAt; }
    public void setReadAt(Date readAt) { this.readAt = readAt; }

    // Assignment specific getters/setters
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public String getSubmissionStatus() { return submissionStatus; }
    public void setSubmissionStatus(String submissionStatus) { this.submissionStatus = submissionStatus; }

    // Common notification fields
    public Date getScheduledFor() { return scheduledFor; }
    public void setScheduledFor(Date scheduledFor) { this.scheduledFor = scheduledFor; }

    public Boolean getActionRequired() { return actionRequired; }
    public void setActionRequired(Boolean actionRequired) { this.actionRequired = actionRequired; }

    public Boolean getActionCompleted() { return actionCompleted; }
    public void setActionCompleted(Boolean actionCompleted) { this.actionCompleted = actionCompleted; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

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

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}