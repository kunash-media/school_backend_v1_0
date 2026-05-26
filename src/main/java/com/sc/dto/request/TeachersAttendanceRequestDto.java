package com.sc.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

public class TeachersAttendanceRequestDto {

    private Long teacherId;
    private LocalDate attendanceDate;
    private String status; // Present, Absent, Late, Leave, Half Day
    private String remarks;

    // Constructors
    public TeachersAttendanceRequestDto() {}

    // Getters and Setters
    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    private LocalTime checkInTime;

    private LocalTime checkOutTime;
}