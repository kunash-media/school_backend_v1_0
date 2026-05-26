package com.sc.dto.request;

import com.sc.entity.ExamEntity.ExamType;
import java.time.LocalDate;
import java.util.List;

public class BulkMarksEntryRequest {

    private ExamType examType;
    private String examName;
    private String academicYear;
    private String className;
    private String section;
    private LocalDate assessmentDate;
    private List<StudentMarksDto> students;

    public static class StudentMarksDto {
        private Long studentId;
        private String studentName;
        private String rollNumber;
        private List<SubjectMarksDto> subjects;

        // Getters and Setters
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getRollNumber() { return rollNumber; }
        public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

        public List<SubjectMarksDto> getSubjects() { return subjects; }
        public void setSubjects(List<SubjectMarksDto> subjects) { this.subjects = subjects; }
    }

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
    public ExamType getExamType() { return examType; }
    public void setExamType(ExamType examType) { this.examType = examType; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public LocalDate getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }

    public List<StudentMarksDto> getStudents() { return students; }
    public void setStudents(List<StudentMarksDto> students) { this.students = students; }
}