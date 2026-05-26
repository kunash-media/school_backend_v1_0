package com.sc.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AssignmentRequestDto {

    private String title;
    private String subject;

    private Long classId;
    private String className;
    private String section;
    private String description;
    private String gradingType;
    private Integer totalMarks;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime scheduledPublishDate;

    private Boolean allowLateSubmission = false;
    private Boolean allowResubmission = false;
    private String priority;
    private String assignTo;

    private String publishStatus;

    private Boolean publishNow = false;

    private List<String> assignedClasses = new ArrayList<>();
    private List<Long> assignedStudents = new ArrayList<>();
    private List<String> attachments = new ArrayList<>();
    private String externalLink;

    private Boolean notifyStudents = true;
    private Boolean notifyParents = true;
    private Boolean sendReminders = true;
    private Boolean sendLateWarnings = false;

    private String status = "active";
    private Long createdByTeacherId;
    private String academicYear;
    private String term;

    private transient List<MultipartFile> attachmentFiles;

    // Custom deserializer for flexible date format
    public static class FlexibleLocalDateTimeDeserializer extends LocalDateTimeDeserializer {
        private static final DateTimeFormatter FORMATTER_WITH_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        private static final DateTimeFormatter FORMATTER_WITHOUT_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        public FlexibleLocalDateTimeDeserializer() {
            super(FORMATTER_WITH_SECONDS);
        }

        @Override
        public LocalDateTime deserialize(com.fasterxml.jackson.core.JsonParser parser,
                                         com.fasterxml.jackson.databind.DeserializationContext context) throws java.io.IOException {
            String dateString = parser.getValueAsString();
            if (dateString == null || dateString.trim().isEmpty()) {
                return null;
            }

            try {
                // Try with seconds first
                return LocalDateTime.parse(dateString, FORMATTER_WITH_SECONDS);
            } catch (DateTimeParseException e1) {
                try {
                    // Try without seconds
                    return LocalDateTime.parse(dateString, FORMATTER_WITHOUT_SECONDS);
                } catch (DateTimeParseException e2) {
                    // If both fail, try default format
                    return LocalDateTime.parse(dateString);
                }
            }
        }
    }

    // Getters and Setters for new fields
    public String getPublishStatus() { return publishStatus; }
    public void setPublishStatus(String publishStatus) { this.publishStatus = publishStatus; }

    public LocalDateTime getScheduledPublishDate() { return scheduledPublishDate; }
    public void setScheduledPublishDate(LocalDateTime scheduledPublishDate) { this.scheduledPublishDate = scheduledPublishDate; }

    public Boolean getPublishNow() { return publishNow; }
    public void setPublishNow(Boolean publishNow) { this.publishNow = publishNow; }

    // Existing getters and setters
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

    public String getGradingType() { return gradingType; }
    public void setGradingType(String gradingType) { this.gradingType = gradingType; }

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

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getAssignTo() { return assignTo; }
    public void setAssignTo(String assignTo) { this.assignTo = assignTo; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCreatedByTeacherId() { return createdByTeacherId; }
    public void setCreatedByTeacherId(Long createdByTeacherId) { this.createdByTeacherId = createdByTeacherId; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public List<MultipartFile> getAttachmentFiles() { return attachmentFiles; }
    public void setAttachmentFiles(List<MultipartFile> attachmentFiles) { this.attachmentFiles = attachmentFiles; }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }
}