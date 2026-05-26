package com.sc.dto.request;

import com.sc.dto.request.TeacherSubjectAssignmentDTO;

import java.util.List;

public class ClassCreateRequestDTO {
    private String className;
    private String classCode;
    private String academicYear;
    private String section;
    private Integer maxStudents;
    private Integer currentStudents;
    private String roomNumber;
    private String startTime;
    private String endTime;
    private String description;
    private Long classTeacherId;
    private String classTeacherSubject;
    private Long assistantTeacherId;
    private String assistantTeacherSubject;
    private List<String> workingDays;
    private String status;
    private List<TeacherSubjectAssignmentDTO> otherTeacherSubject;

    // Constructors
    public ClassCreateRequestDTO() {}

    // Getters and Setters
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Integer getMaxStudents() { return maxStudents; }
    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }

    public Integer getCurrentStudents() { return currentStudents; }
    public void setCurrentStudents(Integer currentStudents) { this.currentStudents = currentStudents; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getClassTeacherId() { return classTeacherId; }
    public void setClassTeacherId(Long classTeacherId) { this.classTeacherId = classTeacherId; }

    public String getClassTeacherSubject() { return classTeacherSubject; }
    public void setClassTeacherSubject(String classTeacherSubject) { this.classTeacherSubject = classTeacherSubject; }

    public Long getAssistantTeacherId() { return assistantTeacherId; }
    public void setAssistantTeacherId(Long assistantTeacherId) { this.assistantTeacherId = assistantTeacherId; }

    public String getAssistantTeacherSubject() { return assistantTeacherSubject; }
    public void setAssistantTeacherSubject(String assistantTeacherSubject) { this.assistantTeacherSubject = assistantTeacherSubject; }

    public List<String> getWorkingDays() { return workingDays; }
    public void setWorkingDays(List<String> workingDays) { this.workingDays = workingDays; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<TeacherSubjectAssignmentDTO> getOtherTeacherSubject() { return otherTeacherSubject; }
    public void setOtherTeacherSubject(List<TeacherSubjectAssignmentDTO> otherTeacherSubject) { this.otherTeacherSubject = otherTeacherSubject; }
}