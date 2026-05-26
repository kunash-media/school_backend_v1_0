package com.sc.controller;

import com.sc.dto.request.ExamCreateRequest;
import com.sc.dto.response.ExamResponse;
import com.sc.service.ExamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private static final Logger logger = LoggerFactory.getLogger(ExamController.class);

    @Autowired
    private ExamService examService;

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }

    private Map<String, String> errorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    // ============= CREATE EXAM =============
    @PostMapping("/create-exam")
    public ResponseEntity<?> createExam(@RequestBody ExamCreateRequest request) {
        logger.info("POST /api/exams/create-exam - Creating new exam: {}", request.getExamName());
        try {
            ExamResponse createdExam = examService.createExam(request, getCurrentUser());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdExam);
        } catch (Exception e) {
            logger.error("Error creating exam: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    // ============= GET ALL EXAMS =============
    @GetMapping("/get-all-exams")
    public ResponseEntity<?> getAllExams() {
        logger.info("GET /api/exams/get-all-exams - Fetching all exams");
        try {
            List<ExamResponse> exams = examService.getAllExams();
            return ResponseEntity.ok(exams);
        } catch (Exception e) {
            logger.error("Error fetching exams: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching exams: " + e.getMessage()));
        }
    }

    // ============= GET EXAM BY ID =============
    @GetMapping("/get-exam-by-id/{examId}")
    public ResponseEntity<?> getExamById(@PathVariable Long examId) {
        logger.info("GET /api/exams/get-exam-by-id/{} - Fetching exam", examId);
        try {
            ExamResponse exam = examService.getExamById(examId);
            return ResponseEntity.ok(exam);
        } catch (Exception e) {
            logger.error("Error fetching exam: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse(e.getMessage()));
        }
    }

    // ============= GET EXAM BY CODE =============
    @GetMapping("/get-exam-by-code/{examCode}")
    public ResponseEntity<?> getExamByCode(@PathVariable String examCode) {
        logger.info("GET /api/exams/get-exam-by-code/{} - Fetching exam", examCode);
        try {
            ExamResponse exam = examService.getExamByCode(examCode);
            return ResponseEntity.ok(exam);
        } catch (Exception e) {
            logger.error("Error fetching exam: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse(e.getMessage()));
        }
    }

    // ============= GET EXAMS BY CLASS =============
    @GetMapping("/get-exams-by-class/{classId}")
    public ResponseEntity<?> getExamsByClass(@PathVariable Long classId) {
        logger.info("GET /api/exams/get-exams-by-class/{} - Fetching exams", classId);
        try {
            List<ExamResponse> exams = examService.getExamsByClass(classId);
            return ResponseEntity.ok(exams);
        } catch (Exception e) {
            logger.error("Error fetching exams: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching exams: " + e.getMessage()));
        }
    }

    // ============= GET EXAMS BY CLASS AND SECTION =============
    @GetMapping("/get-exams-by-class/{classId}/section/{section}")
    public ResponseEntity<?> getExamsByClassAndSection(
            @PathVariable Long classId,
            @PathVariable String section) {
        logger.info("GET /api/exams/get-exams-by-class/{}/section/{} - Fetching exams", classId, section);
        try {
            List<ExamResponse> exams = examService.getExamsByClassAndSection(classId, section);
            return ResponseEntity.ok(exams);
        } catch (Exception e) {
            logger.error("Error fetching exams: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching exams: " + e.getMessage()));
        }
    }

    // ============= GET EXAMS BY ACADEMIC YEAR =============
    @GetMapping("/get-exams-by-academic-year/{academicYear}")
    public ResponseEntity<?> getExamsByAcademicYear(@PathVariable String academicYear) {
        logger.info("GET /api/exams/get-exams-by-academic-year/{} - Fetching exams", academicYear);
        try {
            List<ExamResponse> exams = examService.getExamsByAcademicYear(academicYear);
            return ResponseEntity.ok(exams);
        } catch (Exception e) {
            logger.error("Error fetching exams: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching exams: " + e.getMessage()));
        }
    }

    // ============= GET EXAMS BY STATUS =============
    @GetMapping("/get-exams-by-status/{status}")
    public ResponseEntity<?> getExamsByStatus(@PathVariable String status) {
        logger.info("GET /api/exams/get-exams-by-status/{} - Fetching exams", status);
        try {
            List<ExamResponse> exams = examService.getExamsByStatus(status);
            return ResponseEntity.ok(exams);
        } catch (Exception e) {
            logger.error("Error fetching exams: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching exams: " + e.getMessage()));
        }
    }

    // ============= GET UPCOMING EXAMS =============
    @GetMapping("/get-upcoming-exams")
    public ResponseEntity<?> getUpcomingExams() {
        logger.info("GET /api/exams/get-upcoming-exams - Fetching upcoming exams");
        try {
            List<ExamResponse> exams = examService.getUpcomingExams();
            return ResponseEntity.ok(exams);
        } catch (Exception e) {
            logger.error("Error fetching upcoming exams: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching exams: " + e.getMessage()));
        }
    }

    // ============= UPDATE EXAM =============
    @PutMapping("/update-exam/{examId}")
    public ResponseEntity<?> updateExam(
            @PathVariable Long examId,
            @RequestBody ExamCreateRequest request) {
        logger.info("PUT /api/exams/update-exam/{} - Updating exam", examId);
        try {
            ExamResponse updatedExam = examService.updateExam(examId, request, getCurrentUser());
            return ResponseEntity.ok(updatedExam);
        } catch (Exception e) {
            logger.error("Error updating exam: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    // ============= UPDATE EXAM STATUS =============
    @PatchMapping("/update-exam-status/{examId}")
    public ResponseEntity<?> updateExamStatus(
            @PathVariable Long examId,
            @RequestParam String status) {
        logger.info("PATCH /api/exams/update-exam-status/{} - Updating status to {}", examId, status);
        try {
            ExamResponse updatedExam = examService.updateExamStatus(examId, status);
            return ResponseEntity.ok(updatedExam);
        } catch (Exception e) {
            logger.error("Error updating exam status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    // ============= DELETE EXAM =============
    @DeleteMapping("/delete-exam/{examId}")
    public ResponseEntity<?> deleteExam(@PathVariable Long examId) {
        logger.info("DELETE /api/exams/delete-exam/{} - Deleting exam", examId);
        try {
            examService.deleteExam(examId);
            return ResponseEntity.ok("Exam deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting exam: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse(e.getMessage()));
        }
    }

    // ============= CHECK EXAM CODE =============
    @GetMapping("/check-exam-code/{examCode}")
    public ResponseEntity<?> checkExamCode(@PathVariable String examCode) {
        logger.info("GET /api/exams/check-exam-code/{} - Checking exam code", examCode);
        try {
            boolean exists = examService.isExamCodeExists(examCode);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            logger.error("Error checking exam code: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error checking exam code"));
        }
    }
}