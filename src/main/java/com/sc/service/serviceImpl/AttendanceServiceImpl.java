package com.sc.service.serviceImpl;

import com.sc.dto.request.AttendanceRequestDto;
import com.sc.dto.request.BulkAttendanceRequestDto;
import com.sc.dto.request.HolidayRequestDto;
import com.sc.dto.response.AttendancePercentageDto;
import com.sc.dto.response.AttendanceResponseDto;
import com.sc.dto.response.MonthlyAttendanceSummaryDto;
import com.sc.entity.AttendanceEntity;
import com.sc.entity.HolidayEntity;
import com.sc.entity.StudentEntity;
import com.sc.repository.AttendanceRepository;
import com.sc.repository.HolidayRepository;
import com.sc.repository.StudentRepository;
import com.sc.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceServiceImpl.class);

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    private static final List<Integer> WEEKEND_DAYS = Arrays.asList(6, 7); // Saturday=6, Sunday=7

    @Override
    @Transactional
    public AttendanceResponseDto markAttendance(AttendanceRequestDto requestDto, String markedBy) {
        logger.info("Marking attendance for student: {} on date: {}", requestDto.getStudentId(), requestDto.getDate());

        StudentEntity student = studentRepository.findById(requestDto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Check if date is working day
        boolean isWorkingDay = isWorkingDay(requestDto.getDate());

        // Check if attendance already exists
        AttendanceEntity attendance = attendanceRepository
                .findByStudentIdAndAttendanceDate(requestDto.getStudentId(), requestDto.getDate())
                .orElse(new AttendanceEntity());

        attendance.setStudent(student);
        attendance.setAttendanceDate(requestDto.getDate());
        attendance.setStatus(requestDto.getStatus());
        attendance.setLeaveType(requestDto.getLeaveType());
        attendance.setReason(requestDto.getReason());
        attendance.setIsWorkingDay(isWorkingDay);
        attendance.setMarkedBy(markedBy);

        AttendanceEntity saved = attendanceRepository.save(attendance);
        logger.info("Attendance marked successfully with ID: {}", saved.getId());

        return convertToDto(saved);
    }

    @Override
    @Transactional
    public List<AttendanceResponseDto> markBulkAttendance(BulkAttendanceRequestDto requestDto, String markedBy) {
        logger.info("Marking bulk attendance for class: {} section: {} on date: {}",
                requestDto.getClassName(), requestDto.getSection(), requestDto.getDate());

        List<AttendanceResponseDto> responses = new ArrayList<>();

        for (BulkAttendanceRequestDto.StudentAttendanceDto dto : requestDto.getAttendanceList()) {
            try {
                AttendanceRequestDto singleRequest = new AttendanceRequestDto();
                singleRequest.setStudentId(dto.getStudentId());
                singleRequest.setDate(requestDto.getDate());
                singleRequest.setStatus(dto.getStatus());
                singleRequest.setLeaveType(dto.getLeaveType());
                singleRequest.setReason(dto.getReason());

                AttendanceResponseDto response = markAttendance(singleRequest, markedBy);
                responses.add(response);
            } catch (Exception e) {
                logger.error("Failed to mark attendance for student: {}", dto.getStudentId());
            }
        }

        return responses;
    }

    @Override
    public List<AttendanceResponseDto> getAttendanceByClassAndDate(String className, String section, LocalDate date) {
        logger.info("Getting attendance for class: {} section: {} on date: {}", className, section, date);

        return attendanceRepository.findByClassAndSectionAndDate(className, section, date)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceResponseDto> getStudentAttendance(Long studentId, LocalDate startDate, LocalDate endDate) {
        logger.info("Getting attendance for student: {} from {} to {}", studentId, startDate, endDate);

        return attendanceRepository.findByStudentIdAndAttendanceDateBetween(studentId, startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AttendancePercentageDto calculateAttendancePercentage(Long studentId, LocalDate startDate, LocalDate endDate) {
        logger.info("Calculating attendance % for student: {} from {} to {}", studentId, startDate, endDate);

        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        long workingDays = attendanceRepository.countWorkingDays(studentId, startDate, endDate);
        long presentDays = attendanceRepository.countPresentDays(studentId, startDate, endDate);

        double percentage = workingDays > 0 ? (presentDays * 100.0) / workingDays : 0;

        AttendancePercentageDto dto = new AttendancePercentageDto();
        dto.setStudentId(studentId);
        dto.setStudentName(student.getFirstName() + " " + student.getLastName());
        dto.setClassName(student.getCurrentClass());
        dto.setSection(student.getSection());
        dto.setRollNumber(student.getStudentRollNumber());
        dto.setTotalWorkingDays((int) workingDays);
        dto.setPresentDays((int) presentDays);
        dto.setAbsentDays((int) (workingDays - presentDays));
        dto.setPercentage(Math.round(percentage * 100.0) / 100.0);

        if (percentage >= 75) dto.setStatus("GOOD");
        else if (percentage >= 60) dto.setStatus("AVERAGE");
        else dto.setStatus("POOR");

        return dto;
    }

    @Override
    public MonthlyAttendanceSummaryDto getMonthlySummary(String className, String section, int year, int month) {
        logger.info("Generating monthly summary for class: {} section: {} - {}/{}", className, section, month, year);

        MonthlyAttendanceSummaryDto summary = new MonthlyAttendanceSummaryDto();
        summary.setClassName(className);
        summary.setSection(section);
        summary.setYear(year);
        summary.setMonth(month);

        // Month name
        YearMonth yearMonth = YearMonth.of(year, month);
        summary.setMonthName(yearMonth.getMonth().toString());

        // Get all students
        List<StudentEntity> students = studentRepository.findByCurrentClassAndSection(className, section);
        summary.setTotalStudents(students.size());

        // Get holidays that DON'T affect attendance
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<LocalDate> nonAffectingHolidays = getNonAffectingHolidays(startDate, endDate);

        // Calculate working days and prepare daily summary
        Map<LocalDate, MonthlyAttendanceSummaryDto.DailySummaryDto> dailySummary = new LinkedHashMap<>();
        Map<Long, MonthlyAttendanceSummaryDto.StudentMonthlySummaryDto> studentSummaryMap = new HashMap<>();

        // Initialize student summaries
        for (StudentEntity student : students) {
            MonthlyAttendanceSummaryDto.StudentMonthlySummaryDto studentSummary =
                    new MonthlyAttendanceSummaryDto.StudentMonthlySummaryDto();
            studentSummary.setStudentId(student.getStdId());
            studentSummary.setStudentName(student.getFirstName() + " " + student.getLastName());
            studentSummary.setRollNumber(student.getStudentRollNumber());
            studentSummary.setDailyStatus(new LinkedHashMap<>());
            studentSummary.setPresentCount(0);
            studentSummary.setAbsentCount(0);
            studentSummary.setLeaveCount(0);
            studentSummaryMap.put(student.getStdId(), studentSummary);
        }

        int totalWorkingDays = 0;
        int totalHolidays = 0;

        // Process each day of the month
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate currentDate = LocalDate.of(year, month, day);
            String dayOfWeek = currentDate.getDayOfWeek().toString();
            int dayOfWeekValue = currentDate.getDayOfWeek().getValue(); // 1-7

            MonthlyAttendanceSummaryDto.DailySummaryDto dailyDto =
                    new MonthlyAttendanceSummaryDto.DailySummaryDto();
            dailyDto.setDate(currentDate);
            dailyDto.setDayOfWeek(dayOfWeek);

            // Check if holiday (non-affecting)
            if (nonAffectingHolidays.contains(currentDate)) {
                dailyDto.setHoliday(true);
                dailyDto.setHolidayName("Holiday");
                dailyDto.setWorkingDay(false);
                dailyDto.setPresent(0);
                dailyDto.setAbsent(0);
                dailyDto.setLeave(0);
                dailySummary.put(currentDate, dailyDto);
                totalHolidays++;
                continue;
            }

            // Check if weekend (Saturday/Sunday)
            if (WEEKEND_DAYS.contains(dayOfWeekValue)) {
                dailyDto.setHoliday(true);
                dailyDto.setHolidayName("Weekend");
                dailyDto.setWorkingDay(false);
                dailyDto.setPresent(0);
                dailyDto.setAbsent(0);
                dailyDto.setLeave(0);
                dailySummary.put(currentDate, dailyDto);
                totalHolidays++;
                continue;
            }

            // Working day
            dailyDto.setWorkingDay(true);
            totalWorkingDays++;

            // Get attendance for this day
            List<AttendanceEntity> dayAttendance = attendanceRepository
                    .findByClassAndSectionAndDate(className, section, currentDate);

            Map<Long, AttendanceEntity> attendanceMap = dayAttendance.stream()
                    .collect(Collectors.toMap(a -> a.getStudent().getStdId(), a -> a));

            int present = 0, absent = 0, leave = 0;

            // Update each student's daily status
            for (StudentEntity student : students) {
                MonthlyAttendanceSummaryDto.StudentMonthlySummaryDto studentSummary =
                        studentSummaryMap.get(student.getStdId());
                AttendanceEntity att = attendanceMap.get(student.getStdId());

                String statusCode;
                if (att == null || "ABSENT".equals(att.getStatus())) {
                    statusCode = "A";
                    absent++;
                    studentSummary.setAbsentCount(studentSummary.getAbsentCount() + 1);
                } else if ("PRESENT".equals(att.getStatus())) {
                    statusCode = "P";
                    present++;
                    studentSummary.setPresentCount(studentSummary.getPresentCount() + 1);
                } else if ("LEAVE".equals(att.getStatus())) {
                    statusCode = "L";
                    leave++;
                    studentSummary.setLeaveCount(studentSummary.getLeaveCount() + 1);
                } else {
                    statusCode = "A";
                    absent++;
                    studentSummary.setAbsentCount(studentSummary.getAbsentCount() + 1);
                }

                studentSummary.getDailyStatus().put(currentDate, statusCode);
            }

            dailyDto.setPresent(present);
            dailyDto.setAbsent(absent);
            dailyDto.setLeave(leave);
            dailySummary.put(currentDate, dailyDto);
        }

        summary.setTotalWorkingDays(totalWorkingDays);
        summary.setTotalHolidays(totalHolidays);
        summary.setDailySummary(dailySummary);

        // Calculate percentages and create student list
        List<MonthlyAttendanceSummaryDto.StudentMonthlySummaryDto> studentSummaries = new ArrayList<>();
        int totalPresent = 0, totalAbsent = 0, totalLeave = 0;

        for (MonthlyAttendanceSummaryDto.StudentMonthlySummaryDto studentSummary : studentSummaryMap.values()) {
            double percentage = totalWorkingDays > 0 ?
                    ((studentSummary.getPresentCount() + studentSummary.getLeaveCount()) * 100.0) / totalWorkingDays : 0;
            studentSummary.setAttendancePercentage(Math.round(percentage * 100.0) / 100.0);

            if (percentage >= 75) studentSummary.setAttendanceStatus("GOOD");
            else if (percentage >= 60) studentSummary.setAttendanceStatus("AVERAGE");
            else studentSummary.setAttendanceStatus("POOR");

            studentSummaries.add(studentSummary);

            totalPresent += studentSummary.getPresentCount();
            totalAbsent += studentSummary.getAbsentCount();
            totalLeave += studentSummary.getLeaveCount();
        }

        summary.setStudentSummaries(studentSummaries);

        // Overall stats
        MonthlyAttendanceSummaryDto.OverallStatsDto overallStats =
                new MonthlyAttendanceSummaryDto.OverallStatsDto();
        overallStats.setTotalPresent(totalPresent);
        overallStats.setTotalAbsent(totalAbsent);
        overallStats.setTotalLeave(totalLeave);
        overallStats.setAverageAttendance(students.size() > 0 ?
                studentSummaries.stream()
                        .mapToDouble(MonthlyAttendanceSummaryDto.StudentMonthlySummaryDto::getAttendancePercentage)
                        .average().orElse(0) : 0);

        summary.setOverallStats(overallStats);

        return summary;
    }

    @Override
    @Transactional
    public void addHoliday(HolidayRequestDto requestDto, String createdBy) {
        logger.info("Adding holiday: {} on date: {}", requestDto.getHolidayName(), requestDto.getHolidayDate());

        HolidayEntity holiday = new HolidayEntity();
        holiday.setHolidayName(requestDto.getHolidayName());
        holiday.setHolidayDate(requestDto.getHolidayDate());
        holiday.setHolidayType(requestDto.getHolidayType());
        holiday.setDescription(requestDto.getDescription());
        holiday.setAffectsAttendance(requestDto.getAffectsAttendance() != null ?
                requestDto.getAffectsAttendance() : false);
        holiday.setApplicableClasses(requestDto.getApplicableClasses());
        holiday.setIsRecurring(requestDto.getIsRecurring() != null ? requestDto.getIsRecurring() : false);
        holiday.setCreatedBy(createdBy);

        holidayRepository.save(holiday);
        logger.info("Holiday added successfully");
    }

    @Override
    public List<LocalDate> getNonAffectingHolidays(LocalDate startDate, LocalDate endDate) {
        return holidayRepository.findNonAffectingHolidayDates(startDate, endDate);
    }

    @Override
    public boolean isWorkingDay(LocalDate date) {
        // Check if holiday (non-affecting)
        Optional<HolidayEntity> holiday = holidayRepository.findByHolidayDate(date);
        if (holiday.isPresent() && !holiday.get().getAffectsAttendance()) {
            return false;
        }

        // Check if weekend
        int dayOfWeek = date.getDayOfWeek().getValue();
        if (WEEKEND_DAYS.contains(dayOfWeek)) {
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public AttendanceResponseDto updateAttendance(Long attendanceId, AttendanceRequestDto requestDto, String updatedBy) {
        logger.info("Updating attendance with ID: {}", attendanceId);

        AttendanceEntity attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        if (requestDto.getStatus() != null) attendance.setStatus(requestDto.getStatus());
        if (requestDto.getLeaveType() != null) attendance.setLeaveType(requestDto.getLeaveType());
        if (requestDto.getReason() != null) attendance.setReason(requestDto.getReason());
        attendance.setMarkedBy(updatedBy);

        AttendanceEntity updated = attendanceRepository.save(attendance);
        return convertToDto(updated);
    }

    private AttendanceResponseDto convertToDto(AttendanceEntity entity) {
        AttendanceResponseDto dto = new AttendanceResponseDto();
        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudent().getStdId());
        dto.setStudentName(entity.getStudent().getFirstName() + " " + entity.getStudent().getLastName());
        dto.setClassName(entity.getStudent().getCurrentClass());
        dto.setSection(entity.getStudent().getSection());
        dto.setRollNumber(entity.getStudent().getStudentRollNumber());
        dto.setDate(entity.getAttendanceDate());
        dto.setStatus(entity.getStatus());
        dto.setLeaveType(entity.getLeaveType());
        dto.setReason(entity.getReason());
        dto.setIsWorkingDay(entity.getIsWorkingDay());
        dto.setMarkedAt(entity.getMarkedAt());
        dto.setMarkedBy(entity.getMarkedBy());
        return dto;
    }
}