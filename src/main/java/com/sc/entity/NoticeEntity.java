package com.sc.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "notices")
public class NoticeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 50)
    private String category = "general";

    @Column(name = "priority", length = 20)
    private String priority = "medium";

    @Column(name = "status", length = 20)
    private String status = "draft";

    // Comma-separated: all-users,teachers,students,parents,staff,management,class
    @Column(name = "audience", length = 200)
    private String audience;

    // Only when audience contains "class"
    @Column(name = "target_class", length = 20)
    private String targetClass;

    // "ALL" or "A,B" — only when audience contains "class"
    @Column(name = "target_sections", length = 200)
    private String targetSections;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "publish_date")
    private Date publishDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiry_date")
    private Date expiryDate;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    // Notice → Teacher recipients (ManyToMany)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "notice_teacher_recipients",
            joinColumns        = @JoinColumn(name = "notice_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    private List<TeacherEntity> teacherRecipients = new ArrayList<>();

    // Notice → Student recipients (ManyToMany)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "notice_student_recipients",
            joinColumns        = @JoinColumn(name = "notice_id"),
            inverseJoinColumns = @JoinColumn(name = "student_std_id")
    )
    private List<StudentEntity> studentRecipients = new ArrayList<>();

    // Notice → Attachments (OneToMany)
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NoticeAttachment> attachments = new ArrayList<>();

    // Notice → Notifications (OneToMany)
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NoticeNotification> notifications = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
        if (status   == null) status   = "draft";
        if (category == null) category = "general";
        if (priority == null) priority = "medium";
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = new Date(); }

    // Helpers
    public void addTeacherRecipient(TeacherEntity t) { teacherRecipients.add(t); }
    public void addStudentRecipient(StudentEntity s) { studentRecipients.add(s); }
    public void addNotification(NoticeNotification n) { notifications.add(n); n.setNotice(this); }

    // Getters & Setters
    public Long getNoticeId() { return noticeId; }
    public void setNoticeId(Long noticeId) { this.noticeId = noticeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }

    public String getTargetClass() { return targetClass; }
    public void setTargetClass(String targetClass) { this.targetClass = targetClass; }

    public String getTargetSections() { return targetSections; }
    public void setTargetSections(String targetSections) { this.targetSections = targetSections; }

    public Date getPublishDate() { return publishDate; }
    public void setPublishDate(Date publishDate) { this.publishDate = publishDate; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public List<TeacherEntity> getTeacherRecipients() { return teacherRecipients; }
    public void setTeacherRecipients(List<TeacherEntity> t) { this.teacherRecipients = t; }

    public List<StudentEntity> getStudentRecipients() { return studentRecipients; }
    public void setStudentRecipients(List<StudentEntity> s) { this.studentRecipients = s; }

    public List<NoticeAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<NoticeAttachment> a) { this.attachments = a; }

    public List<NoticeNotification> getNotifications() { return notifications; }
    public void setNotifications(List<NoticeNotification> n) { this.notifications = n; }
}