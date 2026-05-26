package com.sc.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class AttendanceRequestDto {

    private Long studentId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String status; // PRESENT, ABSENT, LEAVE

    private String leaveType; // SICK, CASUAL, etc. (for LEAVE status)

    private String reason;

    // Getters and Setters
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}