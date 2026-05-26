package com.sc.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "exam_subject_marks")
public class ExamSubjectMarkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subjectMarkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_marks_id", nullable = false)
    private ExamMarksEntity examMarks;

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Column(name = "max_marks")
    private Integer maxMarks = 100;

    @Column(name = "obtained_marks")
    private Integer obtainedMarks;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "grade")
    private String grade;

    @Column(name = "status")
    private String status;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "performance_level")
    private String performanceLevel;

    @PrePersist
    @PreUpdate
    protected void calculateFields() {
        if (obtainedMarks != null && maxMarks != null && maxMarks > 0) {
            percentage = (obtainedMarks.doubleValue() * 100.0) / maxMarks;
            grade = calculateGrade(percentage);
            status = (percentage >= 33.0) ? "PASS" : "FAIL";
        } else {
            percentage = 0.0;
            grade = null;
            status = null;
        }
    }

    private String calculateGrade(double perc) {
        if (perc >= 90.0) return "A";
        if (perc >= 75.0) return "B";
        if (perc >= 60.0) return "C";
        if (perc >= 40.0) return "D";
        return "F";
    }

    public Long getSubjectMarkId() { return subjectMarkId; }
    public void setSubjectMarkId(Long subjectMarkId) { this.subjectMarkId = subjectMarkId; }

    public ExamMarksEntity getExamMarks() { return examMarks; }
    public void setExamMarks(ExamMarksEntity examMarks) { this.examMarks = examMarks; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }

    public Integer getObtainedMarks() { return obtainedMarks; }
    public void setObtainedMarks(Integer obtainedMarks) { this.obtainedMarks = obtainedMarks; }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getPerformanceLevel() { return performanceLevel; }
    public void setPerformanceLevel(String performanceLevel) { this.performanceLevel = performanceLevel; }
}