package com.sc.entity;

import com.sc.entity.ExamEntity.ExamType; // Import existing ExamType enum
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exam_marks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "exam_type", "academic_year"}))
public class ExamMarksEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long marksId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false)
    private ExamType examType; // Using ExamType enum from ExamEntity

    @Column(name = "exam_name")
    private String examName;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "assessment_date")
    private LocalDate assessmentDate;

    @Column(name = "teacher_comments", length = 1000)
    private String teacherComments;

    @Column(name = "subjects_json", columnDefinition = "TEXT")
    private String subjectsJson;

    @Column(name = "total_marks")
    private Integer totalMarks;

    @Column(name = "total_max_marks")
    private Integer totalMaxMarks;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "grade")
    private String grade;

    @Column(name = "result")
    private String result;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Transient
    private List<SubjectMark> subjects = new ArrayList<>();

    public void setUpdatedBy(String updatedBy) {

    }

    // Inner class for subject marks
    public static class SubjectMark {
        private String subjectName;
        private Integer marksObtained;
        private Integer maxMarks;
        private Integer passingMarks;
        private String grade;
        private Double percentage;
        private String remarks;
        private String performance;
        private String status;

        public SubjectMark() {}

        public SubjectMark(String subjectName, Integer marksObtained, Integer maxMarks) {
            this.subjectName = subjectName;
            this.marksObtained = marksObtained;
            this.maxMarks = maxMarks;
            this.passingMarks = (int)(maxMarks * 0.33);
            this.percentage = (marksObtained * 100.0) / maxMarks;
            this.grade = calculateGrade(this.percentage);
            this.status = this.percentage >= 33 ? "PASS" : "FAIL";
        }

        private String calculateGrade(double percentage) {
            if (percentage >= 90) return "A";
            if (percentage >= 75) return "B";
            if (percentage >= 60) return "C";
            if (percentage >= 40) return "D";
            return "F";
        }

        // Getters and Setters
        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public Integer getMarksObtained() { return marksObtained; }
        public void setMarksObtained(Integer marksObtained) { this.marksObtained = marksObtained; }

        public Integer getMaxMarks() { return maxMarks; }
        public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }

        public Integer getPassingMarks() { return passingMarks; }
        public void setPassingMarks(Integer passingMarks) { this.passingMarks = passingMarks; }

        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }

        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }

        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }

        public String getPerformance() { return performance; }
        public void setPerformance(String performance) { this.performance = performance; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // JSON Helper Methods
    public void setSubjectsFromJson() {
        if (subjectsJson != null && !subjectsJson.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                subjects = mapper.readValue(subjectsJson,
                        mapper.getTypeFactory().constructCollectionType(List.class, SubjectMark.class));
            } catch (Exception e) {
                subjects = new ArrayList<>();
            }
        }
    }

    public void setSubjectsToJson() {
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

    // Getters and Setters
    public Long getMarksId() { return marksId; }
    public void setMarksId(Long marksId) { this.marksId = marksId; }

    public StudentEntity getStudent() { return student; }
    public void setStudent(StudentEntity student) { this.student = student; }

    public ExamType getExamType() { return examType; }
    public void setExamType(ExamType examType) { this.examType = examType; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public LocalDate getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }

    public String getTeacherComments() { return teacherComments; }
    public void setTeacherComments(String teacherComments) { this.teacherComments = teacherComments; }

    public String getSubjectsJson() { return subjectsJson; }
    public void setSubjectsJson(String subjectsJson) {
        this.subjectsJson = subjectsJson;
        setSubjectsFromJson();
    }

    public List<SubjectMark> getSubjects() { return subjects; }
    public void setSubjects(List<SubjectMark> subjects) {
        this.subjects = subjects;
        setSubjectsToJson();
    }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

    public Integer getTotalMaxMarks() { return totalMaxMarks; }
    public void setTotalMaxMarks(Integer totalMaxMarks) { this.totalMaxMarks = totalMaxMarks; }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() {
        return updatedBy;
    }
}