package com.sc.entity;

import com.sc.enum_util.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignments")
public class AssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @Column(unique = true, nullable = false)
    private String assignmentCode;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String subject;

    // 🔴 FIXED: Relationship with ClassEntity - NOW REQUIRED
    @ManyToOne(fetch = FetchType.LAZY, optional = false)  // optional=false means REQUIRED
    @JoinColumn(name = "class_id", nullable = false)      // nullable=false in database
    private ClassEntity targetClass;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(nullable = false)
    private String section;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "grading_type", nullable = false)
    private GradingType gradingType;

    @Column(name = "total_marks")
    private Integer totalMarks;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "allow_late_submission")
    private Boolean allowLateSubmission = false;

    @Column(name = "allow_resubmission")
    private Boolean allowResubmission = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriorityType priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "assign_to", nullable = false)
    private AssignToType assignTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "publish_status", nullable = false)
    private PublishStatus publishStatus = PublishStatus.DRAFT;

    @Column(name = "scheduled_publish_date")
    private LocalDateTime scheduledPublishDate;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Column(name = "published_by")
    private String publishedBy;

    @ElementCollection
    @CollectionTable(name = "assignment_assigned_classes",
            joinColumns = @JoinColumn(name = "assignment_id"))
    @Column(name = "class_name")
    private List<String> assignedClasses = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "assignment_assigned_students",
            joinColumns = @JoinColumn(name = "assignment_id"))
    @Column(name = "student_std_id")
    private List<Long> assignedStudents = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "assignment_attachments",
            joinColumns = @JoinColumn(name = "assignment_id"))
    @Column(name = "file_name")
    private List<String> attachments = new ArrayList<>();

    @Column(name = "external_link")
    private String externalLink;

    @Column(name = "notify_students")
    private Boolean notifyStudents = true;

    @Column(name = "notify_parents")
    private Boolean notifyParents = true;

    @Column(name = "send_reminders")
    private Boolean sendReminders = true;

    @Column(name = "send_late_warnings")
    private Boolean sendLateWarnings = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType status = StatusType.active;

    // 🔴 FIXED: Teacher relationship - NOW REQUIRED
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_teacher_id", nullable = false)
    private TeacherEntity createdByTeacher;

    @Column(name = "created_by_name")
    private String createdByName;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "academic_year")
    private String academicYear;

    private String term;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SubmissionEntity> submissions = new ArrayList<>();

    // Constructors
    public AssignmentEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.publishStatus = PublishStatus.DRAFT;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method to set target class with validation
    public void setTargetClass(ClassEntity targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class cannot be null");
        }
        this.targetClass = targetClass;
        this.className = targetClass.getClassName();
        this.academicYear = targetClass.getAcademicYear();
    }

    // Helper method to set teacher with validation
    public void setCreatedByTeacher(TeacherEntity teacher) {
        if (teacher == null) {
            throw new IllegalArgumentException("Teacher cannot be null");
        }
        this.createdByTeacher = teacher;
        this.createdByName = teacher.getFullName();
    }

    public void publish(String publishedBy) {
        this.publishStatus = PublishStatus.PUBLISHED;
        this.publishedDate = LocalDateTime.now();
        this.publishedBy = publishedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isVisibleToStudents() {
        if (this.publishStatus == PublishStatus.PUBLISHED) {
            return true;
        }
        if (this.publishStatus == PublishStatus.SCHEDULED &&
                this.scheduledPublishDate != null &&
                this.scheduledPublishDate.isBefore(LocalDateTime.now())) {
            return true;
        }
        return false;
    }

    // Getters and Setters
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public String getAssignmentCode() { return assignmentCode; }
    public void setAssignmentCode(String assignmentCode) { this.assignmentCode = assignmentCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public ClassEntity getTargetClass() { return targetClass; }
    // Setter is above

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public GradingType getGradingType() { return gradingType; }
    public void setGradingType(GradingType gradingType) { this.gradingType = gradingType; }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Boolean getAllowLateSubmission() { return allowLateSubmission; }
    public void setAllowLateSubmission(Boolean allowLateSubmission) { this.allowLateSubmission = allowLateSubmission; }

    public Boolean getAllowResubmission() { return allowResubmission; }
    public void setAllowResubmission(Boolean allowResubmission) { this.allowResubmission = allowResubmission; }

    public PriorityType getPriority() { return priority; }
    public void setPriority(PriorityType priority) { this.priority = priority; }

    public AssignToType getAssignTo() { return assignTo; }
    public void setAssignTo(AssignToType assignTo) { this.assignTo = assignTo; }

    public PublishStatus getPublishStatus() { return publishStatus; }
    public void setPublishStatus(PublishStatus publishStatus) { this.publishStatus = publishStatus; }

    public LocalDateTime getScheduledPublishDate() { return scheduledPublishDate; }
    public void setScheduledPublishDate(LocalDateTime scheduledPublishDate) { this.scheduledPublishDate = scheduledPublishDate; }

    public LocalDateTime getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDateTime publishedDate) { this.publishedDate = publishedDate; }

    public String getPublishedBy() { return publishedBy; }
    public void setPublishedBy(String publishedBy) { this.publishedBy = publishedBy; }

    public List<String> getAssignedClasses() { return assignedClasses; }
    public void setAssignedClasses(List<String> assignedClasses) { this.assignedClasses = assignedClasses; }

    public List<Long> getAssignedStudents() { return assignedStudents; }
    public void setAssignedStudents(List<Long> assignedStudents) { this.assignedStudents = assignedStudents; }

    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }

    public String getExternalLink() { return externalLink; }
    public void setExternalLink(String externalLink) { this.externalLink = externalLink; }

    public Boolean getNotifyStudents() { return notifyStudents; }
    public void setNotifyStudents(Boolean notifyStudents) { this.notifyStudents = notifyStudents; }

    public Boolean getNotifyParents() { return notifyParents; }
    public void setNotifyParents(Boolean notifyParents) { this.notifyParents = notifyParents; }

    public Boolean getSendReminders() { return sendReminders; }
    public void setSendReminders(Boolean sendReminders) { this.sendReminders = sendReminders; }

    public Boolean getSendLateWarnings() { return sendLateWarnings; }
    public void setSendLateWarnings(Boolean sendLateWarnings) { this.sendLateWarnings = sendLateWarnings; }

    public StatusType getStatus() { return status; }
    public void setStatus(StatusType status) { this.status = status; }

    public TeacherEntity getCreatedByTeacher() { return createdByTeacher; }
    // Setter is above

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public List<SubmissionEntity> getSubmissions() { return submissions; }
    public void setSubmissions(List<SubmissionEntity> submissions) { this.submissions = submissions; }
}