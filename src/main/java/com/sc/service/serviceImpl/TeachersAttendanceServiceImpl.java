package com.sc.service.serviceImpl;

import com.sc.CustomExceptions.ResourceNotFoundException;
import com.sc.CustomExceptions.ValidationException;
import com.sc.dto.request.TeachersAttendanceRequestDto;
import com.sc.dto.response.TeachersAttendanceResponseDto;
import com.sc.entity.TeacherEntity;
import com.sc.entity.TeachersAttendanceEntity;
import com.sc.entity.TeachersSalaryEntity;
import com.sc.repository.TeacherRepository;
import com.sc.repository.TeachersAttendanceRepository;
import com.sc.repository.TeachersSalaryRepository;
import com.sc.service.TeachersAttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class TeachersAttendanceServiceImpl implements TeachersAttendanceService {

    private static final Logger log = LoggerFactory.getLogger(TeachersAttendanceServiceImpl.class);

    @Autowired
    private TeachersAttendanceRepository attendanceRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TeachersSalaryRepository salaryRepository;

    // Valid statuses - matching frontend
    private static final Set<String> VALID_STATUSES = Set.of(
            "Present", "Absent", "Late", "Leave", "Half Day"
    );

    private static final int MAX_PAST_DAYS = 30;
    private static final LocalTime DEFAULT_LATE_THRESHOLD = LocalTime.of(8, 30);

    // ========== MARK ATTENDANCE (SINGLE) ==========
    @Override
    public TeachersAttendanceResponseDto markAttendance(TeachersAttendanceRequestDto requestDto) {
        log.info("Marking attendance for teacher ID: {} on date: {}", requestDto.getTeacherId(), requestDto.getAttendanceDate());

        try {
            TeacherEntity teacher = teacherRepository.findById(requestDto.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found with ID: " + requestDto.getTeacherId()));

            validateStatus(requestDto.getStatus());
            validateAttendanceDate(requestDto.getAttendanceDate());

            Optional<TeachersAttendanceEntity> existingAttendance = attendanceRepository
                    .findByTeacherIdAndAttendanceDate(requestDto.getTeacherId(), requestDto.getAttendanceDate());

            TeachersAttendanceEntity entity;

            if (existingAttendance.isPresent()) {
                entity = existingAttendance.get();
                log.info("Updating existing attendance with ID: {}", entity.getId());
                entity.setStatus(requestDto.getStatus());
                entity.setRemarks(requestDto.getRemarks());
                entity.setUpdatedAt(LocalDateTime.now());
            } else {
                entity = new TeachersAttendanceEntity();
                log.info("Creating new attendance record");
                entity.setTeacher(teacher);
                entity.setAttendanceDate(requestDto.getAttendanceDate());
                entity.setStatus(requestDto.getStatus());
                entity.setRemarks(requestDto.getRemarks());
                entity.setTeacherCode(teacher.getTeacherCode());
                entity.setCreatedAt(LocalDateTime.now());
                entity.setUpdatedAt(LocalDateTime.now());
            }

            TeachersAttendanceEntity saved = attendanceRepository.save(entity);
            log.info("Attendance saved successfully with ID: {}", saved.getId());
            return convertToResponseDto(saved);

        } catch (Exception e) {
            log.error("Error marking attendance: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to mark attendance: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TeachersAttendanceResponseDto getAttendanceById(Long id) {
        TeachersAttendanceEntity entity = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with id: " + id));
        return convertToResponseDto(entity);
    }

    @Override
    public TeachersAttendanceResponseDto updateAttendance(Long id, TeachersAttendanceRequestDto requestDto) {
        TeachersAttendanceEntity entity = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with id: " + id));

        if (requestDto.getStatus() != null) {
            validateStatus(requestDto.getStatus());
        }

        if (requestDto.getTeacherId() != null &&
                !requestDto.getTeacherId().equals(entity.getTeacher().getId())) {
            TeacherEntity teacher = teacherRepository.findById(requestDto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + requestDto.getTeacherId()));
            entity.setTeacher(teacher);
            entity.setTeacherCode(teacher.getTeacherCode());
        }

        if (requestDto.getAttendanceDate() != null) {
            validateAttendanceDate(requestDto.getAttendanceDate());
            entity.setAttendanceDate(requestDto.getAttendanceDate());
        }
        if (requestDto.getStatus() != null) {
            entity.setStatus(requestDto.getStatus());
        }
        if (requestDto.getRemarks() != null) {
            entity.setRemarks(requestDto.getRemarks());
        }

        entity.setUpdatedAt(LocalDateTime.now());
        TeachersAttendanceEntity updatedEntity = attendanceRepository.save(entity);
        return convertToResponseDto(updatedEntity);
    }

    @Override
    public void deleteAttendance(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attendance not found with id: " + id);
        }
        attendanceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeachersAttendanceResponseDto> getAllAttendance(Pageable pageable) {
        return attendanceRepository.findAll(pageable).map(this::convertToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachersAttendanceResponseDto> getAttendanceByTeacherId(Long teacherId) {
        teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));
        return attendanceRepository.findByTeacherId(teacherId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeachersAttendanceResponseDto> getAttendanceByTeacherId(Long teacherId, Pageable pageable) {
        teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));
        return attendanceRepository.findByTeacherId(teacherId, pageable)
                .map(this::convertToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachersAttendanceResponseDto> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDate(date).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeachersAttendanceResponseDto> getAttendanceByDate(LocalDate date, Pageable pageable) {
        return attendanceRepository.findByAttendanceDate(date, pageable)
                .map(this::convertToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachersAttendanceResponseDto> getTodaysAttendance() {
        return getAttendanceByDate(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeachersAttendanceResponseDto> getTodaysAttendance(Pageable pageable) {
        return getAttendanceByDate(LocalDate.now(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachersAttendanceResponseDto> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByAttendanceDateBetween(startDate, endDate).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeachersAttendanceResponseDto> getAttendanceByDateRange(LocalDate startDate,
                                                                        LocalDate endDate,
                                                                        Pageable pageable) {
        return attendanceRepository.findByAttendanceDateBetween(startDate, endDate, pageable)
                .map(this::convertToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachersAttendanceResponseDto> getAttendanceByTeacherAndDateRange(
            Long teacherId, LocalDate startDate, LocalDate endDate) {
        teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));
        return attendanceRepository.findByTeacherIdAndAttendanceDateBetween(teacherId, startDate, endDate).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeachersAttendanceResponseDto> getAttendanceByTeacherAndDateRange(
            Long teacherId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));
        return attendanceRepository.findByTeacherIdAndAttendanceDateBetween(teacherId, startDate, endDate, pageable)
                .map(this::convertToResponseDto);
    }

    @Override
    public Map<String, Object> markBulkAttendance(List<TeachersAttendanceRequestDto> requestDtos) {
        Map<String, Object> result = new HashMap<>();
        List<TeachersAttendanceResponseDto> successful = new ArrayList<>();
        List<Map<String, String>> failed = new ArrayList<>();

        for (TeachersAttendanceRequestDto dto : requestDtos) {
            try {
                TeachersAttendanceResponseDto response = markAttendance(dto);
                successful.add(response);
            } catch (Exception e) {
                Map<String, String> error = new HashMap<>();
                error.put("teacherId", String.valueOf(dto.getTeacherId()));
                error.put("error", e.getMessage());
                failed.add(error);
            }
        }

        result.put("successful", successful);
        result.put("failed", failed);
        result.put("totalProcessed", requestDtos.size());
        result.put("successCount", successful.size());
        result.put("failureCount", failed.size());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAttendanceStatistics(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        List<TeachersAttendanceEntity> attendanceList = attendanceRepository.findByAttendanceDate(date);

        long presentCount = 0, absentCount = 0, leaveCount = 0, lateCount = 0, halfDayCount = 0;

        for (TeachersAttendanceEntity a : attendanceList) {
            switch (a.getStatus()) {
                case "Present": presentCount++; break;
                case "Absent": absentCount++; break;
                case "Leave": leaveCount++; break;
                case "Late": lateCount++; break;
                case "Half Day": halfDayCount++; break;
            }
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("attendanceDate", date);
        statistics.put("totalTeachers", attendanceList.size());
        statistics.put("presentCount", presentCount);
        statistics.put("absentCount", absentCount);
        statistics.put("leaveCount", leaveCount);
        statistics.put("lateCount", lateCount);
        statistics.put("halfDayCount", halfDayCount);

        Map<String, Long> statusBreakdown = new HashMap<>();
        statusBreakdown.put("Present", presentCount);
        statusBreakdown.put("Absent", absentCount);
        statusBreakdown.put("Leave", leaveCount);
        statusBreakdown.put("Late", lateCount);
        statusBreakdown.put("Half Day", halfDayCount);
        statistics.put("statusBreakdown", statusBreakdown);

        long attendedCount = presentCount + lateCount + halfDayCount;
        long totalMarked = attendanceList.size();
        double attendancePercentage = totalMarked > 0 ? (attendedCount * 100.0) / totalMarked : 0.0;
        statistics.put("attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyAttendanceSummary(Long teacherId, int year, int month) {
        TeacherEntity teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        List<Object[]> summaryData = attendanceRepository.getTeacherMonthlyAttendanceSummary(teacherId, year, month);

        Map<String, Long> statusCount = new HashMap<>();
        for (Object[] row : summaryData) {
            String status = (String) row[0];
            Long count = (Long) row[1];
            statusCount.put(status, count);
        }

        long presentDays = statusCount.getOrDefault("Present", 0L);
        long absentDays = statusCount.getOrDefault("Absent", 0L);
        long lateDays = statusCount.getOrDefault("Late", 0L);
        long halfDays = statusCount.getOrDefault("Half Day", 0L);
        long leaveDays = statusCount.getOrDefault("Leave", 0L);
        long totalDays = presentDays + absentDays + lateDays + halfDays + leaveDays;
        long workingDays = presentDays + lateDays + halfDays;
        double attendancePercentage = totalDays > 0 ? (workingDays * 100.0) / totalDays : 0.0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("teacherId", teacherId);
        summary.put("teacherName", teacher.getFullName());
        summary.put("teacherCode", teacher.getTeacherCode());
        summary.put("department", teacher.getDepartment());
        summary.put("month", year + "-" + String.format("%02d", month));
        summary.put("totalDays", totalDays);
        summary.put("workingDays", workingDays);
        summary.put("presentDays", presentDays);
        summary.put("absentDays", absentDays);
        summary.put("lateDays", lateDays);
        summary.put("halfDays", halfDays);
        summary.put("leaveDays", leaveDays);
        summary.put("attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);
        summary.put("statusBreakdown", statusCount);

        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDepartmentWiseAttendance(LocalDate date) {
        List<TeachersAttendanceEntity> attendanceList = attendanceRepository.findByAttendanceDate(date);
        Map<String, Map<String, Long>> deptStats = new HashMap<>();

        for (TeachersAttendanceEntity a : attendanceList) {
            if (a.getTeacher() != null) {
                String department = a.getTeacher().getDepartment();
                String status = a.getStatus();
                deptStats.putIfAbsent(department, new HashMap<>());
                Map<String, Long> statusMap = deptStats.get(department);
                statusMap.put(status, statusMap.getOrDefault(status, 0L) + 1);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("departmentWise", deptStats);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTeacherCalendarData(Long teacherId, int year, int month) {
        TeacherEntity teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<TeachersAttendanceEntity> monthlyAttendance =
                attendanceRepository.findByTeacherIdAndAttendanceDateBetween(teacherId, startDate, endDate);

        Map<String, Object> calendarData = new HashMap<>();
        calendarData.put("teacherId", teacherId);
        calendarData.put("teacherName", teacher.getFullName());
        calendarData.put("teacherCode", teacher.getTeacherCode());
        calendarData.put("year", year);
        calendarData.put("month", month);

        List<Map<String, Object>> days = new ArrayList<>();

        for (int day = 1; day <= startDate.lengthOfMonth(); day++) {
            LocalDate currentDate = LocalDate.of(year, month, day);
            Optional<TeachersAttendanceEntity> attendance = monthlyAttendance.stream()
                    .filter(a -> a.getAttendanceDate().equals(currentDate))
                    .findFirst();

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", currentDate.toString());
            dayData.put("day", day);
            dayData.put("dayOfWeek", currentDate.getDayOfWeek().toString());

            if (attendance.isPresent()) {
                TeachersAttendanceEntity a = attendance.get();
                dayData.put("hasAttendance", true);
                dayData.put("status", a.getStatus());
                dayData.put("remarks", a.getRemarks());
                dayData.put("checkInTime", a.getCheckInTime() != null ? a.getCheckInTime().toString() : null);
                dayData.put("checkOutTime", a.getCheckOutTime() != null ? a.getCheckOutTime().toString() : null);
            } else {
                dayData.put("hasAttendance", false);
                dayData.put("status", "Not Marked");
            }
            days.add(dayData);
        }

        calendarData.put("days", days);
        return calendarData;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachersAttendanceResponseDto> filterAttendance(
            LocalDate date, String department, String status, String searchTerm) {

        List<TeachersAttendanceEntity> attendanceList = attendanceRepository.findByAttendanceDate(date);
        List<TeachersAttendanceResponseDto> result = new ArrayList<>();

        for (TeachersAttendanceEntity a : attendanceList) {
            boolean matches = true;

            if (department != null && !department.isEmpty()) {
                if (a.getTeacher() == null || !department.equals(a.getTeacher().getDepartment())) {
                    matches = false;
                }
            }

            if (matches && status != null && !status.isEmpty()) {
                if (!status.equals(a.getStatus())) {
                    matches = false;
                }
            }

            if (matches && searchTerm != null && !searchTerm.isEmpty()) {
                if (a.getTeacher() != null) {
                    String fullName = a.getTeacher().getFullName();
                    String teacherCode = a.getTeacher().getTeacherCode();
                    String searchLower = searchTerm.toLowerCase();

                    if (!fullName.toLowerCase().contains(searchLower) &&
                            !teacherCode.toLowerCase().contains(searchLower)) {
                        matches = false;
                    }
                } else {
                    matches = false;
                }
            }

            if (matches) {
                result.add(convertToResponseDto(a));
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllDepartments() {
        return attendanceRepository.findAllDepartments();
    }

    @Override
    public TeachersAttendanceResponseDto markAsProcessedForSalary(Long attendanceId, Long salaryId) {
        TeachersAttendanceEntity attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with id: " + attendanceId));

        TeachersSalaryEntity salary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary record not found with id: " + salaryId));

        attendance.setTeachersSalaryEntity(salary);
        attendance.setUpdatedAt(LocalDateTime.now());

        TeachersAttendanceEntity saved = attendanceRepository.save(attendance);
        return convertToResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateSalarySlipData(Long teacherId, int year, int month) {
        Map<String, Object> summary = getMonthlyAttendanceSummary(teacherId, year, month);
        TeacherEntity teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        Map<String, Object> salaryData = new HashMap<>();
        salaryData.put("teacherId", teacherId);
        salaryData.put("teacherName", teacher.getFullName());
        salaryData.put("teacherCode", teacher.getTeacherCode());
        salaryData.put("department", teacher.getDepartment());
        salaryData.put("designation", teacher.getDesignation());
        salaryData.put("month", year + "-" + String.format("%02d", month));
        salaryData.put("workingDays", summary.get("workingDays"));
        salaryData.put("totalDays", summary.get("totalDays"));
        salaryData.put("attendancePercentage", summary.get("attendancePercentage"));
        salaryData.put("leaveDays", summary.get("leaveDays"));
        salaryData.put("absentDays", summary.get("absentDays"));
        salaryData.put("basicSalary", teacher.getBasicSalary() != null ? teacher.getBasicSalary() : 0.0);
        salaryData.put("hra", teacher.getHra() != null ? teacher.getHra() : 0.0);
        salaryData.put("da", teacher.getDa() != null ? teacher.getDa() : 0.0);
        salaryData.put("ta", teacher.getTa() != null ? teacher.getTa() : 0.0);
        salaryData.put("grossSalary", teacher.getGrossSalary() != null ? teacher.getGrossSalary() : 0.0);
        salaryData.put("deductions", 0.0);
        salaryData.put("netSalary", teacher.getGrossSalary() != null ? teacher.getGrossSalary() : 0.0);

        return salaryData;
    }

    // ========== CHECK-IN/CHECK-OUT METHODS ==========

    @Override
    public TeachersAttendanceResponseDto checkIn(Long teacherId, LocalDate date, String teacherCode) {
        log.info("Check-in for teacher ID: {} on date: {} with code: {}", teacherId, date, teacherCode);

        TeacherEntity teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));

        if (teacherCode != null && !teacherCode.isEmpty()) {
            if (!teacherCode.equals(teacher.getTeacherCode())) {
                log.warn("Teacher code mismatch. Provided: {}, Actual: {}", teacherCode, teacher.getTeacherCode());
                throw new ValidationException("Invalid teacher code for this teacher");
            }
        }

        validateAttendanceDate(date);

        Optional<TeachersAttendanceEntity> existingAttendance = attendanceRepository
                .findByTeacherIdAndAttendanceDate(teacherId, date);

        TeachersAttendanceEntity entity;
        LocalTime now = LocalTime.now();

        if (existingAttendance.isPresent()) {
            entity = existingAttendance.get();
            if (entity.getCheckInTime() != null) {
                throw new ValidationException("Teacher already checked in at: " + entity.getCheckInTime());
            }
            entity.setCheckInTime(now);
            entity.setStatus("Present");
            entity.setUpdatedAt(LocalDateTime.now());
            log.info("Updated existing attendance with check-in time: {}", now);
        } else {
            entity = new TeachersAttendanceEntity();
            entity.setTeacher(teacher);
            entity.setTeacherCode(teacher.getTeacherCode());
            entity.setAttendanceDate(date);
            entity.setCheckInTime(now);
            entity.setStatus("Present");
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            log.info("Created new attendance record with check-in time: {}", now);
        }

        TeachersAttendanceEntity saved = attendanceRepository.save(entity);
        log.info("Check-in completed successfully with ID: {}", saved.getId());
        return convertToResponseDto(saved);
    }

    @Override
    public TeachersAttendanceResponseDto checkOut(Long teacherId, LocalDate date, String teacherCode) {
        log.info("Check-out for teacher ID: {} on date: {} with code: {}", teacherId, date, teacherCode);

        validateAttendanceDate(date);

        TeachersAttendanceEntity entity = attendanceRepository
                .findByTeacherIdAndAttendanceDate(teacherId, date)
                .orElseThrow(() -> new ValidationException("No attendance record found for teacher ID: " + teacherId));

        if (teacherCode != null && !teacherCode.isEmpty()) {
            if (entity.getTeacher() != null && !teacherCode.equals(entity.getTeacher().getTeacherCode())) {
                throw new ValidationException("Invalid teacher code for this attendance record");
            }
        }

        if (entity.getCheckInTime() == null) {
            throw new ValidationException("Teacher has not checked in yet");
        }

        if (entity.getCheckOutTime() != null) {
            throw new ValidationException("Teacher already checked out at: " + entity.getCheckOutTime());
        }

        LocalTime now = LocalTime.now();
        entity.setCheckOutTime(now);
        entity.setUpdatedAt(LocalDateTime.now());

        if (entity.getCheckInTime() != null) {
            long minutes = java.time.Duration.between(entity.getCheckInTime(), now).toMinutes();
            log.info("Teacher worked for {} minutes", minutes);
        }

        TeachersAttendanceEntity saved = attendanceRepository.save(entity);
        log.info("Check-out completed successfully at: {}", now);
        return convertToResponseDto(saved);
    }

    @Override
    public List<Map<String, Object>> getTeachersWithoutCheckIn() {
        log.info("Fetching teachers who haven't checked in today");
        LocalDate today = LocalDate.now();
        List<TeacherEntity> teachersWithoutCheckIn = attendanceRepository.findTeachersWithoutCheckIn(today);

        return teachersWithoutCheckIn.stream().map(teacher -> {
            Map<String, Object> teacherInfo = new HashMap<>();
            teacherInfo.put("teacherId", teacher.getId());
            teacherInfo.put("teacherCode", teacher.getTeacherCode());
            teacherInfo.put("teacherName", teacher.getFullName());
            teacherInfo.put("department", teacher.getDepartment());
            return teacherInfo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TeachersAttendanceResponseDto> getTeachersWithoutCheckOut() {
        log.info("Fetching teachers who haven't checked out today");
        LocalDate today = LocalDate.now();
        List<TeachersAttendanceEntity> withoutCheckOut = attendanceRepository.findTeachersWithoutCheckOut(today);
        return withoutCheckOut.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeachersAttendanceResponseDto> getLateCheckIns(LocalDate date, String lateThreshold) {
        log.info("Fetching late check-ins for date: {} with threshold: {}", date, lateThreshold);
        LocalTime threshold = lateThreshold != null ?
                LocalTime.parse(lateThreshold, DateTimeFormatter.ofPattern("HH:mm:ss")) :
                DEFAULT_LATE_THRESHOLD;
        List<TeachersAttendanceEntity> lateCheckIns = attendanceRepository.findLateCheckIns(date, threshold);
        return lateCheckIns.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getCheckInOutStatistics(LocalDate date) {
        log.info("Getting check-in/out statistics for date: {}", date);
        long checkedIn = attendanceRepository.countCheckedInByDate(date);
        long checkedOut = attendanceRepository.countCheckedOutByDate(date);
        long totalTeachers = teacherRepository.countByIsDeletedFalse();

        Map<String, Object> stats = new HashMap<>();
        stats.put("date", date);
        stats.put("totalTeachers", totalTeachers);
        stats.put("checkedIn", checkedIn);
        stats.put("checkedOut", checkedOut);
        stats.put("pendingCheckIn", totalTeachers - checkedIn);
        stats.put("pendingCheckOut", checkedIn - checkedOut);
        return stats;
    }

    @Override
    public Map<String, Object> getTodayCheckInOutSummary() {
        LocalDate today = LocalDate.now();
        return getCheckInOutStatistics(today);
    }

    @Override
    public boolean hasCheckedIn(Long teacherId, LocalDate date) {
        Optional<TeachersAttendanceEntity> attendance = attendanceRepository
                .findByTeacherIdAndAttendanceDate(teacherId, date);
        return attendance.isPresent() && attendance.get().getCheckInTime() != null;
    }

    @Override
    public boolean hasCheckedOut(Long teacherId, LocalDate date) {
        Optional<TeachersAttendanceEntity> attendance = attendanceRepository
                .findByTeacherIdAndAttendanceDate(teacherId, date);
        return attendance.isPresent() && attendance.get().getCheckOutTime() != null;
    }

    @Override
    public List<TeachersAttendanceResponseDto> getTeacherCheckInOutHistory(Long teacherId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching check-in/out history for teacher {} from {} to {}", teacherId, startDate, endDate);
        List<TeachersAttendanceEntity> attendanceList = attendanceRepository
                .findByTeacherIdAndAttendanceDateBetween(teacherId, startDate, endDate);
        return attendanceList.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getMonthlyCheckInOutSummary(Long teacherId, int year, int month) {
        log.info("Getting monthly check-in/out summary for teacher {} - {}/{}", teacherId, year, month);
        List<TeachersAttendanceEntity> attendanceList = attendanceRepository
                .findTeacherAttendanceByMonth(teacherId, year, month);

        List<Map<String, Object>> dailySummary = new ArrayList<>();
        int totalCheckIns = 0;
        int totalCheckOuts = 0;

        for (TeachersAttendanceEntity attendance : attendanceList) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", attendance.getAttendanceDate().toString());
            dayData.put("status", attendance.getStatus());
            dayData.put("checkInTime", attendance.getCheckInTime() != null ? attendance.getCheckInTime().toString() : null);
            dayData.put("checkOutTime", attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().toString() : null);
            dailySummary.add(dayData);

            if (attendance.getCheckInTime() != null) totalCheckIns++;
            if (attendance.getCheckOutTime() != null) totalCheckOuts++;
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("teacherId", teacherId);
        summary.put("year", year);
        summary.put("month", month);
        summary.put("totalDays", attendanceList.size());
        summary.put("totalCheckIns", totalCheckIns);
        summary.put("totalCheckOuts", totalCheckOuts);
        summary.put("dailySummary", dailySummary);
        return summary;
    }

    // ========== HELPER METHODS ==========

    private void validateStatus(String status) {
        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new ValidationException("Invalid status. Must be one of: " + VALID_STATUSES);
        }
    }

    private void validateAttendanceDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isAfter(today)) {
            throw new ValidationException("Cannot mark attendance for future dates");
        }
        if (date.isBefore(today.minusDays(MAX_PAST_DAYS))) {
            throw new ValidationException("Cannot mark attendance for dates more than " +
                    MAX_PAST_DAYS + " days in the past");
        }
    }

    private TeachersAttendanceResponseDto convertToResponseDto(TeachersAttendanceEntity entity) {
        TeachersAttendanceResponseDto dto = new TeachersAttendanceResponseDto();
        dto.setId(entity.getId());

        if (entity.getTeacher() != null) {
            dto.setTeacherId(entity.getTeacher().getId());
            dto.setTeacherCode(entity.getTeacher().getTeacherCode());
            dto.setTeacherName(entity.getTeacher().getFullName());
            dto.setDepartment(entity.getTeacher().getDepartment());

            String teacherCode = entity.getTeacher().getTeacherCode();
            if (teacherCode != null && !teacherCode.isEmpty()) {
                dto.setProfileImageUrl("https://i.pravatar.cc/64?u=" + teacherCode);
            } else {
                dto.setProfileImageUrl("https://i.pravatar.cc/64?u=" + entity.getTeacher().getId());
            }
        }

        dto.setAttendanceDate(entity.getAttendanceDate());
        dto.setStatus(entity.getStatus());
        dto.setCheckInTime(entity.getCheckInTime());
        dto.setCheckOutTime(entity.getCheckOutTime());
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getTeachersSalaryEntity() != null) {
            dto.setSalaryId(entity.getTeachersSalaryEntity().getId());
        }

        return dto;
    }
}