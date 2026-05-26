package com.sc.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

public class BulkAttendanceRequestDto {

    private String className;
    private String section;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private List<StudentAttendanceDto> attendanceList;

    // Getters and Setters
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<StudentAttendanceDto> getAttendanceList() { return attendanceList; }
    public void setAttendanceList(List<StudentAttendanceDto> attendanceList) { this.attendanceList = attendanceList; }

    public static class StudentAttendanceDto {
        private Long studentId;
        private String status;
        private String leaveType;
        private String reason;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getLeaveType() { return leaveType; }
        public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}