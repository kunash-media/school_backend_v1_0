package com.sc.dto.response;

import com.sc.entity.ExamEntity;
import com.sc.entity.ExamMarksEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class StudentMarksResponse {

    private Long marksId;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String className;
    private String section;
    private ExamEntity.ExamType examType;
    private String examName;
    private String academicYear;
    private LocalDate assessmentDate;
    private String teacherComments;
    private List<SubjectMarkDto> subjects;
    private Integer totalMarks;
    private Integer totalMaxMarks;
    private Double percentage;
    private String grade;
    private String result;

    public StudentMarksResponse(ExamMarksEntity entity) {
        this.marksId = entity.getMarksId();
        this.studentId = entity.getStudent().getStdId();
        this.studentName = entity.getStudent().getFirstName() + " " + entity.getStudent().getLastName();
        this.rollNumber = entity.getStudent().getStudentRollNumber();
        this.className = entity.getStudent().getCurrentClass();
        this.section = entity.getStudent().getSection();
        this.examType = entity.getExamType();
        this.examName = entity.getExamName();
        this.academicYear = entity.getAcademicYear();
        this.assessmentDate = entity.getAssessmentDate();
        this.teacherComments = entity.getTeacherComments();
        this.subjects = entity.getSubjects().stream()
                .map(SubjectMarkDto::new)
                .collect(Collectors.toList());
        this.totalMarks = entity.getTotalMarks();
        this.totalMaxMarks = entity.getTotalMaxMarks();
        this.percentage = entity.getPercentage();
        this.grade = entity.getGrade();
        this.result = entity.getResult();
    }

    public static class SubjectMarkDto {
        private String subjectName;
        private Integer marksObtained;
        private Integer maxMarks;
        private Integer passingMarks;
        private String grade;
        private Double percentage;
        private String remarks;
        private String performance;
        private String status;

        public SubjectMarkDto(ExamMarksEntity.SubjectMark subject) {
            this.subjectName = subject.getSubjectName();
            this.marksObtained = subject.getMarksObtained();
            this.maxMarks = subject.getMaxMarks();
            this.passingMarks = subject.getPassingMarks();
            this.grade = subject.getGrade();
            this.percentage = subject.getPercentage();
            this.remarks = subject.getRemarks();
            this.performance = subject.getPerformance();
            this.status = subject.getStatus();
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

    // Getters and Setters
    public Long getMarksId() { return marksId; }
    public void setMarksId(Long marksId) { this.marksId = marksId; }

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

    public List<SubjectMarkDto> getSubjects() { return subjects; }
    public void setSubjects(List<SubjectMarkDto> subjects) { this.subjects = subjects; }

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