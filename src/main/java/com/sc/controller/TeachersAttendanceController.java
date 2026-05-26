package com.sc.controller;

import com.sc.dto.request.TeachersAttendanceRequestDto;
import com.sc.dto.response.TeachersAttendanceResponseDto;
import com.sc.service.TeachersAttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Add this import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/teachers-attendance")
public class TeachersAttendanceController {

    // Add this line to initialize the logger
    private static final Logger log = LoggerFactory.getLogger(TeachersAttendanceController.class);

    @Autowired
    private TeachersAttendanceService attendanceService;

    // POST: Mark new attendance
    @PostMapping("/mark-attendance")
    public ResponseEntity<TeachersAttendanceResponseDto> markAttendance(
            @RequestBody TeachersAttendanceRequestDto requestDto) {
        TeachersAttendanceResponseDto response = attendanceService.markAttendance(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // POST: Mark bulk attendance
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> markBulkAttendance(
            @RequestBody List<TeachersAttendanceRequestDto> requestDtos) {
        Map<String, Object> responses = attendanceService.markBulkAttendance(requestDtos);
        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }

    // GET: Get attendance by ID
    @GetMapping("/{id}")
    public ResponseEntity<TeachersAttendanceResponseDto> getAttendanceById(@PathVariable Long id) {
        TeachersAttendanceResponseDto response = attendanceService.getAttendanceById(id);
        return ResponseEntity.ok(response);
    }

    // PATCH: Update attendance
    @PatchMapping("/{id}")
    public ResponseEntity<TeachersAttendanceResponseDto> updateAttendance(
            @PathVariable Long id,
            @RequestBody TeachersAttendanceRequestDto requestDto) {
        TeachersAttendanceResponseDto response = attendanceService.updateAttendance(id, requestDto);
        return ResponseEntity.ok(response);
    }

    // DELETE: Delete attendance
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }

    // GET: Get all attendance with pagination
    @GetMapping
    public ResponseEntity<Page<TeachersAttendanceResponseDto>> getAllAttendance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "attendanceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TeachersAttendanceResponseDto> responses = attendanceService.getAllAttendance(pageable);
        return ResponseEntity.ok(responses);
    }

    // GET: Get attendance by teacher ID
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeachersAttendanceResponseDto>> getAttendanceByTeacherId(
            @PathVariable Long teacherId) {
        List<TeachersAttendanceResponseDto> responses = attendanceService.getAttendanceByTeacherId(teacherId);
        return ResponseEntity.ok(responses);
    }

