package com.sc.controller;

import com.sc.dto.request.AttendanceRequestDto;
import com.sc.dto.request.BulkAttendanceRequestDto;
import com.sc.dto.request.HolidayRequestDto;
import com.sc.dto.response.AttendancePercentageDto;
import com.sc.dto.response.AttendanceResponseDto;
import com.sc.dto.response.MonthlyAttendanceSummaryDto;
import com.sc.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
//@CrossOrigin(origins = "*")
public class AttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    @Autowired
    private AttendanceService attendanceService;

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }

    // ============= MARK ATTENDANCE =============

    @PostMapping("/mark")
    public ResponseEntity<?> markAttendance(@RequestBody AttendanceRequestDto requestDto) {
        try {
            AttendanceResponseDto response = attendanceService.markAttendance(requestDto, getCurrentUser());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error marking attendance: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bulk-mark")
    public ResponseEntity<?> markBulkAttendance(@RequestBody BulkAttendanceRequestDto requestDto) {
        try {
            List<AttendanceResponseDto> responses = attendanceService.markBulkAttendance(requestDto, getCurrentUser());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Error marking bulk attendance: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============= GET ATTENDANCE =============

    @GetMapping("/class/{className}/section/{section}/date/{date}")
    public ResponseEntity<?> getAttendanceByClassAndDate(
            @PathVariable String className,
            @PathVariable String section,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<AttendanceResponseDto> responses = attendanceService.getAttendanceByClassAndDate(className, section, date);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Error getting attendance: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentAttendance(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<AttendanceResponseDto> responses = attendanceService.getStudentAttendance(studentId, startDate, endDate);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Error getting student attendance: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============= ATTENDANCE PERCENTAGE =============

    @GetMapping("/percentage/{studentId}")
    public ResponseEntity<?> getAttendancePercentage(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            AttendancePercentageDto response = attendanceService.calculateAttendancePercentage(studentId, startDate, endDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error calculating attendance %: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============= MONTHLY SUMMARY =============

    @GetMapping("/summary/monthly")
    public ResponseEntity<?> getMonthlySummary(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam int year,
            @RequestParam int month) {
        try {
            MonthlyAttendanceSummaryDto summary = attendanceService.getMonthlySummary(className, section, year, month);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error getting monthly summary: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============= HOLIDAY MANAGEMENT =============

    @PostMapping("/holiday/add")
    public ResponseEntity<?> addHoliday(@RequestBody HolidayRequestDto requestDto) {
        try {
            attendanceService.addHoliday(requestDto, getCurrentUser());
            return ResponseEntity.ok(Map.of("message", "Holiday added successfully"));
        } catch (Exception e) {
            logger.error("Error adding holiday: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/holiday/non-affecting")
    public ResponseEntity<?> getNonAffectingHolidays(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<LocalDate> holidays = attendanceService.getNonAffectingHolidays(startDate, endDate);
            return ResponseEntity.ok(holidays);
        } catch (Exception e) {
            logger.error("Error getting holidays: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/check-working-day/{date}")
    public ResponseEntity<?> checkWorkingDay(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            boolean isWorkingDay = attendanceService.isWorkingDay(date);
            return ResponseEntity.ok(Map.of("date", date, "isWorkingDay", isWorkingDay));
        } catch (Exception e) {
            logger.error("Error checking working day: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============= UPDATE ATTENDANCE =============

    @PutMapping("/{attendanceId}")
    public ResponseEntity<?> updateAttendance(
            @PathVariable Long attendanceId,
            @RequestBody AttendanceRequestDto requestDto) {
        try {
            AttendanceResponseDto response = attendanceService.updateAttendance(attendanceId, requestDto, getCurrentUser());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating attendance: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}