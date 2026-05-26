package com.sc.service;

import com.sc.dto.request.AssignmentRequestDto;
import com.sc.dto.request.BulkActionDto;
import com.sc.dto.request.GradeRequestDto;
import com.sc.dto.response.AssignmentResponseDto;
import com.sc.dto.response.SubmissionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AssignmentService {

    // Create
    AssignmentResponseDto createAssignment(AssignmentRequestDto requestDto);

    // Read
    AssignmentResponseDto getAssignmentById(Long id);
    AssignmentResponseDto getAssignmentByCode(String assignmentCode);
    Page<AssignmentResponseDto> getAllAssignments(Pageable pageable);
    List<AssignmentResponseDto> getAssignmentsByStatus(String status);
    List<AssignmentResponseDto> getAssignmentsByClass(String className);
    List<AssignmentResponseDto> getAssignmentsByClassAndSection(String className, String section);
    List<AssignmentResponseDto> getAssignmentsBySubject(String subject);
    List<AssignmentResponseDto> getOverdueAssignments();

    // Search
    Page<AssignmentResponseDto> searchAssignments(
            String subject, String className, String status, String priority,
            LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    // Update
    AssignmentResponseDto updateAssignment(Long id, AssignmentRequestDto requestDto);
    AssignmentResponseDto updateAssignmentPartial(Long id, Map<String, Object> updates);
    AssignmentResponseDto updateAssignmentStatus(Long id, String status);

    // Delete
    void deleteAssignment(Long id);
    void bulkDeleteAssignments(List<Long> assignmentIds);

    // Statistics
    Map<String, Object> getAssignmentStatistics();
    Map<String, Long> getCountByStatus();
    Map<String, Object> getAssignmentAnalytics(Long id);

    // Submission methods
    SubmissionResponseDto gradeSubmission(GradeRequestDto gradeRequest);
    List<SubmissionResponseDto> getSubmissionsByAssignment(Long assignmentId);
    List<SubmissionResponseDto> getSubmissionsByStudent(Long studentId);
    SubmissionResponseDto getSubmission(Long assignmentId, Long studentId);
    Map<String, Object> getSubmissionStats(Long assignmentId);

    // Bulk operations
    List<AssignmentResponseDto> bulkUpdateStatus(List<Long> assignmentIds, String status);
    Map<String, Object> executeBulkAction(BulkActionDto bulkActionDto);

    // Attachment methods
    AssignmentResponseDto uploadAttachments(Long id, List<MultipartFile> files);
    byte[] getAttachment(Long id, String fileName);

    // Reminder methods
    void sendReminders(Long assignmentId, String reminderType, String customMessage);
    void sendBulkReminders(List<Long> assignmentIds, String reminderType, String customMessage);

    // Student specific methods
    List<AssignmentResponseDto> getAssignmentsForStudent(Long studentId);
    List<AssignmentResponseDto> getPendingAssignmentsForStudent(Long studentId);
    List<AssignmentResponseDto> getCompletedAssignmentsForStudent(Long studentId);
    List<AssignmentResponseDto> getAssignmentsByTeacher(Long teacherId);

    // ============= NEW PUBLISH METHODS =============
    List<AssignmentResponseDto> getDraftAssignments(Long teacherId);
    List<AssignmentResponseDto> getScheduledAssignments(Long teacherId);
    AssignmentResponseDto publishAssignment(Long assignmentId, Long teacherId);



}