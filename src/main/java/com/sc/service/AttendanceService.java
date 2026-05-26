package com.sc.service;

import com.sc.dto.request.AttendanceRequestDto;
import com.sc.dto.request.BulkAttendanceRequestDto;
import com.sc.dto.request.HolidayRequestDto;
import com.sc.dto.response.AttendancePercentageDto;
import com.sc.dto.response.AttendanceResponseDto;
import com.sc.dto.response.MonthlyAttendanceSummaryDto;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    // Single student attendance
    AttendanceResponseDto markAttendance(AttendanceRequestDto requestDto, String markedBy);

    // Bulk attendance for whole class
    List<AttendanceResponseDto> markBulkAttendance(BulkAttendanceRequestDto requestDto, String markedBy);

    // Get attendance by class, section and date
    List<AttendanceResponseDto> getAttendanceByClassAndDate(String className, String section, LocalDate date);

    // Get student attendance for date range
    List<AttendanceResponseDto> getStudentAttendance(Long studentId, LocalDate startDate, LocalDate endDate);

    // Calculate attendance percentage
    AttendancePercentageDto calculateAttendancePercentage(Long studentId, LocalDate startDate, LocalDate endDate);

    // Get monthly summary with all features (holidays, weekends handled)
    MonthlyAttendanceSummaryDto getMonthlySummary(String className, String section, int year, int month);

    // Holiday management
    void addHoliday(HolidayRequestDto requestDto, String createdBy);
    List<LocalDate> getNonAffectingHolidays(LocalDate startDate, LocalDate endDate);
    boolean isWorkingDay(LocalDate date);

    // Update attendance
    AttendanceResponseDto updateAttendance(Long attendanceId, AttendanceRequestDto requestDto, String updatedBy);
}