package com.sc.dto.request;

import java.util.List;

public class TeacherSubjectAssignmentDTO {
    private String teacherId;
    private String teacherName;
    private List<SubjectDetailDTO> subjects;

    // Constructors
    public TeacherSubjectAssignmentDTO() {}


    public TeacherSubjectAssignmentDTO(String teacherId, String teacherName, List<SubjectDetailDTO> subjects) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.subjects = subjects;
    }

    // Getters and Setters
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }


    public List<SubjectDetailDTO> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SubjectDetailDTO> subjects) {
        this.subjects = subjects;
    }
}