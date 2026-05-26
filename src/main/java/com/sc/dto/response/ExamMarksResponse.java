package com.sc.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExamMarksResponse {

    private Long marksRecordId;
    private Long stdId;                     // Student primary key (Long stdId from StudentEntity)
    private String studentId;               // Student custom/admission ID (String studentId from StudentEntity)
    private String studentName;
    private String rollNumber;
    private String className;
    private String section;
    private String examType;
    private String examName;
    private String academicYear;
    private LocalDate assessmentDate;
    private String teacherComments;
    private String enteredBy;
    private Integer totalObtainedMarks;
    private Integer totalMaxMarks;
    private Double percentage;
    private String grade;
    private String result;
    private List<SubjectMarkResponse> subjectMarks = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters & Setters
    public Long getMarksRecordId() {
        return marksRecordId;
    }

    public void setMarksRecordId(Long marksRecordId) {
        this.marksRecordId = marksRecordId;
    }

    public Long getStdId() {
        return stdId;
    }

    public void setStdId(Long stdId) {
        this.stdId = stdId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDate assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public String getTeacherComments() {
        return teacherComments;
    }

    public void setTeacherComments(String teacherComments) {
        this.teacherComments = teacherComments;
    }

    public String getEnteredBy() {
        return enteredBy;
    }

    public void setEnteredBy(String enteredBy) {
        this.enteredBy = enteredBy;
    }

    public Integer getTotalObtainedMarks() {
        return totalObtainedMarks;
    }

    public void setTotalObtainedMarks(Integer totalObtainedMarks) {
        this.totalObtainedMarks = totalObtainedMarks;
    }

    public Integer getTotalMaxMarks() {
        return totalMaxMarks;
    }

    public void setTotalMaxMarks(Integer totalMaxMarks) {
        this.totalMaxMarks = totalMaxMarks;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<SubjectMarkResponse> getSubjectMarks() {
        return subjectMarks;
    }

    public void setSubjectMarks(List<SubjectMarkResponse> subjectMarks) {
        this.subjectMarks = subjectMarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}