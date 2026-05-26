package com.sc.dto.request;

public class GradeRequestDto {

    private Long assignmentId;
    private Long studentId;
    private Float obtainedMarks;
    private String grade;
    private String teacherFeedback;
    private String gradingMethod;
    private Boolean publishToStudent = true;

    // Constructors
    public GradeRequestDto() {}

    public GradeRequestDto(Long assignmentId, Long studentId, Float obtainedMarks, String grade,
                           String teacherFeedback, String gradingMethod, Boolean publishToStudent) {
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.obtainedMarks = obtainedMarks;
        this.grade = grade;
        this.teacherFeedback = teacherFeedback;
        this.gradingMethod = gradingMethod;
        this.publishToStudent = publishToStudent;
    }

    // Getters and Setters
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Float getObtainedMarks() { return obtainedMarks; }
    public void setObtainedMarks(Float obtainedMarks) { this.obtainedMarks = obtainedMarks; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getTeacherFeedback() { return teacherFeedback; }
    public void setTeacherFeedback(String teacherFeedback) { this.teacherFeedback = teacherFeedback; }

    public String getGradingMethod() { return gradingMethod; }
    public void setGradingMethod(String gradingMethod) { this.gradingMethod = gradingMethod; }

    public Boolean getPublishToStudent() { return publishToStudent; }
    public void setPublishToStudent(Boolean publishToStudent) { this.publishToStudent = publishToStudent; }
}