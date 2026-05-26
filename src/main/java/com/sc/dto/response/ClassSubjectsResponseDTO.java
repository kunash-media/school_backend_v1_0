package com.sc.dto.response;

import java.util.ArrayList;
import java.util.List;

public class ClassSubjectsResponseDTO {
    private Long classId;
    private String className;
    private String classCode;
    private String section;
    private String academicYear;
    private List<String> subjects;

    public ClassSubjectsResponseDTO() {
        this.subjects = new ArrayList<>();
    }

    public ClassSubjectsResponseDTO(Long classId, String className, String classCode,
                                    String section, String academicYear) {
        this.classId = classId;
        this.className = className;
        this.classCode = classCode;
        this.section = section;
        this.academicYear = academicYear;
        this.subjects = new ArrayList<>();
    }

    // Getters and Setters
    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public void addSubject(String subject) {
        this.subjects.add(subject);
    }
}