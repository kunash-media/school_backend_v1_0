package com.sc.dto.request;


public class SubjectCreateRequestDTO {

    private String subjectCode;

    private String subjectName;

    private String description;

    private String subjectType;

    private String gradeLevel;

    private Integer maxMarks;

    private Integer passingMarks;

    private Integer creditHours;

    private Integer periodsPerWeek;

    private String colorCode;

    private Integer displayOrder;

    private Long primaryTeacherId;

    private String status;

    // Constructors

    public SubjectCreateRequestDTO() {}

    public SubjectCreateRequestDTO(String subjectCode, String subjectName, String subjectType) {

        this.subjectCode = subjectCode;

        this.subjectName = subjectName;

        this.subjectType = subjectType;

    }

    // Getters and Setters

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

    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }

    public Integer getPassingMarks() { return passingMarks; }

    public void setPassingMarks(Integer passingMarks) { this.passingMarks = passingMarks; }

    public Integer getCreditHours() { return creditHours; }

    public void setCreditHours(Integer creditHours) { this.creditHours = creditHours; }

    public Integer getPeriodsPerWeek() { return periodsPerWeek; }

    public void setPeriodsPerWeek(Integer periodsPerWeek) { this.periodsPerWeek = periodsPerWeek; }

    public String getColorCode() { return colorCode; }

    public void setColorCode(String colorCode) { this.colorCode = colorCode; }

    public Integer getDisplayOrder() { return displayOrder; }

    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Long getPrimaryTeacherId() { return primaryTeacherId; }

    public void setPrimaryTeacherId(Long primaryTeacherId) { this.primaryTeacherId = primaryTeacherId; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

}

