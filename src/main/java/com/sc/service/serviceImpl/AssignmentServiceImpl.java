package com.sc.service.serviceImpl;

import com.sc.dto.request.AssignmentRequestDto;
import com.sc.dto.request.BulkActionDto;
import com.sc.dto.request.GradeRequestDto;
import com.sc.dto.response.AssignmentResponseDto;
import com.sc.dto.response.SubmissionResponseDto;
import com.sc.entity.*;
import com.sc.enum_util.*;
import com.sc.repository.*;
import com.sc.service.AssignmentNotificationService;
import com.sc.service.AssignmentService;
import com.sc.util.AssignmentIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private AssignmentIdGenerator assignmentIdGenerator;

    @Autowired
    private AssignmentNotificationService notificationService;

    @Value("${file.upload-dir:uploads/assignments}")
    private String uploadDir;

    private void logInfo(String message) {
        System.out.println("[INFO] " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " - " + message);
    }

    private void logError(String message, Exception e) {
        System.err.println("[ERROR] " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " - " + message);
        if (e != null) e.printStackTrace();
    }

    // ============= ENUM CONVERSION METHODS =============

    private GradingType convertToGradingType(String gradingType) {
        if (gradingType == null) return GradingType.marks;
        try {
            return GradingType.valueOf(gradingType.toLowerCase());
        } catch (IllegalArgumentException e) {
            return GradingType.marks;
        }
    }

    private PriorityType convertToPriorityType(String priority) {
        if (priority == null) return PriorityType.medium;
        try {
            return PriorityType.valueOf(priority.toLowerCase());
        } catch (IllegalArgumentException e) {
            return PriorityType.medium;
        }
    }

    private AssignToType convertToAssignToType(String assignTo) {
        if (assignTo == null) return AssignToType.specific_class;
        try {
            return AssignToType.valueOf(assignTo.toLowerCase());
        } catch (IllegalArgumentException e) {
            return AssignToType.specific_class;
        }
    }

    private StatusType convertToStatusType(String status) {
        if (status == null) return StatusType.active;
        try {
            return StatusType.valueOf(status.toLowerCase());
        } catch (IllegalArgumentException e) {
            return StatusType.active;
        }
    }

    private PublishStatus convertToPublishStatus(String publishStatus) {
        if (publishStatus == null) return PublishStatus.DRAFT;
        try {
            return PublishStatus.valueOf(publishStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PublishStatus.DRAFT;
        }
    }

    // ============= CREATE ASSIGNMENT =============

    @Override
    @Transactional
    public AssignmentResponseDto createAssignment(AssignmentRequestDto requestDto) {
        logInfo("Creating assignment: " + requestDto.getTitle());

        try {
            // 🔴 STEP 1: Validate classId
            if (requestDto.getClassId() == null) {
                throw new IllegalArgumentException("classId is required");
            }

            // 🔴 STEP 2: Fetch class from database
            ClassEntity classEntity = classRepository.findById(requestDto.getClassId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Class not found with ID: " + requestDto.getClassId()));

            // 🔴 STEP 3: Validate teacherId
            if (requestDto.getCreatedByTeacherId() == null) {
                throw new IllegalArgumentException("teacherId is required");
            }

            // 🔴 STEP 4: Fetch teacher from database
            TeacherEntity teacher = teacherRepository.findById(requestDto.getCreatedByTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Teacher not found with ID: " + requestDto.getCreatedByTeacherId()));

            // 🔴 STEP 5: Convert DTO to Entity
            AssignmentEntity assignment = convertToEntity(requestDto);

            // 🔴 STEP 6: Set relationships
            assignment.setTargetClass(classEntity);      // class set कर रहे हैं
            assignment.setCreatedByTeacher(teacher);      // teacher set कर रहे हैं

            // 🔴 STEP 7: Generate assignment code - NOW USING THE METHOD
            assignment.setAssignmentCode(generateAssignmentCode());

            // 🔴 STEP 8: Handle publish status
            handlePublishStatus(assignment, requestDto);

            // 🔴 STEP 9: Save to database
            AssignmentEntity savedAssignment = assignmentRepository.save(assignment);

            logInfo("✅ Assignment saved with ID: " + savedAssignment.getAssignmentId() +
                    " for Class ID: " + classEntity.getClassId() +
                    " with Code: " + savedAssignment.getAssignmentCode());

            // 🔴 STEP 10: Send notifications if published
            if (savedAssignment.getPublishStatus() == PublishStatus.PUBLISHED) {
                sendNewAssignmentNotifications(savedAssignment);
            }

            return convertToDto(savedAssignment);

        } catch (Exception e) {
            logError("Error creating assignment: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create assignment: " + e.getMessage());
        }
    }

    // ============= 🔴 FIXED: ASSIGNMENT CODE GENERATION METHOD =============

    /**
     * Generate a unique assignment code
     * Format: ASM-YYYYMMDD-XXXXX (e.g., ASM-20240307-ABC12)
     */
    private String generateAssignmentCode() {
        String prefix = "ASM";
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = generateRandomString(5);
        String code = prefix + "-" + datePart + "-" + randomPart;

        // Check if code already exists and regenerate if needed
        int attempts = 0;
        while (assignmentRepository.findByAssignmentCode(code) != null && attempts < 10) {
            randomPart = generateRandomString(5);
            code = prefix + "-" + datePart + "-" + randomPart;
            attempts++;
        }

        return code;
    }

    /**
     * Generate random alphanumeric string
     */
    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * Alternative: Use AssignmentIdGenerator if available
     */
    private String generateAssignmentCodeUsingGenerator() {
        return assignmentIdGenerator.generateUniqueAssignmentCode();
    }

    private void handlePublishStatus(AssignmentEntity assignment, AssignmentRequestDto dto) {
        if (dto.getPublishNow() != null && dto.getPublishNow()) {
            assignment.setPublishStatus(PublishStatus.PUBLISHED);
            assignment.setPublishedDate(LocalDateTime.now());
            if (dto.getCreatedByTeacherId() != null) {
                teacherRepository.findById(dto.getCreatedByTeacherId()).ifPresent(teacher ->
                        assignment.setPublishedBy(teacher.getFullName()));
            }
            return;
        }

        if (dto.getScheduledPublishDate() != null) {
            assignment.setPublishStatus(PublishStatus.SCHEDULED);
            assignment.setScheduledPublishDate(dto.getScheduledPublishDate());
            return;
        }

        if (dto.getPublishStatus() != null) {
            PublishStatus status = convertToPublishStatus(dto.getPublishStatus());
            assignment.setPublishStatus(status);

            if (status == PublishStatus.PUBLISHED) {
                assignment.setPublishedDate(LocalDateTime.now());
                if (dto.getCreatedByTeacherId() != null) {
                    teacherRepository.findById(dto.getCreatedByTeacherId()).ifPresent(teacher ->
                            assignment.setPublishedBy(teacher.getFullName()));
                }
            }

            if (status == PublishStatus.SCHEDULED && dto.getScheduledPublishDate() != null) {
                assignment.setScheduledPublishDate(dto.getScheduledPublishDate());
            }
            return;
        }

        assignment.setPublishStatus(PublishStatus.DRAFT);
    }

    // ============= SCHEDULED PUBLISHING =============

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void checkScheduledPublishing() {
        logInfo("Checking for assignments scheduled to publish...");
        LocalDateTime now = LocalDateTime.now();
        List<AssignmentEntity> scheduledAssignments = assignmentRepository.findScheduledForPublishing(now);

        if (!scheduledAssignments.isEmpty()) {
            logInfo("Found " + scheduledAssignments.size() + " assignments to publish");
            for (AssignmentEntity assignment : scheduledAssignments) {
                try {
                    assignment.setPublishStatus(PublishStatus.PUBLISHED);
                    assignment.setPublishedDate(now);
                    assignment.setPublishedBy("System (Scheduled)");
                    assignmentRepository.save(assignment);
                    sendNewAssignmentNotifications(assignment);
                    logInfo("Published scheduled assignment: " + assignment.getAssignmentCode());
                } catch (Exception e) {
                    logError("Failed to publish scheduled assignment " + assignment.getAssignmentId(), e);
                }
            }
        }
    }

    // ============= PUBLISH ASSIGNMENT =============

    @Override
    @Transactional
    public AssignmentResponseDto publishAssignment(Long assignmentId, Long teacherId) {
        logInfo("Manually publishing assignment ID: " + assignmentId);
        AssignmentEntity assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (assignment.getPublishStatus() == PublishStatus.PUBLISHED) {
            throw new IllegalStateException("Assignment is already published");
        }

        TeacherEntity teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        assignment.setPublishStatus(PublishStatus.PUBLISHED);
        assignment.setPublishedDate(LocalDateTime.now());
        assignment.setPublishedBy(teacher.getFullName());

        AssignmentEntity updated = assignmentRepository.save(assignment);
        sendNewAssignmentNotifications(updated);
        return convertToDto(updated);
    }

    // ============= GET DRAFT ASSIGNMENTS =============

    @Override
    public List<AssignmentResponseDto> getDraftAssignments(Long teacherId) {
        logInfo("Getting draft assignments for teacher: " + teacherId);
        List<AssignmentEntity> drafts = assignmentRepository.findByTeacherAndPublishStatus(teacherId, PublishStatus.DRAFT);
        return drafts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    // ============= GET SCHEDULED ASSIGNMENTS =============

    @Override
    public List<AssignmentResponseDto> getScheduledAssignments(Long teacherId) {
        logInfo("Getting scheduled assignments for teacher: " + teacherId);
        List<AssignmentEntity> scheduled = assignmentRepository.findByTeacherAndPublishStatus(teacherId, PublishStatus.SCHEDULED);
        return scheduled.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    // ============= GET ALL ASSIGNMENTS =============

    @Override
    public Page<AssignmentResponseDto> getAllAssignments(Pageable pageable) {
        return assignmentRepository.findAll(pageable).map(this::convertToDtoWithStats);
    }

    // ============= GET ASSIGNMENT BY ID =============

    @Override
    public AssignmentResponseDto getAssignmentById(Long id) {
        return assignmentRepository.findById(id).map(this::convertToDtoWithStats).orElse(null);
    }

    // ============= GET ASSIGNMENT BY CODE =============

    @Override
    public AssignmentResponseDto getAssignmentByCode(String assignmentCode) {
        AssignmentEntity assignment = assignmentRepository.findByAssignmentCode(assignmentCode);
        return assignment != null ? convertToDtoWithStats(assignment) : null;
    }

    // ============= GET ASSIGNMENTS BY STATUS =============

    @Override
    public List<AssignmentResponseDto> getAssignmentsByStatus(String status) {
        StatusType statusType = convertToStatusType(status);
        return assignmentRepository.findByStatus(statusType).stream()
                .map(this::convertToDto).collect(Collectors.toList());
    }

    // ============= GET ASSIGNMENTS BY CLASS =============

    @Override
    public List<AssignmentResponseDto> getAssignmentsByClass(String className) {
        return assignmentRepository.findByClassNameAndSection(className, "All Sections").stream()
                .map(this::convertToDto).collect(Collectors.toList());
    }

    // ============= GET ASSIGNMENTS BY CLASS AND SECTION =============

    @Override
    public List<AssignmentResponseDto> getAssignmentsByClassAndSection(String className, String section) {
        return assignmentRepository.findByClassNameAndSection(className, section).stream()
                .map(this::convertToDto).collect(Collectors.toList());
    }

    // ============= GET ASSIGNMENTS BY SUBJECT =============

    @Override
    public List<AssignmentResponseDto> getAssignmentsBySubject(String subject) {
        return assignmentRepository.findBySubject(subject).stream()
                .map(this::convertToDto).collect(Collectors.toList());
    }

    // ============= GET OVERDUE ASSIGNMENTS =============

    @Override
    public List<AssignmentResponseDto> getOverdueAssignments() {
        return assignmentRepository.findOverdueAssignments(LocalDateTime.now()).stream()
                .map(this::convertToDto).collect(Collectors.toList());
    }

    // ============= SEARCH ASSIGNMENTS =============

    @Override
    public Page<AssignmentResponseDto> searchAssignments(
            String subject, String className, String status, String priority,
            LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {

        StatusType statusType = null;
        if (status != null && !status.isEmpty()) {
            statusType = convertToStatusType(status);
        }

        return assignmentRepository.searchAssignments(
                        subject, className, statusType, fromDate, toDate, pageable)
                .map(this::convertToDtoWithStats);
    }

    // ============= UPDATE ASSIGNMENT =============

    @Override
    @Transactional
    public AssignmentResponseDto updateAssignment(Long id, AssignmentRequestDto requestDto) {
        return assignmentRepository.findById(id)
                .map(assignment -> {
                    updateAssignmentFields(assignment, requestDto);

                    if (requestDto.getCreatedByTeacherId() != null) {
                        TeacherEntity teacher = teacherRepository.findById(requestDto.getCreatedByTeacherId())
                                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
                        assignment.setCreatedByTeacher(teacher);
                    }

                    if (requestDto.getPublishStatus() != null) {
                        handlePublishStatus(assignment, requestDto);
                    }

                    assignment.setUpdatedAt(LocalDateTime.now());
                    AssignmentEntity updated = assignmentRepository.save(assignment);

                    if (assignment.getPublishStatus() == PublishStatus.PUBLISHED &&
                            requestDto.getPublishNow() != null && requestDto.getPublishNow()) {
                        sendNewAssignmentNotifications(updated);
                    }

                    return convertToDto(updated);
                })
                .orElse(null);
    }

    // ============= UPDATE ASSIGNMENT PARTIAL =============

    @Override
    @Transactional
    public AssignmentResponseDto updateAssignmentPartial(Long id, Map<String, Object> updates) {
        return assignmentRepository.findById(id)
                .map(assignment -> {
                    updates.forEach((key, value) -> {
                        switch (key) {
                            case "title": assignment.setTitle((String) value); break;
                            case "description": assignment.setDescription((String) value); break;
                            case "dueDate": assignment.setDueDate(LocalDateTime.parse((String) value)); break;
                            case "gradingType": assignment.setGradingType(convertToGradingType((String) value)); break;
                            case "priority": assignment.setPriority(convertToPriorityType((String) value)); break;
                            case "assignTo": assignment.setAssignTo(convertToAssignToType((String) value)); break;
                            case "status": assignment.setStatus(convertToStatusType((String) value)); break;
                            case "totalMarks": assignment.setTotalMarks((Integer) value); break;
                            case "publishStatus":
                                assignment.setPublishStatus(convertToPublishStatus((String) value));
                                if (assignment.getPublishStatus() == PublishStatus.PUBLISHED) {
                                    assignment.setPublishedDate(LocalDateTime.now());
                                }
                                break;
                        }
                    });

                    assignment.setUpdatedAt(LocalDateTime.now());
                    AssignmentEntity updated = assignmentRepository.save(assignment);
                    return convertToDto(updated);
                })
                .orElse(null);
    }

    // ============= UPDATE ASSIGNMENT STATUS =============

    @Override
    @Transactional
    public AssignmentResponseDto updateAssignmentStatus(Long id, String status) {
        return assignmentRepository.findById(id)
                .map(assignment -> {
                    assignment.setStatus(convertToStatusType(status));
                    assignment.setUpdatedAt(LocalDateTime.now());
                    AssignmentEntity updated = assignmentRepository.save(assignment);
                    return convertToDto(updated);
                })
                .orElse(null);
    }

    // ============= DELETE ASSIGNMENT =============

    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Assignment not found with ID: " + id);
        }
        submissionRepository.deleteByAssignment_AssignmentId(id);
        assignmentRepository.deleteById(id);
        logInfo("Assignment deleted successfully with ID: " + id);
    }

    // ============= BULK DELETE =============

    @Override
    @Transactional
    public void bulkDeleteAssignments(List<Long> assignmentIds) {
        for (Long id : assignmentIds) {
            submissionRepository.deleteByAssignment_AssignmentId(id);
            assignmentRepository.deleteById(id);
        }
        logInfo("Bulk deleted " + assignmentIds.size() + " assignments");
    }

    // ============= BULK UPDATE STATUS =============

    @Override
    @Transactional
    public List<AssignmentResponseDto> bulkUpdateStatus(List<Long> assignmentIds, String status) {
        StatusType statusType = convertToStatusType(status);
        List<AssignmentEntity> assignments = assignmentRepository.findAllById(assignmentIds);
        assignments.forEach(assignment -> {
            assignment.setStatus(statusType);
            assignment.setUpdatedAt(LocalDateTime.now());
        });
        List<AssignmentEntity> updated = assignmentRepository.saveAll(assignments);
        return updated.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    // ============= EXECUTE BULK ACTION =============

    @Override
    @Transactional
    public Map<String, Object> executeBulkAction(BulkActionDto bulkActionDto) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Long> ids = bulkActionDto.getAssignmentIds();
            String actionType = bulkActionDto.getActionType();

            switch (actionType) {
                case "change_status":
                    List<AssignmentResponseDto> updated = bulkUpdateStatus(ids, bulkActionDto.getNewStatus());
                    result.put("updatedCount", updated.size());
                    result.put("message", "Status updated successfully");
                    break;
                case "delete":
                    bulkDeleteAssignments(ids);
                    result.put("deletedCount", ids.size());
                    result.put("message", "Assignments deleted successfully");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action type: " + actionType);
            }
            result.put("success", true);
        } catch (Exception e) {
            logError("Error executing bulk action", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ============= GET ASSIGNMENT STATISTICS =============

    @Override
    public Map<String, Object> getAssignmentStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        long totalAssignments = assignmentRepository.count();
        statistics.put("totalAssignments", totalAssignments);

        Map<String, Long> byStatus = getCountByStatus();
        statistics.put("assignmentsByStatus", byStatus);

        long activeAssignments = byStatus.getOrDefault("active", 0L);
        statistics.put("activeAssignments", activeAssignments);

        long completedAssignments = byStatus.getOrDefault("completed", 0L);
        statistics.put("completedAssignments", completedAssignments);

        long overdueAssignments = assignmentRepository.findOverdueAssignments(LocalDateTime.now()).size();
        statistics.put("overdueAssignments", overdueAssignments);

        long draftAssignments = assignmentRepository.findByPublishStatus(PublishStatus.DRAFT).size();
        statistics.put("draftAssignments", draftAssignments);

        long scheduledAssignments = assignmentRepository.findByPublishStatus(PublishStatus.SCHEDULED).size();
        statistics.put("scheduledAssignments", scheduledAssignments);

        long totalSubmissions = submissionRepository.count();
        statistics.put("totalSubmissions", totalSubmissions);

        return statistics;
    }

    // ============= GET COUNT BY STATUS =============

    @Override
    public Map<String, Long> getCountByStatus() {
        List<Object[]> results = assignmentRepository.countByStatus();
        Map<String, Long> countMap = new HashMap<>();
        for (Object[] result : results) {
            StatusType status = (StatusType) result[0];
            Long count = (Long) result[1];
            countMap.put(status.name(), count);
        }
        return countMap;
    }

    // ============= GET ASSIGNMENT ANALYTICS =============

    @Override
    public Map<String, Object> getAssignmentAnalytics(Long id) {
        AssignmentEntity assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("assignmentId", assignment.getAssignmentId());
        analytics.put("title", assignment.getTitle());
        analytics.put("publishStatus", assignment.getPublishStatus());
        analytics.put("publishedDate", assignment.getPublishedDate());

        List<Object[]> stats = submissionRepository.getSubmissionStats(id);
        if (stats != null && !stats.isEmpty()) {
            Object[] row = stats.get(0);
            analytics.put("totalStudents", getTotalStudentsForAssignment(assignment));
            analytics.put("submittedCount", row[1] != null ? row[1] : 0);
            analytics.put("lateCount", row[2] != null ? row[2] : 0);
            analytics.put("gradedCount", row[3] != null ? row[3] : 0);
            analytics.put("averageScore", row[4] != null ? Math.round((Double) row[4] * 100) / 100.0 : 0);
        }

        LocalDateTime now = LocalDateTime.now();
        if (assignment.getDueDate().isBefore(now)) {
            analytics.put("dueStatus", "overdue");
        } else if (assignment.getDueDate().minusDays(2).isBefore(now)) {
            analytics.put("dueStatus", "dueSoon");
        } else {
            analytics.put("dueStatus", "normal");
        }

        return analytics;
    }

    // ============= GRADE SUBMISSION =============

    @Override
    @Transactional
    public SubmissionResponseDto gradeSubmission(GradeRequestDto gradeRequest) {
        logInfo("Grading submission for assignment: " + gradeRequest.getAssignmentId());

        SubmissionEntity submission = submissionRepository
                .findByAssignment_AssignmentIdAndStudent_StdId(
                        gradeRequest.getAssignmentId(), gradeRequest.getStudentId())
                .orElseGet(() -> createNewSubmission(gradeRequest));

        AssignmentEntity assignment = submission.getAssignment();

        if (gradeRequest.getObtainedMarks() != null) {
            if (gradeRequest.getObtainedMarks() < 0 ||
                    gradeRequest.getObtainedMarks() > assignment.getTotalMarks()) {
                throw new IllegalArgumentException("Marks must be between 0 and " + assignment.getTotalMarks());
            }
            submission.setObtainedMarks(gradeRequest.getObtainedMarks());
        }

        if (gradeRequest.getGrade() != null && !gradeRequest.getGrade().isEmpty()) {
            submission.setGrade(gradeRequest.getGrade());
        }

        submission.setTeacherFeedback(gradeRequest.getTeacherFeedback());
        submission.setStatus("graded");

        SubmissionEntity saved = submissionRepository.save(submission);

        try {
            notificationService.markAssignmentActionCompleted(gradeRequest.getStudentId(), gradeRequest.getAssignmentId());
        } catch (Exception e) {
            logError("Failed to mark notifications completed", e);
        }

        return convertToSubmissionDto(saved);
    }

    private SubmissionEntity createNewSubmission(GradeRequestDto gradeRequest) {
        SubmissionEntity submission = new SubmissionEntity();
        AssignmentEntity assignment = assignmentRepository.findById(gradeRequest.getAssignmentId())
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        StudentEntity student = studentRepository.findById(gradeRequest.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setStatus("submitted");
        submission.setSubmittedDate(LocalDateTime.now());
        submission.setIsLate(LocalDateTime.now().isAfter(assignment.getDueDate()));

        return submission;
    }

    // ============= GET SUBMISSIONS BY ASSIGNMENT =============

    @Override
    public List<SubmissionResponseDto> getSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignment_AssignmentId(assignmentId).stream()
                .map(this::convertToSubmissionDto)
                .collect(Collectors.toList());
    }

    // ============= GET SUBMISSIONS BY STUDENT =============

    @Override
    public List<SubmissionResponseDto> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudent_StdId(studentId).stream()
                .map(this::convertToSubmissionDto)
                .collect(Collectors.toList());
    }

    // ============= GET SUBMISSION =============

    @Override
    public SubmissionResponseDto getSubmission(Long assignmentId, Long studentId) {
        return submissionRepository
                .findByAssignment_AssignmentIdAndStudent_StdId(assignmentId, studentId)
                .map(this::convertToSubmissionDto)
                .orElse(null);
    }

    // ============= GET SUBMISSION STATS =============

    @Override
    public Map<String, Object> getSubmissionStats(Long assignmentId) {
        Map<String, Object> stats = new HashMap<>();
        List<Object[]> results = submissionRepository.getSubmissionStats(assignmentId);
        if (results != null && !results.isEmpty()) {
            Object[] row = results.get(0);
            stats.put("total", row[0] != null ? row[0] : 0);
            stats.put("submitted", row[1] != null ? row[1] : 0);
            stats.put("late", row[2] != null ? row[2] : 0);
            stats.put("graded", row[3] != null ? row[3] : 0);
            stats.put("average", row[4] != null ? Math.round((Double) row[4] * 100) / 100.0 : 0);
        }
        return stats;
    }

    // ============= SEND REMINDERS =============

    @Override
    public void sendReminders(Long assignmentId, String reminderType, String customMessage) {
        logInfo("Sending " + reminderType + " reminders for assignment: " + assignmentId);
        AssignmentEntity assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        List<StudentEntity> students = studentRepository.findStudentsByClassAndSection(
                assignment.getClassName(),
                assignment.getSection().equals("All Sections") ? null : assignment.getSection()
        );

        List<Long> submittedStudentIds = submissionRepository
                .findByAssignment_AssignmentId(assignmentId)
                .stream()
                .map(s -> s.getStudent().getStdId())
                .collect(Collectors.toList());

        int reminderCount = 0;
        for (StudentEntity student : students) {
            if (!submittedStudentIds.contains(student.getStdId())) {
                // Send reminder logic here
                reminderCount++;
            }
        }
        logInfo("Sent " + reminderCount + " reminders for assignment " + assignmentId);
    }

    @Override
    public void sendBulkReminders(List<Long> assignmentIds, String reminderType, String customMessage) {
        for (Long assignmentId : assignmentIds) {
            try {
                sendReminders(assignmentId, reminderType, customMessage);
            } catch (Exception e) {
                logError("Failed to send reminder for assignment " + assignmentId, e);
            }
        }
    }

    // ============= FIXED STUDENT SPECIFIC METHODS =============

    @Override
    public List<AssignmentResponseDto> getAssignmentsForStudent(Long studentId) {
        logInfo("Getting assignments for student ID: " + studentId);

        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        // Use the correct method with 2 parameters (no LocalDateTime)
        List<AssignmentEntity> assignments = assignmentRepository
                .findActiveAssignmentsForStudent(
                        student.getCurrentClass(),
                        student.getSection());

        logInfo("Found " + assignments.size() + " active assignments for student");

        return assignments.stream()
                .map(this::convertToDtoWithStats)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentResponseDto> getPendingAssignmentsForStudent(Long studentId) {
        logInfo("Getting pending assignments for student ID: " + studentId);

        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        // Get all assignments for the student
        List<AssignmentEntity> allAssignments = assignmentRepository
                .findAllAssignmentsForStudent(
                        student.getCurrentClass(),
                        student.getSection());

        // Get submissions for this student
        List<SubmissionEntity> submissions = submissionRepository.findByStudent_StdId(studentId);
        Set<Long> submittedAssignmentIds = submissions.stream()
                .map(s -> s.getAssignment().getAssignmentId())
                .collect(Collectors.toSet());

        // Filter for pending assignments (not submitted)
        List<AssignmentResponseDto> pending = allAssignments.stream()
                .filter(assignment -> !submittedAssignmentIds.contains(assignment.getAssignmentId()))
                .filter(assignment -> assignment.getDueDate() != null &&
                        assignment.getDueDate().isAfter(LocalDateTime.now())) // Only future assignments
                .map(this::convertToDtoWithStats)
                .collect(Collectors.toList());

        logInfo("Found " + pending.size() + " pending assignments for student");
        return pending;
    }

    @Override
    public List<AssignmentResponseDto> getCompletedAssignmentsForStudent(Long studentId) {
        logInfo("Getting completed assignments for student ID: " + studentId);

        // Get submissions for this student
        List<SubmissionEntity> submissions = submissionRepository.findByStudent_StdId(studentId);

        List<AssignmentResponseDto> completed = new ArrayList<>();
        for (SubmissionEntity submission : submissions) {
            if ("graded".equals(submission.getStatus()) || "submitted".equals(submission.getStatus())) {
                AssignmentResponseDto assignment = getAssignmentById(submission.getAssignment().getAssignmentId());
                if (assignment != null) {
                    completed.add(assignment);
                }
            }
        }

        logInfo("Found " + completed.size() + " completed assignments for student");
        return completed;
    }

    @Override
    public List<AssignmentResponseDto> getAssignmentsByTeacher(Long teacherId) {
        logInfo("Getting assignments for teacher ID: " + teacherId);

        if (!teacherRepository.existsById(teacherId)) {
            throw new IllegalArgumentException("Teacher not found with ID: " + teacherId);
        }

        List<AssignmentEntity> assignments = assignmentRepository.findByCreatedByTeacher_Id(teacherId);
        logInfo("Found " + assignments.size() + " assignments for teacher");

        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ============= ATTACHMENT METHODS =============

    @Override
    @Transactional
    public AssignmentResponseDto uploadAttachments(Long id, List<MultipartFile> files) {
        AssignmentEntity assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        try {
            processAttachments(assignment, files);
            AssignmentEntity updated = assignmentRepository.save(assignment);
            return convertToDto(updated);
        } catch (IOException e) {
            logError("Error uploading attachments", e);
            throw new RuntimeException("Failed to upload attachments");
        }
    }

    @Override
    public byte[] getAttachment(Long id, String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(id.toString()).resolve(fileName);
            if (Files.exists(filePath)) {
                return Files.readAllBytes(filePath);
            }
            return null;
        } catch (IOException e) {
            logError("Error reading attachment", e);
            return null;
        }
    }

    // ============= PRIVATE HELPER METHODS =============

    private void sendNewAssignmentNotifications(AssignmentEntity assignment) {
        try {
            List<Long> studentIds = getAllStudentIdsForAssignment(assignment);
            CompletableFuture.runAsync(() -> {
                notificationService.notifyStudentsAboutNewAssignment(assignment, studentIds);
            });
            logInfo("Triggered notifications for " + studentIds.size() + " students");
        } catch (Exception e) {
            logError("Failed to send notifications", e);
        }
    }

    private List<Long> getAllStudentIdsForAssignment(AssignmentEntity assignment) {
        List<StudentEntity> students = studentRepository.findStudentsByClassAndSection(
                assignment.getClassName(),
                assignment.getSection().equals("All Sections") ? null : assignment.getSection()
        );
        return students.stream().map(StudentEntity::getStdId).collect(Collectors.toList());
    }

    private int getTotalStudentsForAssignment(AssignmentEntity assignment) {
        List<StudentEntity> students = studentRepository.findStudentsByClassAndSection(
                assignment.getClassName(),
                assignment.getSection().equals("All Sections") ? null : assignment.getSection()
        );
        return students.size();
    }

    private void processAttachments(AssignmentEntity assignment, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) return;

        List<String> fileNames = new ArrayList<>();
        Path assignmentDir = Paths.get(uploadDir).resolve(assignment.getAssignmentId().toString());

        if (!Files.exists(assignmentDir)) {
            Files.createDirectories(assignmentDir);
        }

        for (MultipartFile file : files) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = assignmentDir.resolve(fileName);
            Files.write(filePath, file.getBytes());
            fileNames.add(fileName);
        }

        assignment.setAttachments(fileNames);
    }

    // ============= CONVERSION METHODS =============

    private AssignmentEntity convertToEntity(AssignmentRequestDto dto) {
        AssignmentEntity entity = new AssignmentEntity();
        entity.setTitle(dto.getTitle());
        entity.setSubject(dto.getSubject());
        entity.setClassName(dto.getClassName());
        entity.setSection(dto.getSection());
        entity.setDescription(dto.getDescription());
        entity.setGradingType(convertToGradingType(dto.getGradingType()));
        entity.setTotalMarks(dto.getTotalMarks());
        entity.setStartDate(dto.getStartDate());
        entity.setDueDate(dto.getDueDate());
        entity.setAllowLateSubmission(dto.getAllowLateSubmission());
        entity.setAllowResubmission(dto.getAllowResubmission());
        entity.setPriority(convertToPriorityType(dto.getPriority()));
        entity.setAssignTo(convertToAssignToType(dto.getAssignTo()));

        if (dto.getAssignedClasses() != null) {
            entity.setAssignedClasses(dto.getAssignedClasses());
        }
        if (dto.getAssignedStudents() != null) {
            entity.setAssignedStudents(dto.getAssignedStudents());
        }

        entity.setExternalLink(dto.getExternalLink());
        entity.setNotifyStudents(dto.getNotifyStudents());
        entity.setNotifyParents(dto.getNotifyParents());
        entity.setSendReminders(dto.getSendReminders());
        entity.setSendLateWarnings(dto.getSendLateWarnings());
        entity.setStatus(convertToStatusType(dto.getStatus()));
        entity.setAcademicYear(dto.getAcademicYear());
        entity.setTerm(dto.getTerm());

        return entity;
    }

    private void updateAssignmentFields(AssignmentEntity entity, AssignmentRequestDto dto) {
        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getSubject() != null) entity.setSubject(dto.getSubject());
        if (dto.getClassName() != null) entity.setClassName(dto.getClassName());
        if (dto.getSection() != null) entity.setSection(dto.getSection());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getGradingType() != null) entity.setGradingType(convertToGradingType(dto.getGradingType()));
        if (dto.getTotalMarks() != null) entity.setTotalMarks(dto.getTotalMarks());
        if (dto.getStartDate() != null) entity.setStartDate(dto.getStartDate());
        if (dto.getDueDate() != null) entity.setDueDate(dto.getDueDate());
        if (dto.getAllowLateSubmission() != null) entity.setAllowLateSubmission(dto.getAllowLateSubmission());
        if (dto.getAllowResubmission() != null) entity.setAllowResubmission(dto.getAllowResubmission());
        if (dto.getPriority() != null) entity.setPriority(convertToPriorityType(dto.getPriority()));
        if (dto.getAssignTo() != null) entity.setAssignTo(convertToAssignToType(dto.getAssignTo()));
        if (dto.getAssignedClasses() != null) entity.setAssignedClasses(dto.getAssignedClasses());
        if (dto.getAssignedStudents() != null) entity.setAssignedStudents(dto.getAssignedStudents());
        if (dto.getExternalLink() != null) entity.setExternalLink(dto.getExternalLink());
        if (dto.getNotifyStudents() != null) entity.setNotifyStudents(dto.getNotifyStudents());
        if (dto.getNotifyParents() != null) entity.setNotifyParents(dto.getNotifyParents());
        if (dto.getSendReminders() != null) entity.setSendReminders(dto.getSendReminders());
        if (dto.getSendLateWarnings() != null) entity.setSendLateWarnings(dto.getSendLateWarnings());
        if (dto.getStatus() != null) entity.setStatus(convertToStatusType(dto.getStatus()));
        if (dto.getAcademicYear() != null) entity.setAcademicYear(dto.getAcademicYear());
        if (dto.getTerm() != null) entity.setTerm(dto.getTerm());
    }

    private AssignmentResponseDto convertToDto(AssignmentEntity entity) {
        AssignmentResponseDto dto = new AssignmentResponseDto();
        dto.setAssignmentId(entity.getAssignmentId());
        dto.setAssignmentCode(entity.getAssignmentCode());
        dto.setTitle(entity.getTitle());
        dto.setSubject(entity.getSubject());
        dto.setClassName(entity.getClassName());
        dto.setSection(entity.getSection());
        dto.setDescription(entity.getDescription());
        dto.setGradingType(entity.getGradingType());
        dto.setTotalMarks(entity.getTotalMarks());
        dto.setStartDate(entity.getStartDate());
        dto.setDueDate(entity.getDueDate());
        dto.setAllowLateSubmission(entity.getAllowLateSubmission());
        dto.setAllowResubmission(entity.getAllowResubmission());
        dto.setPriority(entity.getPriority());
        dto.setAssignTo(entity.getAssignTo());
        dto.setPublishStatus(entity.getPublishStatus());
        dto.setScheduledPublishDate(entity.getScheduledPublishDate());
        dto.setPublishedDate(entity.getPublishedDate());
        dto.setPublishedBy(entity.getPublishedBy());
        dto.setAssignedClasses(new ArrayList<>(entity.getAssignedClasses()));
        dto.setAssignedStudents(new ArrayList<>(entity.getAssignedStudents()));
        dto.setAttachments(new ArrayList<>(entity.getAttachments()));
        dto.setExternalLink(entity.getExternalLink());
        dto.setNotifyStudents(entity.getNotifyStudents());
        dto.setNotifyParents(entity.getNotifyParents());
        dto.setSendReminders(entity.getSendReminders());
        dto.setSendLateWarnings(entity.getSendLateWarnings());
        dto.setStatus(entity.getStatus());

        if (entity.getCreatedByTeacher() != null) {
            dto.setCreatedByTeacherId(entity.getCreatedByTeacher().getId());
            dto.setCreatedByName(entity.getCreatedByName());
        }

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setAcademicYear(entity.getAcademicYear());
        dto.setTerm(entity.getTerm());

        List<String> urls = entity.getAttachments().stream()
                .map(file -> "/api/assignments/" + entity.getAssignmentId() + "/attachments/" + file)
                .collect(Collectors.toList());
        dto.setAttachmentUrls(urls);

        return dto;
    }

    private AssignmentResponseDto convertToDtoWithStats(AssignmentEntity entity) {
        AssignmentResponseDto dto = convertToDto(entity);

        if (entity.getPublishStatus() == PublishStatus.PUBLISHED) {
            Long assignmentId = entity.getAssignmentId();
            long totalStudents = getTotalStudentsForAssignment(entity);
            long submitted = submissionRepository.countByAssignment_AssignmentIdAndStatus(assignmentId, "submitted") +
                    submissionRepository.countByAssignment_AssignmentIdAndStatus(assignmentId, "graded");
            long late = submissionRepository.countByAssignment_AssignmentIdAndIsLateTrue(assignmentId);
            long graded = submissionRepository.countByAssignment_AssignmentIdAndStatus(assignmentId, "graded");

            dto.setTotalStudents((int) totalStudents);
            dto.setSubmittedCount((int) submitted);
            dto.setPendingCount((int) (totalStudents - submitted));
            dto.setLateCount((int) late);
            dto.setGradedCount((int) graded);

            if (totalStudents > 0) {
                double rate = (submitted * 100.0) / totalStudents;
                dto.setSubmissionRate(Math.round(rate * 100) / 100.0);
            }

            List<Object[]> stats = submissionRepository.getSubmissionStats(assignmentId);
            if (stats != null && !stats.isEmpty() && stats.get(0)[4] != null) {
                dto.setAverageScore((Double) stats.get(0)[4]);
            }
        }

        dto.setIsVisible(entity.isVisibleToStudents());
        return dto;
    }

    private SubmissionResponseDto convertToSubmissionDto(SubmissionEntity entity) {
        SubmissionResponseDto dto = new SubmissionResponseDto();
        dto.setSubmissionId(entity.getSubmissionId());
        dto.setAssignmentId(entity.getAssignment().getAssignmentId());
        dto.setAssignmentTitle(entity.getAssignment().getTitle());

        if (entity.getStudent() != null) {
            dto.setStudentId(entity.getStudent().getStdId());
            dto.setStudentName(entity.getStudentName());
            dto.setRollNumber(entity.getRollNumber());
            dto.setStudentClass(entity.getStudentClass());
            dto.setStudentSection(entity.getStudentSection());
        }

        dto.setSubmittedDate(entity.getSubmittedDate());
        dto.setFiles(new ArrayList<>(entity.getFiles()));
        dto.setStatus(entity.getStatus());
        dto.setObtainedMarks(entity.getObtainedMarks());
        dto.setGrade(entity.getGrade());
        dto.setTeacherFeedback(entity.getTeacherFeedback());
        dto.setIsLate(entity.getIsLate());
        dto.setIsResubmission(entity.getIsResubmission());
        dto.setRecheckRequested(entity.getRecheckRequested());
        dto.setCreatedAt(entity.getCreatedAt());

        return dto;
    }
}