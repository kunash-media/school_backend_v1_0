package com.sc.dto.response;

import com.sc.entity.ExamEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ExamResponse {

    private Long examId;
    private String examName;
    private String examCode;
    private String examType;
    private String academicYear;
    private Long classId;
    private String className;
    private String classCode;
    private String section;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String status;
    private List<ExamEntity.SubjectDetail> subjects;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ExamResponse(ExamEntity exam) {
        this.examId = exam.getExamId();
        this.examName = exam.getExamName();
        this.examCode = exam.getExamCode();
        this.examType = exam.getExamType() != null ? exam.getExamType().name() : null;
        this.academicYear = exam.getAcademicYear();
        this.classId = exam.getClassEntity() != null ? exam.getClassEntity().getClassId() : null;
        this.className = exam.getClassEntity() != null ? exam.getClassEntity().getClassName() : null;
        this.classCode = exam.getClassEntity() != null ? exam.getClassEntity().getClassCode() : null;
        this.section = exam.getSection();
        this.startDate = exam.getStartDate();
        this.endDate = exam.getEndDate();
        this.description = exam.getDescription();
        this.status = exam.getStatus();
        this.subjects = exam.getSubjects();
        this.createdAt = exam.getCreatedAt();
        this.updatedAt = exam.getUpdatedAt();
    }

    // Getters and Setters
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

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

    public List<ExamEntity.SubjectDetail> getSubjects() { return subjects; }
    public void setSubjects(List<ExamEntity.SubjectDetail> subjects) { this.subjects = subjects; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}