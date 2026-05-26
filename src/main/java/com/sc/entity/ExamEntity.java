package com.sc.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exams_table")
public class ExamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examId;

    @Column(name = "exam_name", nullable = false)
    private String examName;

    @Column(name = "exam_code", unique = true)
    private String examCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false)
    private ExamType examType;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @Column(name = "section", nullable = false)
    private String section;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "status")
    private String status = "SCHEDULED";

    @Column(name = "subjects_json", columnDefinition = "TEXT")
    private String subjectsJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    // Transient field for class name (for easy access in frontend)
    @Transient
    private String className;

    @Transient
    private List<SubjectDetail> subjects = new ArrayList<>();

    public enum ExamType {
        TERM1, TERM2, TERM3, UNIT_TEST, MID_TERM, FINAL
    }

    public static class SubjectDetail {
        private String subjectName;
        private Integer maxMarks;
        private Integer passingMarks;
        private LocalDate examDate;
        private String startTime;
        private String endTime;
        private String roomNumber;
        private String invigilator;

        public SubjectDetail() {}

        public SubjectDetail(String subjectName, Integer maxMarks) {
            this.subjectName = subjectName;
            this.maxMarks = maxMarks;
            this.passingMarks = 33;
        }

        // Getters and Setters
        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public Integer getMaxMarks() { return maxMarks; }
        public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }

        public Integer getPassingMarks() { return passingMarks; }
        public void setPassingMarks(Integer passingMarks) { this.passingMarks = passingMarks; }

        public LocalDate getExamDate() { return examDate; }
        public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }

        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }

        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

        public String getInvigilator() { return invigilator; }
        public void setInvigilator(String invigilator) { this.invigilator = invigilator; }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        saveSubjectsToJson();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        saveSubjectsToJson();
    }

    @PostLoad
    private void onLoad() {
        loadSubjectsFromJson();
        // Set class name from class entity for easy access in frontend
        if (classEntity != null) {
            this.className = classEntity.getClassName();
        }
    }

    private void loadSubjectsFromJson() {
        if (subjectsJson != null && !subjectsJson.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                subjects = mapper.readValue(subjectsJson,
                        mapper.getTypeFactory().constructCollectionType(List.class, SubjectDetail.class));
            } catch (Exception e) {
                subjects = new ArrayList<>();
            }
        } else {
            subjects = new ArrayList<>();
        }
    }

    private void saveSubjectsToJson() {
        if (subjects != null && !subjects.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                subjectsJson = mapper.writeValueAsString(subjects);
            } catch (Exception e) {
                subjectsJson = "[]";
            }
        } else {
            subjectsJson = "[]";
        }
    }

    public void addSubject(SubjectDetail subject) {
        if (this.subjects == null) {
            this.subjects = new ArrayList<>();
        }
        this.subjects.add(subject);
        saveSubjectsToJson();
    }

    // Helper method to get class ID directly
    public Long getClassId() {
        return classEntity != null ? classEntity.getClassId() : null;
    }

    // Helper method to get class code directly
    public String getClassCode() {
        return classEntity != null ? classEntity.getClassCode() : null;
    }

    // Getters and Setters
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }

    public ExamType getExamType() { return examType; }
    public void setExamType(ExamType examType) { this.examType = examType; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public ClassEntity getClassEntity() { return classEntity; }
    public void setClassEntity(ClassEntity classEntity) {
        this.classEntity = classEntity;
        // Update className when classEntity is set
        if (classEntity != null) {
            this.className = classEntity.getClassName();
        }
    }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubjectsJson() { return subjectsJson; }
    public void setSubjectsJson(String subjectsJson) {
        this.subjectsJson = subjectsJson;
        loadSubjectsFromJson();
    }

    public List<SubjectDetail> getSubjects() { return subjects; }
    public void setSubjects(List<SubjectDetail> subjects) {
        this.subjects = subjects;
        saveSubjectsToJson();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Getter and Setter for className transient field
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}