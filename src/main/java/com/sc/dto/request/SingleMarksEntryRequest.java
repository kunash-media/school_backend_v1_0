package com.sc.dto.request;

import com.sc.entity.ExamEntity;

import java.time.LocalDate;
import java.util.List;

public class SingleMarksEntryRequest {

    private Long studentId;
    private ExamEntity.ExamType examType;
    private String examName;
    private String academicYear;
    private LocalDate assessmentDate;
    private String teacherComments;
    private List<SubjectMarksDto> subjects;

    public static class SubjectMarksDto {
        private String subjectName;
        private Integer marksObtained;
        private Integer maxMarks;
        private String remarks;
        private String performance;

        // Getters and Setters
        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public Integer getMarksObtained() { return marksObtained; }
        public void setMarksObtained(Integer marksObtained) { this.marksObtained = marksObtained; }

        public Integer getMaxMarks() { return maxMarks; }
        public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }

        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }

        public String getPerformance() { return performance; }
        public void setPerformance(String performance) { this.performance = performance; }
    }

    // Getters and Setters
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public ExamEntity.ExamType getExamType() { return examType; }
    public void setExamType(ExamEntity.ExamType examType) { this.examType = examType; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public LocalDate getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }

    public String getTeacherComments() { return teacherComments; }
    public void setTeacherComments(String teacherComments) { this.teacherComments = teacherComments; }

    public List<SubjectMarksDto> getSubjects() { return subjects; }
    public void setSubjects(List<SubjectMarksDto> subjects) { this.subjects = subjects; }
}