package com.sc.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sc.enum_util.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AssignmentResponseDto {

    private Long assignmentId;
    private String assignmentCode;
    private String title;
    private String subject;
    private String className;
    private String section;
    private String description;
    private GradingType gradingType;
    private Integer totalMarks;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueDate;

    private Boolean allowLateSubmission;
    private Boolean allowResubmission;
    private PriorityType priority;
    private AssignToType assignTo;

    // ============= NEW PUBLISH FIELDS =============
    private PublishStatus publishStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledPublishDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishedDate;

    private String publishedBy;
    private Boolean isVisible; // For student view
    // ==============================================

    private List<String> assignedClasses = new ArrayList<>();
    private List<Long> assignedStudents = new ArrayList<>();
    private List<String> attachments = new ArrayList<>();
    private String externalLink;
    private Boolean notifyStudents;
    private Boolean notifyParents;
    private Boolean sendReminders;
    private Boolean sendLateWarnings;
    private StatusType status;

    private Long createdByTeacherId;
    private String createdByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String academicYear;
    private String term;

    // Statistics fields
    private Integer totalStudents;
    private Integer submittedCount;
    private Integer pendingCount;
    private Integer lateCount;
    private Integer gradedCount;
    private Double submissionRate;
    private Double averageScore;

    private List<String> attachmentUrls = new ArrayList<>();

    // Getters and Setters for new fields
    public PublishStatus getPublishStatus() { return publishStatus; }
    public void setPublishStatus(PublishStatus publishStatus) { this.publishStatus = publishStatus; }

    public LocalDateTime getScheduledPublishDate() { return scheduledPublishDate; }
    public void setScheduledPublishDate(LocalDateTime scheduledPublishDate) { this.scheduledPublishDate = scheduledPublishDate; }

    public LocalDateTime getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDateTime publishedDate) { this.publishedDate = publishedDate; }

    public String getPublishedBy() { return publishedBy; }
    public void setPublishedBy(String publishedBy) { this.publishedBy = publishedBy; }

    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }

    // Existing getters and setters
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public String getAssignmentCode() { return assignmentCode; }
    public void setAssignmentCode(String assignmentCode) { this.assignmentCode = assignmentCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

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

    public Long getCreatedByTeacherId() { return createdByTeacherId; }
    public void setCreatedByTeacherId(Long createdByTeacherId) { this.createdByTeacherId = createdByTeacherId; }

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

    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }

    public Integer getSubmittedCount() { return submittedCount; }
    public void setSubmittedCount(Integer submittedCount) { this.submittedCount = submittedCount; }

    public Integer getPendingCount() { return pendingCount; }
    public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }

    public Integer getLateCount() { return lateCount; }
    public void setLateCount(Integer lateCount) { this.lateCount = lateCount; }

    public Integer getGradedCount() { return gradedCount; }
    public void setGradedCount(Integer gradedCount) { this.gradedCount = gradedCount; }

    public Double getSubmissionRate() { return submissionRate; }
    public void setSubmissionRate(Double submissionRate) { this.submissionRate = submissionRate; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

    public List<String> getAttachmentUrls() { return attachmentUrls; }
    public void setAttachmentUrls(List<String> attachmentUrls) { this.attachmentUrls = attachmentUrls; }
}