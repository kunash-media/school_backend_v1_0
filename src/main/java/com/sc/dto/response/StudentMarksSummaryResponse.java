package com.sc.dto.response;

import java.util.List;

public class StudentMarksSummaryResponse {

    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String className;
    private String section;
    private String academicYear;
    private List<ExamSummaryDto> exams;
    private OverallSummaryDto overall;


    public static class ExamSummaryDto {
        private String examType;
        private String examName;
        private Integer totalMarks;
        private Integer totalMaxMarks;
        private Double percentage;
        private String grade;
        private String result;

        // Getters and Setters
        public String getExamType() { return examType; }
        public void setExamType(String examType) { this.examType = examType; }

        public String getExamName() { return examName; }
        public void setExamName(String examName) { this.examName = examName; }

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
    }

    public static class OverallSummaryDto {
        private Integer totalExams;
        private Integer totalMarks;
        private Integer totalMaxMarks;
        private Double overallPercentage;
        private String overallGrade;
        private Integer rank;

        // Getters and Setters
        public Integer getTotalExams() { return totalExams; }
        public void setTotalExams(Integer totalExams) { this.totalExams = totalExams; }

        public Integer getTotalMarks() { return totalMarks; }
        public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

        public Integer getTotalMaxMarks() { return totalMaxMarks; }
        public void setTotalMaxMarks(Integer totalMaxMarks) { this.totalMaxMarks = totalMaxMarks; }

        public Double getOverallPercentage() { return overallPercentage; }
        public void setOverallPercentage(Double overallPercentage) { this.overallPercentage = overallPercentage; }

        public String getOverallGrade() { return overallGrade; }
        public void setOverallGrade(String overallGrade) { this.overallGrade = overallGrade; }

        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }
    }

    // Getters and Setters
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public List<ExamSummaryDto> getExams() { return exams; }
    public void setExams(List<ExamSummaryDto> exams) { this.exams = exams; }

    public OverallSummaryDto getOverall() { return overall; }
    public void setOverall(OverallSummaryDto overall) { this.overall = overall; }
}