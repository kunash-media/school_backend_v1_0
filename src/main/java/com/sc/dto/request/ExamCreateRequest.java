package com.sc.dto.request;

import com.sc.entity.ExamEntity;
import java.time.LocalDate;
import java.util.List;

public class ExamCreateRequest {

    private String examName;
    private String examCode;
    private ExamEntity.ExamType examType;
    private String academicYear;
    private Long classId;
    private String section;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private List<ExamEntity.SubjectDetail> subjects;

    // Getters and Setters
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }

    public ExamEntity.ExamType getExamType() { return examType; }
    public void setExamType(ExamEntity.ExamType examType) { this.examType = examType; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ExamEntity.SubjectDetail> getSubjects() { return subjects; }
    public void setSubjects(List<ExamEntity.SubjectDetail> subjects) { this.subjects = subjects; }
}