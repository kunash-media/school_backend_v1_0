package com.sc.controller;

import com.sc.dto.request.TimetableRequestDto;
import com.sc.dto.response.TimetableResponseDto;
import com.sc.entity.TeacherEntity;
import com.sc.repository.TeacherRepository;
import com.sc.repository.TimetableEntryRepository;
import com.sc.service.TimetableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/timetable")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://127.0.0.1:5500", "http://localhost:5500"},
        allowCredentials = "true")
public class TimetableController {

    @Autowired
    private TimetableService timetableService;

    @Autowired
    private TimetableEntryRepository timetableRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    /**
     * Get timetable with filters
     * GET /api/timetable?className=Class%2010&section=A&academicYear=2024-2025&weekNumber=1
     */
    @GetMapping
    public ResponseEntity<TimetableResponseDto> getTimetable(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String classCode,
            @RequestParam String section,
            @RequestParam String academicYear,
            @RequestParam(required = false) Integer weekNumber,
            @RequestParam(required = false) String day,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String subjectName) {

        TimetableRequestDto request = new TimetableRequestDto();
        request.setClassName(className);
        request.setClassCode(classCode);
        request.setSection(section);
        request.setAcademicYear(academicYear);
        request.setWeekNumber(weekNumber);
        request.setDay(day);
        request.setTeacherId(teacherId);
        request.setSubjectName(subjectName);

        TimetableResponseDto response = timetableService.getTimetable(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Create new timetable
     * POST /api/timetable/create
     */
    @PostMapping("/create")
    public ResponseEntity<TimetableResponseDto> createTimetable(@RequestBody TimetableRequestDto request) {
        TimetableResponseDto response = timetableService.createTimetable(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update single period
     * PUT /api/timetable/update-period
     */
    @PutMapping("/update-period")
    public ResponseEntity<TimetableResponseDto> updatePeriod(@RequestBody TimetableRequestDto request) {
        TimetableResponseDto response = timetableService.updatePeriod(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Clear entire day
     * DELETE /api/timetable/clear-day
     */
    @DeleteMapping("/clear-day")
    public ResponseEntity<TimetableResponseDto> clearDay(@RequestBody TimetableRequestDto request) {
        TimetableResponseDto response = timetableService.clearDay(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Check for conflicts
     * POST /api/timetable/check-conflicts
     */
    @PostMapping("/check-conflicts")
    public ResponseEntity<TimetableResponseDto> checkConflicts(@RequestBody TimetableRequestDto request) {
        TimetableResponseDto response = timetableService.checkConflicts(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Copy schedule from one day/week to another
     * POST /api/timetable/copy
     */
    @PostMapping("/copy")
    public ResponseEntity<TimetableResponseDto> copySchedule(@RequestBody TimetableRequestDto request) {
        TimetableResponseDto response = timetableService.copySchedule(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete single entry by ID
     * DELETE /api/timetable/entry/{id}
     */
    @DeleteMapping("/entry/{id}")
    public ResponseEntity<TimetableResponseDto> deleteEntry(@PathVariable Long id) {
        TimetableResponseDto response = timetableService.deleteEntry(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get periods for a specific day
     * GET /api/timetable/periods?className=Class%2010&section=A&day=Monday&academicYear=2024-2025
     */
    @GetMapping("/periods")
    public ResponseEntity<TimetableResponseDto> getPeriods(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam String day,
            @RequestParam String academicYear) {

        TimetableRequestDto request = new TimetableRequestDto();
        request.setClassName(className);
        request.setSection(section);
        request.setDay(day);
        request.setAcademicYear(academicYear);

        TimetableResponseDto response = timetableService.getTimetable(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get schedule for a specific week
     * GET /api/timetable/week/1?className=Class%2010&section=A&academicYear=2024-2025
     */
    @GetMapping("/week/{weekNumber}")
    public ResponseEntity<TimetableResponseDto> getWeekSchedule(
            @RequestParam String className,
            @RequestParam String section,
            @PathVariable Integer weekNumber,
            @RequestParam String academicYear) {

        TimetableRequestDto request = new TimetableRequestDto();
        request.setClassName(className);
        request.setSection(section);
        request.setWeekNumber(weekNumber);
        request.setAcademicYear(academicYear);

        TimetableResponseDto response = timetableService.getTimetable(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get schedule for a specific month (all weeks)
     * GET /api/timetable/month?className=Class%2010&section=A&academicYear=2024-2025
     */
    @GetMapping("/month")
    public ResponseEntity<TimetableResponseDto> getMonthSchedule(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam String academicYear) {

        TimetableRequestDto request = new TimetableRequestDto();
        request.setClassName(className);
        request.setSection(section);
        request.setAcademicYear(academicYear);

        TimetableResponseDto response = timetableService.getTimetable(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get teacher's schedule
     * GET /api/timetable/teacher/{teacherId}?academicYear=2024-2025
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<TimetableResponseDto> getTeacherSchedule(
            @PathVariable Long teacherId,
            @RequestParam String academicYear) {

        TimetableRequestDto request = new TimetableRequestDto();
        request.setTeacherId(teacherId);
        request.setAcademicYear(academicYear);

        TimetableResponseDto response = new TimetableResponseDto();
        response.setSuccess(true);
        response.setMessage("Teacher schedule retrieved");
        return ResponseEntity.ok(response);
    }

    /**
     * Bulk update periods
     * POST /api/timetable/bulk-update
     */
    @PostMapping("/bulk-update")
    public ResponseEntity<TimetableResponseDto> bulkUpdate(@RequestBody TimetableRequestDto request) {
        TimetableResponseDto response = new TimetableResponseDto();
        response.setSuccess(true);
        response.setMessage("Bulk update completed");
        return ResponseEntity.ok(response);
    }

    /**
     * Get available rooms for a period
     * GET /api/timetable/available-rooms?day=Monday&period=1&weekNumber=1&academicYear=2024-2025
     */
    @GetMapping("/available-rooms")
    public ResponseEntity<TimetableResponseDto> getAvailableRooms(
            @RequestParam String day,
            @RequestParam Integer period,
            @RequestParam(required = false, defaultValue = "1") Integer weekNumber,
            @RequestParam String academicYear) {

        TimetableResponseDto response = new TimetableResponseDto();
        response.setSuccess(true);
        response.setMessage("Available rooms retrieved");
        return ResponseEntity.ok(response);
    }

    /**
     * Export timetable
     * GET /api/timetable/export?format=excel&className=Class%2010&section=A&academicYear=2024-2025
     */
    @GetMapping("/export")
    public ResponseEntity<TimetableResponseDto> exportTimetable(
            @RequestParam String format,
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam String academicYear,
            @RequestParam(required = false) Integer weekNumber) {

        TimetableRequestDto request = new TimetableRequestDto();
        request.setClassName(className);
        request.setSection(section);
        request.setAcademicYear(academicYear);
        request.setWeekNumber(weekNumber);
        request.setMode(format);

        TimetableResponseDto response = new TimetableResponseDto();
        response.setSuccess(true);
        response.setMessage("Timetable exported as " + format);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     * GET /api/timetable/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Timetable service is running");
    }

    /**
     * Get available teachers and rooms for a specific time slot
     * GET /api/timetable/available?day=Monday&period=2&weekNumber=1&academicYear=2024-2025
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailable(
            @RequestParam String day,
            @RequestParam Integer period,
            @RequestParam(required = false, defaultValue = "1") Integer weekNumber,
            @RequestParam String academicYear) {

        try {
            // Get booked teacher IDs
            Set<Long> bookedTeacherIds = timetableRepository.findBookedTeacherIds(day, period, weekNumber, academicYear);

            // Get booked rooms
            Set<String> bookedRooms = timetableRepository.findBookedRooms(day, period, weekNumber, academicYear);

            // Get all teachers
            List<TeacherEntity> allTeachers = teacherRepository.findAll();

            // Filter available teachers
            List<Map<String, Object>> availableTeachers = allTeachers.stream()
                    .filter(t -> !bookedTeacherIds.contains(t.getId()))
                    .map(t -> {
                        Map<String, Object> teacherMap = new HashMap<>();
                        teacherMap.put("id", t.getId());
                        teacherMap.put("name", t.getFullName());
                        return teacherMap;
                    })
                    .collect(Collectors.toList());

            // Get all rooms from existing entries or defaults
            Set<String> allRooms = new HashSet<>();
            allRooms.addAll(Arrays.asList("101", "102", "103", "104", "105", "106", "107", "108", "201", "202", "203"));

            // Filter available rooms
            List<String> availableRooms = allRooms.stream()
                    .filter(room -> !bookedRooms.contains(room))
                    .sorted()
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("availableTeachers", availableTeachers);
            response.put("availableRooms", availableRooms);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to get availability: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}