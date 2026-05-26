package com.sc.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "notice_notifications")
public class NoticeNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private NoticeEntity notice;

    // Set when recipientType = TEACHER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private TeacherEntity teacher;

    // Set when recipientType = STUDENT or PARENT
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_std_id")
    private StudentEntity student;

    // TEACHER | STUDENT | PARENT
    @Column(name = "recipient_type", length = 20, nullable = false)
    private String recipientType;

    @Column(name = "is_read", nullable = false, columnDefinition = "boolean default false")
    private boolean isRead = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "read_at")
    private Date readAt;

    // PENDING | DELIVERED | FAILED
    @Column(name = "delivery_status", length = 20)
    private String deliveryStatus = "PENDING";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sent_at", updatable = false)
    private Date sentAt;

    @PrePersist
    protected void onCreate() {
        sentAt = new Date();
        if (deliveryStatus == null) deliveryStatus = "PENDING";
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = new Date();
        this.deliveryStatus = "DELIVERED";
    }

    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }

    public NoticeEntity getNotice() { return notice; }
    public void setNotice(NoticeEntity notice) { this.notice = notice; }

    public TeacherEntity getTeacher() { return teacher; }
    public void setTeacher(TeacherEntity teacher) { this.teacher = teacher; }

    public StudentEntity getStudent() { return student; }
    public void setStudent(StudentEntity student) { this.student = student; }

    public String getRecipientType() { return recipientType; }
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Date getReadAt() { return readAt; }
    public void setReadAt(Date readAt) { this.readAt = readAt; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public Date getSentAt() { return sentAt; }
    public void setSentAt(Date sentAt) { this.sentAt = sentAt; }
}