package com.sc.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class NoticeRequestDTO {

    private String id;           // null on create, set on update

    private String title;        // required

    private String description;  // required

    // general|academic|event|holiday|exam|emergency
    private String category = "general";

    // low|medium|high
    private String priority = "medium";

    // draft|active|expired
    private String status = "draft";

    // Comma-separated: all-users|teachers|students|parents|staff|management|class
    private String audience;

    // Only when audience contains "class"  e.g. "9"
    private String targetClass;

    // Only when audience contains "class"  e.g. "A,B" or "ALL"
    private String targetSections;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date publishDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date expiryDate;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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
}