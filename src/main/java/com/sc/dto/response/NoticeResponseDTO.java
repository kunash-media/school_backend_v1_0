package com.sc.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeResponseDTO {

    private Long   id;
    private String title;
    private String description;
    private String category;
    private String priority;
    private String status;
    private String audience;
    private String targetClass;
    private String targetSections;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date publishDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date expiryDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date updatedAt;

    private String createdBy;

    private int totalTeacherRecipients;
    private int totalStudentRecipients;
    private int totalNotificationsSent;
    private int totalNotificationsRead;
    private int totalNotificationsPending;

    // attachments[].name and .size match frontend {name, size} format
    private List<AttachmentDTO> attachments;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AttachmentDTO {
        private Long   id;
        private String name;         // originalName mapped as "name"
        private String size;         // human-readable e.g. "1.2 MB"
        private String fileType;
        private String downloadUrl;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }

        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public int getTotalTeacherRecipients() { return totalTeacherRecipients; }
    public void setTotalTeacherRecipients(int v) { this.totalTeacherRecipients = v; }

    public int getTotalStudentRecipients() { return totalStudentRecipients; }
    public void setTotalStudentRecipients(int v) { this.totalStudentRecipients = v; }

    public int getTotalNotificationsSent() { return totalNotificationsSent; }
    public void setTotalNotificationsSent(int v) { this.totalNotificationsSent = v; }

    public int getTotalNotificationsRead() { return totalNotificationsRead; }
    public void setTotalNotificationsRead(int v) { this.totalNotificationsRead = v; }

    public int getTotalNotificationsPending() { return totalNotificationsPending; }
    public void setTotalNotificationsPending(int v) { this.totalNotificationsPending = v; }

    public List<AttachmentDTO> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentDTO> attachments) { this.attachments = attachments; }
}