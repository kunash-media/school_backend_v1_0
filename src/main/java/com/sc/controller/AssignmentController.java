package com.sc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sc.dto.request.AssignmentRequestDto;
import com.sc.dto.request.BulkActionDto;
import com.sc.dto.request.GradeRequestDto;
import com.sc.dto.response.ApiResponse;
import com.sc.dto.response.AssignmentResponseDto;
import com.sc.dto.response.SubmissionResponseDto;
import com.sc.enum_util.AssignToType;
import com.sc.enum_util.GradingType;
import com.sc.enum_util.PriorityType;
import com.sc.enum_util.StatusType;
import com.sc.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500", "http://localhost:3000"})
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private void logInfo(String message) {
        System.out.println("[INFO] " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " - " + message);
    }

    private void logError(String message, Exception e) {
        System.err.println("[ERROR] " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " - " + message);
        if (e != null) e.printStackTrace();
    }

    // ============= STATISTICS ENDPOINT - MAKE SURE THIS IS CORRECT =============

    @GetMapping("/get-assignment-statistics")
    public ResponseEntity<?> getAssignmentStatistics() {
        logInfo("📊 Fetching assignment statistics");
        try {
            Map<String, Object> statistics = assignmentService.getAssignmentStatistics();
            return ResponseEntity.ok(new ApiResponse<>(true, "Statistics fetched successfully", statistics));
        } catch (Exception e) {
            logError("Error getting assignment statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error getting statistics", e.getMessage()));
        }
    }

    // ============= PUBLISH ENDPOINTS =============

    @GetMapping("/teacher/{teacherId}/drafts")
    public ResponseEntity<?> getDraftAssignments(@PathVariable Long teacherId) {
        try {
            List<AssignmentResponseDto> drafts = assignmentService.getDraftAssignments(teacherId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Drafts fetched successfully", drafts));
        } catch (Exception e) {
            logError("Error fetching drafts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error fetching drafts", e.getMessage()));
        }
    }

    @GetMapping("/teacher/{teacherId}/scheduled")
    public ResponseEntity<?> getScheduledAssignments(@PathVariable Long teacherId) {
        try {
            List<AssignmentResponseDto> scheduled = assignmentService.getScheduledAssignments(teacherId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Scheduled assignments fetched successfully", scheduled));
        } catch (Exception e) {
            logError("Error fetching scheduled", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error fetching scheduled", e.getMessage()));
        }
    }

    @PostMapping("/{assignmentId}/publish")
    public ResponseEntity<?> publishAssignment(
            @PathVariable Long assignmentId,
            @RequestParam Long teacherId) {
        try {
            AssignmentResponseDto published = assignmentService.publishAssignment(assignmentId, teacherId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Assignment published successfully", published));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logError("Error publishing assignment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error publishing assignment", e.getMessage()));
        }
    }

    // ============= CREATE ASSIGNMENT =============

    @PostMapping("/create-assignment")
    public ResponseEntity<?> createAssignment(@RequestBody AssignmentRequestDto requestDto) {
        logInfo("Creating assignment: " + requestDto.getTitle());
        try {
            validateAssignmentRequest(requestDto);
            AssignmentResponseDto response = assignmentService.createAssignment(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Assignment created successfully", response));
        } catch (IllegalArgumentException e) {
            logError("Validation error: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logError("Error creating assignment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error creating assignment", e.getMessage()));
        }
    }

    @PostMapping(value = "/create-assignment-with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAssignmentWithFiles(
            @RequestPart("assignmentData") String assignmentDataJson,
            @RequestPart(value = "attachmentFiles", required = false) List<MultipartFile> attachmentFiles) {

        logInfo("Creating assignment with multipart data");
        try {
            objectMapper.registerModule(new JavaTimeModule());
            AssignmentRequestDto requestDto = objectMapper.readValue(assignmentDataJson, AssignmentRequestDto.class);

            if (attachmentFiles != null && !attachmentFiles.isEmpty()) {
                requestDto.setAttachmentFiles(attachmentFiles);
            }

            validateAssignmentRequest(requestDto);
            AssignmentResponseDto response = assignmentService.createAssignment(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Assignment created successfully with files", response));

        } catch (Exception e) {
            logError("Error creating assignment with files", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Error creating assignment with files", e.getMessage()));
        }
    }

    // ============= GET ASSIGNMENTS =============

    @GetMapping("/get-all-assignments")
    public ResponseEntity<?> getAllAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        logInfo("📚 Fetching all assignments - page: " + page + ", size: " + size);

        Sort sort;
        if (sortBy != null && !sortBy.isBlank()) {
            Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(dir, sortBy);
        } else {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AssignmentResponseDto> assignmentsPage = assignmentService.getAllAssignments(pageable);
        return ResponseEntity.ok(assignmentsPage);
    }

    @GetMapping("/get-assignment-by-id/{id}")
    public ResponseEntity<?> getAssignmentById(@PathVariable Long id) {
        logInfo("🔍 Fetching assignment by ID: " + id);
        try {
            AssignmentResponseDto assignment = assignmentService.getAssignmentById(id);
            if (assignment != null) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Assignment found", assignment));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Assignment not found with ID: " + id, null));
        } catch (Exception e) {
            logError("Error getting assignment by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error getting assignment", e.getMessage()));
        }
    }

    @GetMapping("/get-assignments-by-status/{status}")
    public ResponseEntity<?> getAssignmentsByStatus(@PathVariable String status) {
        try {
            List<AssignmentResponseDto> assignments = assignmentService.getAssignmentsByStatus(status);
            return ResponseEntity.ok(new ApiResponse<>(true, "Assignments fetched successfully", assignments));
        } catch (Exception e) {
            logError("Error getting assignments by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error getting assignments", e.getMessage()));
        }
    }

    @GetMapping("/get-assignments-by-class/{className}")
    public ResponseEntity<?> getAssignmentsByClass(@PathVariable String className) {
        try {
            List<AssignmentResponseDto> assignments = assignmentService.getAssignmentsByClass(className);
            return ResponseEntity.ok(new ApiResponse<>(true, "Assignments fetched successfully", assignments));
        } catch (Exception e) {
            logError("Error getting assignments by class", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error getting assignments", e.getMessage()));
        }
    }

    @GetMapping("/search-assignments")
    public ResponseEntity<?> searchAssignments(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        try {
            Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

            Page<AssignmentResponseDto> assignments = assignmentService.searchAssignments(
                    subject, className, status, priority, fromDate, toDate, pageable);

            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logError("Error searching assignments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error searching assignments", e.getMessage()));
        }
    }

    // ============= UPDATE ASSIGNMENTS =============

    @PutMapping("/update-assignment/{id}")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Long id,
            @RequestBody AssignmentRequestDto requestDto) {
        try {
            AssignmentResponseDto response = assignmentService.updateAssignment(id, requestDto);
            if (response != null) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Assignment updated successfully", response));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Assignment not found with ID: " + id, null));
        } catch (Exception e) {
            logError("Error updating assignment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error updating assignment", e.getMessage()));
        }
    }

    @PatchMapping("/update-assignment-partial/{id}")
    public ResponseEntity<?> updateAssignmentPartial(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        try {
            AssignmentResponseDto response = assignmentService.updateAssignmentPartial(id, updates);
            if (response != null) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Assignment updated successfully", response));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Assignment not found with ID: " + id, null));
        } catch (Exception e) {
            logError("Error partially updating assignment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error updating assignment", e.getMessage()));
        }
    }

    @PatchMapping("/update-assignment-status/{id}")
    public ResponseEntity<?> updateAssignmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            AssignmentResponseDto response = assignmentService.updateAssignmentStatus(id, status);
            if (response != null) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Status updated successfully", response));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Assignment not found with ID: " + id, null));
        } catch (Exception e) {
            logError("Error updating assignment status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error updating status", e.getMessage()));
        }
    }

    // ============= DELETE ASSIGNMENTS =============

    @DeleteMapping("/delete-assignment/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Assignment deleted successfully with ID: " + id, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logError("Error deleting assignment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error deleting assignment", e.getMessage()));
        }
    }

    @DeleteMapping("/bulk-delete")
    public ResponseEntity<?> bulkDelete(@RequestBody List<Long> assignmentIds) {
        try {
            assignmentService.bulkDeleteAssignments(assignmentIds);
            return ResponseEntity.ok(new ApiResponse<>(true, "Assignments deleted successfully", null));
        } catch (Exception e) {
            logError("Error bulk deleting", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error bulk deleting", e.getMessage()));
        }
    }

    // ============= BULK OPERATIONS =============

    @PatchMapping("/bulk-update-status")
    public ResponseEntity<?> bulkUpdateStatus(
            @RequestBody List<Long> assignmentIds,
            @RequestParam String status) {
        try {
            List<AssignmentResponseDto> updated = assignmentService.bulkUpdateStatus(assignmentIds, status);
            return ResponseEntity.ok(new ApiResponse<>(true, "Status updated successfully", updated));
        } catch (Exception e) {
            logError("Error bulk updating status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error bulk updating status", e.getMessage()));
        }
    }

    @PostMapping("/execute-bulk-action")
    public ResponseEntity<?> executeBulkAction(@RequestBody BulkActionDto bulkActionDto) {
        try {
            Map<String, Object> result = assignmentService.executeBulkAction(bulkActionDto);
            return ResponseEntity.ok(new ApiResponse<>(true, "Bulk action executed", result));
        } catch (Exception e) {
            logError("Error executing bulk action", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error executing bulk action", e.getMessage()));
        }
    }

    // ============= SUBMISSIONS =============

    @PostMapping("/grade-submission")
    public ResponseEntity<?> gradeSubmission(@RequestBody GradeRequestDto gradeRequest) {
        try {
            SubmissionResponseDto response = assignmentService.gradeSubmission(gradeRequest);
            return ResponseEntity.ok(new ApiResponse<>(true, "Grade saved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logError("Error grading submission", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error grading submission", e.getMessage()));
        }
    }

    @GetMapping("/{assignmentId}/get-submissions")
    public ResponseEntity<?> getSubmissionsByAssignment(@PathVariable Long assignmentId) {
        try {
            List<SubmissionResponseDto> submissions = assignmentService.getSubmissionsByAssignment(assignmentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Submissions fetched successfully", submissions));
        } catch (Exception e) {
            logError("Error getting submissions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error getting submissions", e.getMessage()));
        }
    }

    @GetMapping("/{assignmentId}/get-submission/{studentId}")
    public ResponseEntity<?> getSubmission(
            @PathVariable Long assignmentId,
            @PathVariable Long studentId) {
        try {
            SubmissionResponseDto submission = assignmentService.getSubmission(assignmentId, studentId);
            if (submission != null) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Submission found", submission));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Submission not found", null));
        } catch (Exception e) {
            logError("Error getting submission", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error getting submission", e.getMessage()));
        }
    }

    @GetMapping("/{assignmentId}/get-submission-stats")
    public ResponseEntity<?> getSubmissionStats(@PathVariable Long assignmentId) {
        try {
            Map<String, Object> stats = assignmentService.getSubmissionStats(assignmentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Submission stats fetched successfully", stats));
        } catch (Exception e) {
            logError("Error getting submission stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error getting submission stats", e.getMessage()));
        }
    }

    // ============= REMINDERS =============

    @PostMapping("/{id}/send-reminders")
    public ResponseEntity<?> sendReminders(
            @PathVariable Long id,
            @RequestParam String reminderType,
            @RequestParam(required = false) String customMessage) {
        try {
            assignmentService.sendReminders(id, reminderType, customMessage);
            return ResponseEntity.ok(new ApiResponse<>(true, "Reminders sent successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logError("Error sending reminders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error sending reminders", e.getMessage()));
        }
    }

    @PostMapping("/send-bulk-reminders")
    public ResponseEntity<?> sendBulkReminders(
            @RequestBody List<Long> assignmentIds,
            @RequestParam String reminderType,
            @RequestParam(required = false) String customMessage) {
        try {
            assignmentService.sendBulkReminders(assignmentIds, reminderType, customMessage);
            return ResponseEntity.ok(new ApiResponse<>(true, "Bulk reminders sent successfully", null));
        } catch (Exception e) {
            logError("Error sending bulk reminders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error sending bulk reminders", e.getMessage()));
        }
    }

    // ============= VALIDATION METHOD =============

    private void validateAssignmentRequest(AssignmentRequestDto requestDto) {
        if (requestDto.getTitle() == null || requestDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (requestDto.getSubject() == null || requestDto.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required");
        }
        if (requestDto.getClassId() == null) {
            throw new IllegalArgumentException("Class is required (classId must be provided)");
        }
        if (requestDto.getSection() == null || requestDto.getSection().trim().isEmpty()) {
            throw new IllegalArgumentException("Section is required");
        }
        if (requestDto.getDescription() == null || requestDto.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (requestDto.getGradingType() == null) {
            throw new IllegalArgumentException("Grading type is required");
        }
        if (requestDto.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (requestDto.getDueDate() == null) {
            throw new IllegalArgumentException("Due date is required");
        }
        if (requestDto.getDueDate().isBefore(requestDto.getStartDate())) {
            throw new IllegalArgumentException("Due date must be after start date");
        }
        if (requestDto.getPriority() == null) {
            throw new IllegalArgumentException("Priority is required");
        }
        if (requestDto.getAssignTo() == null) {
            throw new IllegalArgumentException("Assign to is required");
        }
        if (requestDto.getCreatedByTeacherId() == null) {
            throw new IllegalArgumentException("Teacher ID is required");
        }
    }
}