    // GET: Get attendance by teacher ID with pagination
    @GetMapping("/teacher/{teacherId}/paged")
    public ResponseEntity<Page<TeachersAttendanceResponseDto>> getAttendanceByTeacherIdPaged(
            @PathVariable Long teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("attendanceDate").descending());
        Page<TeachersAttendanceResponseDto> responses = attendanceService.getAttendanceByTeacherId(teacherId, pageable);
        return ResponseEntity.ok(responses);
    }

    // GET: Get attendance by date
    @GetMapping("/date/{date}")
    public ResponseEntity<List<TeachersAttendanceResponseDto>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TeachersAttendanceResponseDto> responses = attendanceService.getAttendanceByDate(date);
        return ResponseEntity.ok(responses);
    }

    // GET: Get today's attendance
    @GetMapping("/today")
    public ResponseEntity<List<TeachersAttendanceResponseDto>> getTodaysAttendance() {
        List<TeachersAttendanceResponseDto> responses = attendanceService.getTodaysAttendance();
        return ResponseEntity.ok(responses);
    }

    // GET: Get today's attendance with pagination
    @GetMapping("/today/paged")
    public ResponseEntity<Page<TeachersAttendanceResponseDto>> getTodaysAttendancePaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TeachersAttendanceResponseDto> responses = attendanceService.getTodaysAttendance(pageable);
        return ResponseEntity.ok(responses);
    }

    // GET: Get attendance by teacher and date range
    @GetMapping("/teacher/{teacherId}/date-range")
    public ResponseEntity<List<TeachersAttendanceResponseDto>> getAttendanceByTeacherAndDateRange(
            @PathVariable Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TeachersAttendanceResponseDto> responses = attendanceService
                .getAttendanceByTeacherAndDateRange(teacherId, startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    // GET: Get attendance by date range
    @GetMapping("/date-range")
    public ResponseEntity<List<TeachersAttendanceResponseDto>> getAttendanceByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TeachersAttendanceResponseDto> responses = attendanceService
                .getAttendanceByDateRange(startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    // GET: Get attendance statistics for a date
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAttendanceStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> statistics = attendanceService.getAttendanceStatistics(date);
        return ResponseEntity.ok(statistics);
    }

    // GET: Get monthly attendance summary for a teacher
    @GetMapping("/teacher/{teacherId}/monthly-summary")
    public ResponseEntity<Map<String, Object>> getMonthlyAttendanceSummary(
            @PathVariable Long teacherId,
            @RequestParam int year,
            @RequestParam int month) {
        Map<String, Object> summary = attendanceService.getMonthlyAttendanceSummary(teacherId, year, month);
        return ResponseEntity.ok(summary);
    }

    // GET: Get calendar data for teacher dashboard
    @GetMapping("/teacher/{teacherId}/calendar")
    public ResponseEntity<Map<String, Object>> getTeacherCalendarData(
            @PathVariable Long teacherId,
            @RequestParam int year,
            @RequestParam int month) {
        Map<String, Object> calendarData = attendanceService.getTeacherCalendarData(teacherId, year, month);
        return ResponseEntity.ok(calendarData);
    }

    // GET: Get department wise attendance
    @GetMapping("/department-wise")
    public ResponseEntity<Map<String, Object>> getDepartmentWiseAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        Map<String, Object> deptStats = attendanceService.getDepartmentWiseAttendance(date);
        return ResponseEntity.ok(deptStats);
    }

    // GET: Get all departments for filter dropdown
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getAllDepartments() {
        List<String> departments = attendanceService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    // GET: Filter attendance
    @GetMapping("/filter")
    public ResponseEntity<List<TeachersAttendanceResponseDto>> filterAttendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        List<TeachersAttendanceResponseDto> responses = attendanceService
                .filterAttendance(date, department, status, search);
        return ResponseEntity.ok(responses);
    }

    // POST: Mark attendance as processed for salary
    @PostMapping("/{attendanceId}/process-salary/{salaryId}")
    public ResponseEntity<TeachersAttendanceResponseDto> markAsProcessedForSalary(
            @PathVariable Long attendanceId,
            @PathVariable Long salaryId) {
        TeachersAttendanceResponseDto response = attendanceService
                .markAsProcessedForSalary(attendanceId, salaryId);
        return ResponseEntity.ok(response);
    }

    // GET: Generate salary slip data
    @GetMapping("/teacher/{teacherId}/salary-slip")
    public ResponseEntity<Map<String, Object>> generateSalarySlipData(
            @PathVariable Long teacherId,
            @RequestParam int year,
            @RequestParam int month) {
        Map<String, Object> salaryData = attendanceService.generateSalarySlipData(teacherId, year, month);
        return ResponseEntity.ok(salaryData);
    }

    // ========== CHECK-IN/CHECK-OUT ENDPOINTS ==========

    /**
     * POST: Check-in a teacher
     * URL: /api/teachers-attendance/check-in?teacherId=1&date=2024-03-09&teacherCode=TCH001
     */
    @PostMapping("/check-in")
    public ResponseEntity<TeachersAttendanceResponseDto> checkIn(
            @RequestParam Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String teacherCode) {

        log.info("Check-in request - Teacher ID: {}, Date: {}, Teacher Code: {}",
                teacherId, date, teacherCode);

        TeachersAttendanceResponseDto response = attendanceService.checkIn(teacherId, date, teacherCode);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * POST: Check-out a teacher
     * URL: /api/teachers-attendance/check-out?teacherId=1&date=2024-03-09&teacherCode=TCH001
     */
    @PostMapping("/check-out")
    public ResponseEntity<TeachersAttendanceResponseDto> checkOut(
            @RequestParam Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String teacherCode) {

        log.info("Check-out request - Teacher ID: {}, Date: {}, Teacher Code: {}",
                teacherId, date, teacherCode);

        TeachersAttendanceResponseDto response = attendanceService.checkOut(teacherId, date, teacherCode);
        return ResponseEntity.ok(response);
    }

    /**
     * GET: Check if teacher has checked in for a date
     * URL: /api/teachers-attendance/has-checked-in?teacherId=1&date=2024-03-09
     */
    @GetMapping("/has-checked-in")
    public ResponseEntity<Map<String, Boolean>> hasCheckedIn(
            @RequestParam Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean hasCheckedIn = attendanceService.hasCheckedIn(teacherId, date);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasCheckedIn", hasCheckedIn);
        return ResponseEntity.ok(response);
    }

    /**
     * GET: Check if teacher has checked out for a date
     * URL: /api/teachers-attendance/has-checked-out?teacherId=1&date=2024-03-09
     */
    @GetMapping("/has-checked-out")
    public ResponseEntity<Map<String, Boolean>> hasCheckedOut(
            @RequestParam Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean hasCheckedOut = attendanceService.hasCheckedOut(teacherId, date);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasCheckedOut", hasCheckedOut);
        return ResponseEntity.ok(response);
    }

    /**
     * GET: Get teachers without check-in for today
     * URL: /api/teachers-attendance/without-checkin
     */
    @GetMapping("/without-checkin")
    public ResponseEntity<List<Map<String, Object>>> getTeachersWithoutCheckIn() {
        List<Map<String, Object>> teachers = attendanceService.getTeachersWithoutCheckIn();
        return ResponseEntity.ok(teachers);
    }

    /**
     * GET: Get teachers without check-out for today
     * URL: /api/teachers-attendance/without-checkout
     */
    @GetMapping("/without-checkout")
    public ResponseEntity<List<TeachersAttendanceResponseDto>> getTeachersWithoutCheckOut() {
        List<TeachersAttendanceResponseDto> teachers = attendanceService.getTeachersWithoutCheckOut();
        return ResponseEntity.ok(teachers);
    }

    /**
     * GET: Get late check-ins for a date
     * URL: /api/teachers-attendance/late-checkins?date=2024-03-09&threshold=09:00:00
     */
    @GetMapping("/late-checkins")
    public ResponseEntity<List<TeachersAttendanceResponseDto>> getLateCheckIns(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String threshold) {
        List<TeachersAttendanceResponseDto> lateCheckIns = attendanceService.getLateCheckIns(date, threshold);
        return ResponseEntity.ok(lateCheckIns);
    }

    /**
     * GET: Get check-in/out statistics for a date
     * URL: /api/teachers-attendance/checkinout-stats?date=2024-03-09
     */
    @GetMapping("/checkinout-stats")
    public ResponseEntity<Map<String, Object>> getCheckInOutStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        Map<String, Object> stats = attendanceService.getCheckInOutStatistics(date);
        return ResponseEntity.ok(stats);
    }

    /**
     * GET: Get today's check-in/out summary
     * URL: /api/teachers-attendance/today-summary
     */
    @GetMapping("/today-summary")
    public ResponseEntity<Map<String, Object>> getTodayCheckInOutSummary() {
        Map<String, Object> summary = attendanceService.getTodayCheckInOutSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * GET: Get teacher check-in/out history
     * URL: /api/teachers-attendance/teacher/{teacherId}/checkinout-history?startDate=2024-03-01&endDate=2024-03-31
     */
    @GetMapping("/teacher/{teacherId}/checkinout-history")
    public ResponseEntity<List<TeachersAttendanceResponseDto>> getTeacherCheckInOutHistory(
            @PathVariable Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TeachersAttendanceResponseDto> history = attendanceService
                .getTeacherCheckInOutHistory(teacherId, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    /**
     * GET: Get monthly check-in/out summary
     * URL: /api/teachers-attendance/teacher/{teacherId}/monthly-checkinout?year=2024&month=3
     */
    @GetMapping("/teacher/{teacherId}/monthly-checkinout")
    public ResponseEntity<Map<String, Object>> getMonthlyCheckInOutSummary(
            @PathVariable Long teacherId,
            @RequestParam int year,
            @RequestParam int month) {
        Map<String, Object> summary = attendanceService.getMonthlyCheckInOutSummary(teacherId, year, month);
        return ResponseEntity.ok(summary);
    }
}