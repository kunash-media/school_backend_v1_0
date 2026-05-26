package com.sc.service;

import com.sc.dto.request.TeachersAttendanceRequestDto;
import com.sc.dto.response.TeachersAttendanceResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TeachersAttendanceService {

    // Single attendance operations
    TeachersAttendanceResponseDto markAttendance(TeachersAttendanceRequestDto requestDto);
    TeachersAttendanceResponseDto getAttendanceById(Long id);
    TeachersAttendanceResponseDto updateAttendance(Long id, TeachersAttendanceRequestDto requestDto);
    void deleteAttendance(Long id);

    // Bulk operations
    Map<String, Object> markBulkAttendance(List<TeachersAttendanceRequestDto> requestDtos);

    // Get all with pagination
    Page<TeachersAttendanceResponseDto> getAllAttendance(Pageable pageable);

    // Get by teacher
    List<TeachersAttendanceResponseDto> getAttendanceByTeacherId(Long teacherId);
    Page<TeachersAttendanceResponseDto> getAttendanceByTeacherId(Long teacherId, Pageable pageable);

    // Get by date
    List<TeachersAttendanceResponseDto> getAttendanceByDate(LocalDate date);
    Page<TeachersAttendanceResponseDto> getAttendanceByDate(LocalDate date, Pageable pageable);

    // Get today's attendance
    List<TeachersAttendanceResponseDto> getTodaysAttendance();
    Page<TeachersAttendanceResponseDto> getTodaysAttendance(Pageable pageable);

    // Get by date range
    List<TeachersAttendanceResponseDto> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate);
    Page<TeachersAttendanceResponseDto> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Get by teacher and date range
    List<TeachersAttendanceResponseDto> getAttendanceByTeacherAndDateRange(
            Long teacherId, LocalDate startDate, LocalDate endDate);
    Page<TeachersAttendanceResponseDto> getAttendanceByTeacherAndDateRange(
            Long teacherId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Statistics and summaries
    Map<String, Object> getAttendanceStatistics(LocalDate date);
    Map<String, Object> getMonthlyAttendanceSummary(Long teacherId, int year, int month);
    Map<String, Object> getDepartmentWiseAttendance(LocalDate date);

    // Calendar data for frontend dashboard view
    Map<String, Object> getTeacherCalendarData(Long teacherId, int year, int month);

    // Filter and search
    List<TeachersAttendanceResponseDto> filterAttendance(
            LocalDate date, String department, String status, String searchTerm);

    // Department list for filter dropdown
    List<String> getAllDepartments();

    // Salary integration
    TeachersAttendanceResponseDto markAsProcessedForSalary(Long attendanceId, Long salaryId);

    // Generate salary slip data
    Map<String, Object> generateSalarySlipData(Long teacherId, int year, int month);

    // ========== CHECK-IN/CHECK-OUT METHODS ==========

    /**
     * Check-in a teacher for a specific date
     * @param teacherId The teacher's ID
     * @param date The attendance date
     * @param teacherCode The teacher's code for verification (optional)
     * @return The updated attendance record
     */
    TeachersAttendanceResponseDto checkIn(Long teacherId, LocalDate date, String teacherCode);

    /**
     * Check-out a teacher for a specific date
     * @param teacherId The teacher's ID
     * @param date The attendance date
     * @param teacherCode The teacher's code for verification (optional)
     * @return The updated attendance record
     */
    TeachersAttendanceResponseDto checkOut(Long teacherId, LocalDate date, String teacherCode);

    /**
     * Get teachers who haven't checked in today
     * @return List of teachers without check-in
     */
    List<Map<String, Object>> getTeachersWithoutCheckIn();

    /**
     * Get teachers who have checked in but not checked out today
     * @return List of attendance records without check-out
     */
    List<TeachersAttendanceResponseDto> getTeachersWithoutCheckOut();

    /**
     * Get late check-ins for a specific date
     * @param date The date to check
     * @param lateThreshold Time threshold for late (e.g., 09:00:00)
     * @return List of late attendance records
     */
    List<TeachersAttendanceResponseDto> getLateCheckIns(LocalDate date, String lateThreshold);

    /**
     * Get attendance statistics with check-in/out details
     * @param date The date to get statistics for
     * @return Map containing check-in/out statistics
     */
    Map<String, Object> getCheckInOutStatistics(LocalDate date);

    /**
     * Get today's check-in/out summary for dashboard
     * @return Map with today's check-in/out data
     */
    Map<String, Object> getTodayCheckInOutSummary();

    /**
     * Check if a teacher has already checked in for a date
     * @param teacherId The teacher's ID
     * @param date The date to check
     * @return true if already checked in
     */
    boolean hasCheckedIn(Long teacherId, LocalDate date);

    /**
     * Check if a teacher has already checked out for a date
     * @param teacherId The teacher's ID
     * @param date The date to check
     * @return true if already checked out
     */
    boolean hasCheckedOut(Long teacherId, LocalDate date);

    /**
     * Get check-in/out history for a teacher in a date range
     * @param teacherId The teacher's ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of attendance records with check-in/out times
     */
    List<TeachersAttendanceResponseDto> getTeacherCheckInOutHistory(
            Long teacherId, LocalDate startDate, LocalDate endDate);

    /**
     * Get monthly check-in/out summary for a teacher
     * @param teacherId The teacher's ID
     * @param year Year
     * @param month Month
     * @return Map with daily check-in/out data
     */
    Map<String, Object> getMonthlyCheckInOutSummary(Long teacherId, int year, int month);
}