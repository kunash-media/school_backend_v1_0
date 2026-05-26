package com.sc.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "subjects")
public class SubjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "subject_code", unique = true, nullable = false)
    private String subjectCode;

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "subject_type")
    private String subjectType;

    @Column(name = "grade_level")
    private String gradeLevel;

    @Column(name = "max_marks")
    private Integer maxMarks = 100;

    @Column(name = "passing_marks")
    private Integer passingMarks = 35;

    @Column(name = "credit_hours")
    private Integer creditHours;

    @Column(name = "periods_per_week")
    private Integer periodsPerWeek = 5;

    @Column(name = "color_code")
    private String colorCode = "#3B82F6";

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "primary_teacher_id")
    private Long primaryTeacherId;

    @Column(name = "status")
    private String status = "ACTIVE";

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    // Constructors
    public SubjectEntity() {}

    public SubjectEntity(String subjectCode, String subjectName, String subjectType) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.subjectType = subjectType;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
        if (status == null) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    // Getters and Setters
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSubjectType() { return subjectType; }
    public void setSubjectType(String subjectType) { this.subjectType = subjectType; }

    public String getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }

    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks != null ? maxMarks : 100; }

    public Integer getPassingMarks() { return passingMarks; }
    public void setPassingMarks(Integer passingMarks) { this.passingMarks = passingMarks != null ? passingMarks : 35; }

    public Integer getCreditHours() { return creditHours; }
    public void setCreditHours(Integer creditHours) { this.creditHours = creditHours; }

    public Integer getPeriodsPerWeek() { return periodsPerWeek; }
    public void setPeriodsPerWeek(Integer periodsPerWeek) { this.periodsPerWeek = periodsPerWeek != null ? periodsPerWeek : 5; }

    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode != null ? colorCode : "#3B82F6"; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Long getPrimaryTeacherId() { return primaryTeacherId; }
    public void setPrimaryTeacherId(Long primaryTeacherId) { this.primaryTeacherId = primaryTeacherId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status != null ? status : "ACTIVE"; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
}