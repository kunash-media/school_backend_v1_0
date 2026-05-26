package com.sc.service.serviceImpl;

import com.sc.CustomExceptions.ResourceNotFoundException;
import com.sc.dto.request.TimetableRequestDto;
import com.sc.dto.response.TimetableResponseDto;
import com.sc.entity.ClassEntity;
import com.sc.entity.TeacherEntity;
import com.sc.entity.TimetableEntryEntity;
import com.sc.repository.ClassRepository;
import com.sc.repository.TeacherRepository;
import com.sc.repository.TimetableEntryRepository;
import com.sc.service.TimetableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TimetableServiceImpl implements TimetableService {

    @Autowired
    private TimetableEntryRepository timetableRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Override
    public TimetableResponseDto createTimetable(TimetableRequestDto request) {
        TimetableResponseDto response = new TimetableResponseDto();

        try {
            validateCreateRequest(request);

            ClassEntity classEntity = findClass(request.getClassCode(), request.getClassName(), request.getSection());

            List<String> targetDays = determineTargetDays(request);
            List<Integer> targetWeeks = determineTargetWeeks(request);

            // Clear existing if needed
            if (request.getMode() != null && (request.getMode().equals("week") || request.getMode().equals("month"))) {
                clearExistingEntries(classEntity.getClassId(), request.getSection(),
                        targetDays, targetWeeks, request.getAcademicYear());
            }

            List<TimetableEntryEntity> createdEntries = new ArrayList<>();

            for (String day : targetDays) {
                for (Integer week : targetWeeks) {
                    if (request.getPeriods() != null) {
                        for (TimetableRequestDto.PeriodDto periodReq : request.getPeriods()) {
                            TimetableEntryEntity entry = createPeriodEntry(
                                    classEntity, request.getSection(), request.getAcademicYear(),
                                    day, week, periodReq, request.getCreatedBy()
                            );
                            createdEntries.add(timetableRepository.save(entry));
                        }
                    }
                }
            }

            response.setSuccess(true);
            response.setMessage("Timetable created successfully");
            response.setData(mapToTimetableData(createdEntries, request));

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Failed to create timetable: " + e.getMessage());
            response.setErrors(Collections.singletonList(e.getMessage()));
        }

        return response;
    }

    @Override
    public TimetableResponseDto updatePeriod(TimetableRequestDto request) {
        TimetableResponseDto response = new TimetableResponseDto();

        try {
            ClassEntity classEntity = findClass(request.getClassCode(), request.getClassName(), request.getSection());

            Optional<TimetableEntryEntity> existing = timetableRepository
                    .findByClassEntity_ClassIdAndSectionAndDayAndPeriodNumberAndWeekNumberAndAcademicYearAndIsDeletedFalse(
                            classEntity.getClassId(),
                            request.getSection(),
                            request.getDay(),
                            request.getPeriod(),
                            request.getWeekNumber() != null ? request.getWeekNumber() : 1,
                            request.getAcademicYear()
                    );

            TimetableEntryEntity entry;

            if (existing.isPresent()) {
                entry = existing.get();
                updateEntryFromRequest(entry, request);
            } else {
                entry = createEntryFromRequest(classEntity, request);
            }

            // Check conflicts
            List<TimetableResponseDto.ConflictData> conflicts = checkConflictsForEntry(entry);
            if (!conflicts.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Schedule conflict detected");
                TimetableResponseDto.TimetableData data = new TimetableResponseDto.TimetableData();
                data.setConflicts(conflicts);
                response.setData(data);
                return response;
            }

            TimetableEntryEntity saved = timetableRepository.save(entry);

            response.setSuccess(true);
            response.setMessage("Period updated successfully");
            response.setData(mapToTimetableData(Collections.singletonList(saved), request));

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Failed to update period: " + e.getMessage());
            response.setErrors(Collections.singletonList(e.getMessage()));
        }

        return response;
    }

    @Override
    public TimetableResponseDto getTimetable(TimetableRequestDto request) {
        TimetableResponseDto response = new TimetableResponseDto();

        try {
            ClassEntity classEntity = findClass(request.getClassCode(), request.getClassName(), request.getSection());

            List<TimetableEntryEntity> entries;

            if (request.getDay() != null && !request.getDay().isEmpty()) {
                entries = timetableRepository
                        .findByClassEntity_ClassIdAndSectionAndDayAndAcademicYearAndIsDeletedFalse(
                                classEntity.getClassId(),
                                request.getSection(),
                                request.getDay(),
                                request.getAcademicYear()
                        );
            } else if (request.getWeekNumber() != null) {
                entries = timetableRepository
                        .findByClassEntity_ClassIdAndSectionAndWeekNumberAndAcademicYearAndIsDeletedFalse(
                                classEntity.getClassId(),
                                request.getSection(),
                                request.getWeekNumber(),
                                request.getAcademicYear()
                        );
            } else {
                entries = timetableRepository
                        .findAllForClass(
                                classEntity.getClassId(),
                                request.getSection(),
                                request.getAcademicYear()
                        );
            }

            // Filter by teacher if specified
            if (request.getTeacherId() != null) {
                entries = entries.stream()
                        .filter(e -> e.getTeacher() != null && e.getTeacher().getId().equals(request.getTeacherId()))
                        .collect(Collectors.toList());
            }

            // Filter by subject if specified
            if (request.getSubjectName() != null && !request.getSubjectName().isEmpty()) {
                entries = entries.stream()
                        .filter(e -> request.getSubjectName().equals(e.getSubjectName()))
                        .collect(Collectors.toList());
            }

            response.setSuccess(true);
            response.setMessage("Timetable retrieved successfully");
            response.setData(mapToTimetableData(entries, request));

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Failed to get timetable: " + e.getMessage());
            response.setErrors(Collections.singletonList(e.getMessage()));
        }

        return response;
    }

    @Override
    public TimetableResponseDto clearDay(TimetableRequestDto request) {
        TimetableResponseDto response = new TimetableResponseDto();

        try {
            ClassEntity classEntity = findClass(request.getClassCode(), request.getClassName(), request.getSection());

            timetableRepository.softDeleteByClassAndSectionAndDay(
                    classEntity.getClassId(),
                    request.getSection(),
                    request.getDay(),
                    request.getAcademicYear()
            );

            response.setSuccess(true);
            response.setMessage("Day schedule cleared successfully");

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Failed to clear day: " + e.getMessage());
            response.setErrors(Collections.singletonList(e.getMessage()));
        }

        return response;
    }

    @Override
    public TimetableResponseDto checkConflicts(TimetableRequestDto request) {
        TimetableResponseDto response = new TimetableResponseDto();

        try {
            ClassEntity classEntity = findClass(request.getClassCode(), request.getClassName(), request.getSection());

            List<TimetableEntryEntity> entries;

            if (request.getWeekNumber() != null) {
                entries = timetableRepository
                        .findByClassEntity_ClassIdAndSectionAndWeekNumberAndAcademicYearAndIsDeletedFalse(
                                classEntity.getClassId(),
                                request.getSection(),
                                request.getWeekNumber(),
                                request.getAcademicYear()
                        );
            } else {
                entries = timetableRepository
                        .findAllForClass(
                                classEntity.getClassId(),
                                request.getSection(),
                                request.getAcademicYear()
                        );
            }

            List<TimetableResponseDto.ConflictData> conflicts = new ArrayList<>();
            conflicts.addAll(checkTeacherConflicts(entries));
            conflicts.addAll(checkRoomConflicts(entries));

            TimetableResponseDto.TimetableData data = new TimetableResponseDto.TimetableData();
            data.setConflicts(conflicts);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalConflicts", conflicts.size());
            stats.put("teacherConflicts", conflicts.stream().filter(c -> "teacher".equals(c.getType())).count());
            stats.put("roomConflicts", conflicts.stream().filter(c -> "room".equals(c.getType())).count());
            data.setStats(stats);

            response.setSuccess(true);
            response.setMessage(conflicts.isEmpty() ? "No conflicts found" : conflicts.size() + " conflict(s) detected");
            response.setData(data);

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Failed to check conflicts: " + e.getMessage());
            response.setErrors(Collections.singletonList(e.getMessage()));
        }

        return response;
    }

    @Override
    public TimetableResponseDto copySchedule(TimetableRequestDto request) {
        TimetableResponseDto response = new TimetableResponseDto();

        try {
            if (request.getSource() == null || request.getDestination() == null) {
                throw new IllegalArgumentException("Source and destination are required");
            }

            ClassEntity sourceClass = findClass(
                    request.getSource().getClassCode(),
                    request.getSource().getClassName(),
                    request.getSource().getSection()
            );

            List<TimetableEntryEntity> sourceEntries = timetableRepository
                    .findByClassEntity_ClassIdAndSectionAndDayAndAcademicYearAndIsDeletedFalse(
                            sourceClass.getClassId(),
                            request.getSource().getSection(),
                            request.getSource().getDay(),
                            request.getSource().getAcademicYear()
                    );

            if (sourceEntries.isEmpty()) {
                throw new IllegalArgumentException("No source entries found");
            }

            // Clear destination if requested
            if (request.getDestination().getOverrideExisting() != null &&
                    request.getDestination().getOverrideExisting()) {

                for (String day : request.getDestination().getTargetDays()) {
                    for (Integer week : request.getDestination().getWeekNumbers()) {
                        timetableRepository.softDeleteByClassAndSectionAndDay(
                                sourceClass.getClassId(),
                                request.getSource().getSection(),
                                day,
                                request.getSource().getAcademicYear()
                        );
                    }
                }
            }

            List<TimetableEntryEntity> copiedEntries = new ArrayList<>();

            for (String day : request.getDestination().getTargetDays()) {
                for (Integer week : request.getDestination().getWeekNumbers()) {
                    for (TimetableEntryEntity source : sourceEntries) {
                        TimetableEntryEntity newEntry = new TimetableEntryEntity();
                        newEntry.setClassEntity(sourceClass);
                        newEntry.setSection(source.getSection());
                        newEntry.setAcademicYear(source.getAcademicYear());
                        newEntry.setDay(day);
                        newEntry.setPeriodNumber(source.getPeriodNumber());
                        newEntry.setWeekNumber(week);
                        newEntry.setSubjectName(source.getSubjectName());
                        newEntry.setSubjectCode(source.getSubjectCode());
                        newEntry.setTeacher(source.getTeacher());
                        newEntry.setTeacherName(source.getTeacherName());
                        newEntry.setRoomNumber(source.getRoomNumber());
                        newEntry.setRoomType(source.getRoomType());
                        newEntry.setTimeSlot(source.getTimeSlot());
                        newEntry.setIsBreak(source.getIsBreak());
                        newEntry.setBreakType(source.getBreakType());
                        newEntry.setNotes(source.getNotes());
                        newEntry.setCreatedBy(request.getUpdatedBy());
                        newEntry.setUpdatedBy(request.getUpdatedBy());

                        copiedEntries.add(timetableRepository.save(newEntry));
                    }
                }
            }

            response.setSuccess(true);
            response.setMessage("Schedule copied successfully");
            response.setData(mapToTimetableData(copiedEntries, request));

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Failed to copy schedule: " + e.getMessage());
            response.setErrors(Collections.singletonList(e.getMessage()));
        }

        return response;
    }

    @Override
    public TimetableResponseDto deleteEntry(Long id) {
        TimetableResponseDto response = new TimetableResponseDto();

        try {
            TimetableEntryEntity entry = timetableRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id: " + id));

            entry.setIsDeleted(true);
            timetableRepository.save(entry);

            response.setSuccess(true);
            response.setMessage("Entry deleted successfully");

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Failed to delete entry: " + e.getMessage());
            response.setErrors(Collections.singletonList(e.getMessage()));
        }

        return response;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private ClassEntity findClass(String classCode, String className, String section) {
        if (classCode != null && !classCode.isEmpty()) {
            return classRepository.findByClassCode(classCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found with code: " + classCode));
        } else {
            List<ClassEntity> classes = classRepository.findByClassName(className);
            return classes.stream()
                    .filter(c -> section.equals(c.getSection()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Class not found with name: " + className + " and section: " + section));
        }
    }

    private void validateCreateRequest(TimetableRequestDto request) {
        if (request.getPeriods() == null || request.getPeriods().isEmpty()) {
            throw new IllegalArgumentException("At least one period is required");
        }

        if (request.getPeriods().size() > 8) {
            throw new IllegalArgumentException("Maximum 8 periods per day");
        }

        Set<Integer> periodNumbers = request.getPeriods().stream()
                .map(TimetableRequestDto.PeriodDto::getPeriod)
                .collect(Collectors.toSet());

        if (periodNumbers.size() != request.getPeriods().size()) {
            throw new IllegalArgumentException("Duplicate period numbers found");
        }
    }

    private List<String> determineTargetDays(TimetableRequestDto request) {
        if (request.getTargetDays() != null && !request.getTargetDays().isEmpty()) {
            return request.getTargetDays();
        }
        return Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
    }

    private List<Integer> determineTargetWeeks(TimetableRequestDto request) {
        if (request.getApplyToAllWeeks() != null && request.getApplyToAllWeeks()) {
            return Arrays.asList(1, 2, 3, 4);
        }
        return Arrays.asList(request.getWeekNumber() != null ? request.getWeekNumber() : 1);
    }

    private void clearExistingEntries(Long classId, String section, List<String> days, List<Integer> weeks, String academicYear) {
        for (String day : days) {
            List<TimetableEntryEntity> existing = timetableRepository
                    .findByClassEntity_ClassIdAndSectionAndDayAndAcademicYearAndIsDeletedFalse(
                            classId, section, day, academicYear);

            for (TimetableEntryEntity entry : existing) {
                if (weeks.contains(entry.getWeekNumber())) {
                    entry.setIsDeleted(true);
                    timetableRepository.save(entry);
                }
            }
        }
    }

    private TimetableEntryEntity createPeriodEntry(
            ClassEntity classEntity, String section, String academicYear,
            String day, Integer week, TimetableRequestDto.PeriodDto periodReq,
            String createdBy) {

        TeacherEntity teacher = null;
        if (periodReq.getTeacherId() != null) {
            teacher = teacherRepository.findById(periodReq.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + periodReq.getTeacherId()));
        }

        TimetableEntryEntity entry = new TimetableEntryEntity();
        entry.setClassEntity(classEntity);
        entry.setSection(section);
        entry.setAcademicYear(academicYear);
        entry.setDay(day);
        entry.setPeriodNumber(periodReq.getPeriod());
        entry.setWeekNumber(week);
        entry.setSubjectName(periodReq.getSubjectName());
        entry.setSubjectCode(periodReq.getSubjectCode());
        entry.setTeacher(teacher);
        if (teacher != null) {
            entry.setTeacherName(teacher.getFullName());
        }
        entry.setRoomNumber(periodReq.getRoomNumber());
        entry.setRoomType(periodReq.getRoomType());
        entry.setIsBreak(false);
        entry.setNotes(periodReq.getNotes());
        entry.setCreatedBy(createdBy);
        entry.setUpdatedBy(createdBy);

        return entry;
    }

    private TimetableEntryEntity createEntryFromRequest(ClassEntity classEntity, TimetableRequestDto request) {
        TeacherEntity teacher = null;
        if (request.getTeacherId() != null) {
            teacher = teacherRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + request.getTeacherId()));
        }

        TimetableEntryEntity entry = new TimetableEntryEntity();
        entry.setClassEntity(classEntity);
        entry.setSection(request.getSection());
        entry.setAcademicYear(request.getAcademicYear());
        entry.setDay(request.getDay());
        entry.setPeriodNumber(request.getPeriod());
        entry.setWeekNumber(request.getWeekNumber() != null ? request.getWeekNumber() : 1);
        entry.setSubjectName(request.getSubjectName());
        entry.setSubjectCode(request.getSubjectCode());
        entry.setTeacher(teacher);
        if (teacher != null) {
            entry.setTeacherName(teacher.getFullName());
        }
        entry.setRoomNumber(request.getRoomNumber());
        entry.setRoomType(request.getRoomType());
        entry.setIsBreak(request.getIsBreak());
        entry.setBreakType(request.getBreakType());
        entry.setNotes(request.getNotes());
        entry.setCreatedBy(request.getUpdatedBy());
        entry.setUpdatedBy(request.getUpdatedBy());

        return entry;
    }

    private void updateEntryFromRequest(TimetableEntryEntity entry, TimetableRequestDto request) {
        entry.setSubjectName(request.getSubjectName());
        entry.setSubjectCode(request.getSubjectCode());

        if (request.getTeacherId() != null) {
            TeacherEntity teacher = teacherRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + request.getTeacherId()));
            entry.setTeacher(teacher);
            entry.setTeacherName(teacher.getFullName());
        }

        entry.setRoomNumber(request.getRoomNumber());
        entry.setRoomType(request.getRoomType());
        entry.setIsBreak(request.getIsBreak());
        entry.setBreakType(request.getBreakType());
        entry.setNotes(request.getNotes());
        entry.setUpdatedBy(request.getUpdatedBy());
    }

    private List<TimetableResponseDto.ConflictData> checkConflictsForEntry(TimetableEntryEntity entry) {
        List<TimetableResponseDto.ConflictData> conflicts = new ArrayList<>();

        if (entry.getIsBreak() != null && entry.getIsBreak()) {
            return conflicts;
        }

        // Check teacher conflicts
        if (entry.getTeacher() != null) {
            List<TimetableEntryEntity> teacherConflicts = timetableRepository
                    .findTeacherConflicts(
                            entry.getTeacher().getId(),
                            entry.getDay(),
                            entry.getPeriodNumber(),
                            entry.getWeekNumber(),
                            entry.getAcademicYear()
                    )
                    .stream()
                    .filter(e -> !e.getTimetableId().equals(entry.getTimetableId()))
                    .collect(Collectors.toList());

            for (TimetableEntryEntity conflict : teacherConflicts) {
                TimetableResponseDto.ConflictData dto = new TimetableResponseDto.ConflictData();
                dto.setType("teacher");
                dto.setTeacherName(entry.getTeacher().getFullName());
                dto.setTeacherId(entry.getTeacher().getId());
                dto.setDay(entry.getDay());
                dto.setPeriod(entry.getPeriodNumber());
                dto.setWeek(entry.getWeekNumber());
                dto.setConflictingPeriod(conflict.getPeriodNumber());
                dto.setMessage("Teacher " + entry.getTeacher().getFullName() +
                        " is already scheduled in period " + conflict.getPeriodNumber());
                conflicts.add(dto);
            }
        }

        // Check room conflicts
        if (entry.getRoomNumber() != null && !entry.getRoomNumber().isEmpty()) {
            List<TimetableEntryEntity> roomConflicts = timetableRepository
                    .findRoomConflicts(
                            entry.getRoomNumber(),
                            entry.getDay(),
                            entry.getPeriodNumber(),
                            entry.getWeekNumber(),
                            entry.getAcademicYear()
                    )
                    .stream()
                    .filter(e -> !e.getTimetableId().equals(entry.getTimetableId()))
                    .collect(Collectors.toList());

            for (TimetableEntryEntity conflict : roomConflicts) {
                TimetableResponseDto.ConflictData dto = new TimetableResponseDto.ConflictData();
                dto.setType("room");
                dto.setRoomNumber(entry.getRoomNumber());
                dto.setDay(entry.getDay());
                dto.setPeriod(entry.getPeriodNumber());
                dto.setWeek(entry.getWeekNumber());
                dto.setConflictingPeriod(conflict.getPeriodNumber());
                dto.setMessage("Room " + entry.getRoomNumber() +
                        " is already booked for period " + conflict.getPeriodNumber());
                conflicts.add(dto);
            }
        }

        return conflicts;
    }

    private List<TimetableResponseDto.ConflictData> checkTeacherConflicts(List<TimetableEntryEntity> entries) {
        List<TimetableResponseDto.ConflictData> conflicts = new ArrayList<>();
        Map<String, Map<Integer, List<TimetableEntryEntity>>> teacherSchedule = new HashMap<>();

        for (TimetableEntryEntity entry : entries) {
            if (entry.getIsBreak() != null && entry.getIsBreak()) continue;
            if (entry.getTeacher() == null) continue;

            String key = entry.getTeacher().getId() + "_" + entry.getDay() + "_" + entry.getWeekNumber();
            teacherSchedule.computeIfAbsent(key, k -> new HashMap<>())
                    .computeIfAbsent(entry.getPeriodNumber(), k -> new ArrayList<>())
                    .add(entry);
        }

        for (Map.Entry<String, Map<Integer, List<TimetableEntryEntity>>> entry : teacherSchedule.entrySet()) {
            for (Map.Entry<Integer, List<TimetableEntryEntity>> periodEntry : entry.getValue().entrySet()) {
                if (periodEntry.getValue().size() > 1) {
                    TimetableResponseDto.ConflictData conflict = new TimetableResponseDto.ConflictData();
                    conflict.setType("teacher");
                    conflict.setTeacherName(periodEntry.getValue().get(0).getTeacherName());
                    conflict.setTeacherId(periodEntry.getValue().get(0).getTeacher().getId());
                    conflict.setDay(periodEntry.getValue().get(0).getDay());
                    conflict.setPeriod(periodEntry.getKey());
                    conflict.setWeek(periodEntry.getValue().get(0).getWeekNumber());
                    conflict.setMessage("Teacher " + periodEntry.getValue().get(0).getTeacherName() +
                            " has multiple classes at same time");
                    conflicts.add(conflict);
                }
            }
        }

        return conflicts;
    }

    private List<TimetableResponseDto.ConflictData> checkRoomConflicts(List<TimetableEntryEntity> entries) {
        List<TimetableResponseDto.ConflictData> conflicts = new ArrayList<>();
        Map<String, Map<Integer, List<TimetableEntryEntity>>> roomSchedule = new HashMap<>();

        for (TimetableEntryEntity entry : entries) {
            if (entry.getIsBreak() != null && entry.getIsBreak()) continue;
            if (entry.getRoomNumber() == null || entry.getRoomNumber().isEmpty()) continue;

            String key = entry.getRoomNumber() + "_" + entry.getDay() + "_" + entry.getWeekNumber();
            roomSchedule.computeIfAbsent(key, k -> new HashMap<>())
                    .computeIfAbsent(entry.getPeriodNumber(), k -> new ArrayList<>())
                    .add(entry);
        }

        for (Map.Entry<String, Map<Integer, List<TimetableEntryEntity>>> entry : roomSchedule.entrySet()) {
            for (Map.Entry<Integer, List<TimetableEntryEntity>> periodEntry : entry.getValue().entrySet()) {
                if (periodEntry.getValue().size() > 1) {
                    TimetableResponseDto.ConflictData conflict = new TimetableResponseDto.ConflictData();
                    conflict.setType("room");
                    conflict.setRoomNumber(periodEntry.getValue().get(0).getRoomNumber());
                    conflict.setDay(periodEntry.getValue().get(0).getDay());
                    conflict.setPeriod(periodEntry.getKey());
                    conflict.setWeek(periodEntry.getValue().get(0).getWeekNumber());
                    conflict.setMessage("Room " + periodEntry.getValue().get(0).getRoomNumber() +
                            " is double-booked");
                    conflicts.add(conflict);
                }
            }
        }

        return conflicts;
    }

    private TimetableResponseDto.TimetableData mapToTimetableData(
            List<TimetableEntryEntity> entries,
            TimetableRequestDto request) {

        TimetableResponseDto.TimetableData data = new TimetableResponseDto.TimetableData();

        if (entries.isEmpty()) {
            return data;
        }

        // Group by week
        Map<Integer, List<TimetableEntryEntity>> byWeek = entries.stream()
                .collect(Collectors.groupingBy(TimetableEntryEntity::getWeekNumber));

        List<TimetableResponseDto.WeekData> weeks = new ArrayList<>();

        for (Map.Entry<Integer, List<TimetableEntryEntity>> weekEntry : byWeek.entrySet()) {
            TimetableResponseDto.WeekData weekData = new TimetableResponseDto.WeekData();
            weekData.setWeekNumber(weekEntry.getKey());

            // Group by day
            Map<String, List<TimetableEntryEntity>> byDay = weekEntry.getValue().stream()
                    .collect(Collectors.groupingBy(TimetableEntryEntity::getDay));

            List<TimetableResponseDto.DayData> days = new ArrayList<>();

            for (Map.Entry<String, List<TimetableEntryEntity>> dayEntry : byDay.entrySet()) {
                TimetableResponseDto.DayData dayData = new TimetableResponseDto.DayData();
                dayData.setDay(dayEntry.getKey());

                List<TimetableResponseDto.PeriodData> periods = dayEntry.getValue().stream()
                        .sorted(Comparator.comparing(TimetableEntryEntity::getPeriodNumber))
                        .map(this::mapToPeriodData)
                        .collect(Collectors.toList());

                dayData.setPeriods(periods);
                dayData.setTotalPeriods(8);
                dayData.setFilledPeriods((int) periods.stream()
                        .filter(p -> !p.getIsBreak()).count());

                days.add(dayData);
            }

            weekData.setDays(days);
            weeks.add(weekData);
        }

        data.setWeeks(weeks);

        // Set class info
        TimetableEntryEntity first = entries.get(0);
        data.setClassName(first.getClassEntity().getClassName());
        data.setClassCode(first.getClassEntity().getClassCode());
        data.setSection(first.getSection());
        data.setAcademicYear(first.getAcademicYear());

        // Set metadata
        TimetableResponseDto.Metadata metadata = new TimetableResponseDto.Metadata();
        metadata.setGeneratedAt(new Date());
        metadata.setTotalRecords(entries.size());

        if (request != null) {
            Map<String, String> filters = new HashMap<>();
            filters.put("class", request.getClassName());
            filters.put("section", request.getSection());
            filters.put("academicYear", request.getAcademicYear());
            if (request.getDay() != null) filters.put("day", request.getDay());
            if (request.getWeekNumber() != null) filters.put("week", String.valueOf(request.getWeekNumber()));
            metadata.setFilters(filters);
        }

        data.setMetadata(metadata);

        return data;
    }

    private TimetableResponseDto.PeriodData mapToPeriodData(TimetableEntryEntity entry) {
        TimetableResponseDto.PeriodData dto = new TimetableResponseDto.PeriodData();
        dto.setPeriod(entry.getPeriodNumber());
        dto.setIsBreak(entry.getIsBreak());
        dto.setTime(entry.getTimeSlot());
        dto.setNotes(entry.getNotes());

        if (entry.getIsBreak() != null && entry.getIsBreak()) {
            dto.setBreakType(entry.getBreakType());
        } else {
            dto.setSubjectName(entry.getSubjectName());
            dto.setSubjectCode(entry.getSubjectCode());

            // Use helper methods instead of direct entity access
            dto.setTeacherId(entry.getTeacherId());
            dto.setTeacherName(entry.getTeacherFullName() != null ?
                    entry.getTeacherFullName() : entry.getTeacherName());

            // Class info if needed
            // dto.setClassName(entry.getClassName());
            // dto.setClassCode(entry.getClassCode());

            dto.setRoomNumber(entry.getRoomNumber());
            dto.setRoomType(entry.getRoomType());
        }

        // Check for conflicts (you'll need to implement this)
        dto.setHasConflict(false);

        return dto;
    }
}