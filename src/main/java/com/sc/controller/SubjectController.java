package com.sc.controller;

import com.sc.dto.request.SubjectCreateRequestDTO;
import com.sc.dto.response.SubjectResponseDTO;
import com.sc.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private static final Logger logger = LoggerFactory.getLogger(SubjectController.class);

    private final SubjectService subjectService;

    @Autowired
    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSubject(@RequestBody SubjectCreateRequestDTO requestDTO) {
        logger.info("POST /api/subjects/create → Creating subject: {}", requestDTO.getSubjectCode());

        try {
            SubjectResponseDTO createdSubject = subjectService.createSubject(requestDTO);
            logger.info("Subject created successfully with ID: {}", createdSubject.getSubjectId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSubject);
        } catch (IllegalArgumentException e) {
            logger.warn("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating subject", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create subject. Please try again later.");
        }
    }

    @GetMapping("/get-subject-by-id/get/{subjectId}")
    public ResponseEntity<?> getSubjectById(@PathVariable Long subjectId) {
        logger.info("GET /api/subjects/get/{} → Fetching subject", subjectId);

        try {
            SubjectResponseDTO subject = subjectService.getSubjectById(subjectId);
            logger.info("Subject found with ID: {}", subjectId);
            return ResponseEntity.ok(subject);
        } catch (Exception e) {
            logger.error("Error fetching subject with ID: {}", subjectId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Subject not found with ID: " + subjectId);
        }
    }

    @GetMapping("/get-subject-by-code/{subjectCode}")
    public ResponseEntity<?> getSubjectByCode(@PathVariable String subjectCode) {
        logger.info("GET /api/subjects/get-by-code/{} → Fetching subject", subjectCode);

        try {
            SubjectResponseDTO subject = subjectService.getSubjectByCode(subjectCode);
            logger.info("Subject found with code: {}", subjectCode);
            return ResponseEntity.ok(subject);
        } catch (Exception e) {
            logger.error("Error fetching subject with code: {}", subjectCode, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Subject not found with code: " + subjectCode);
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllSubjects() {
        logger.info("GET /api/subjects/get-all → Fetching all subjects");

        try {
            List<SubjectResponseDTO> subjects = subjectService.getAllSubjects();
            logger.info("Returning {} subjects", subjects.size());
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            logger.error("Error fetching all subjects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch subjects. Please try again later.");
        }
    }

    @GetMapping("/get-subject-by-type/{subjectType}")
    public ResponseEntity<?> getSubjectsByType(@PathVariable String subjectType) {
        logger.info("GET /api/subjects/get-by-type/{} → Fetching subjects", subjectType);

        try {
            List<SubjectResponseDTO> subjects = subjectService.getSubjectsByType(subjectType);
            logger.info("Returning {} subjects of type: {}", subjects.size(), subjectType);
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            logger.error("Error fetching subjects by type: {}", subjectType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch subjects. Please try again later.");
        }
    }

    @GetMapping("/get-subjects-by-grade-level/{gradeLevel}")
    public ResponseEntity<?> getSubjectsByGradeLevel(@PathVariable String gradeLevel) {
        logger.info("GET /api/subjects/get-by-grade/{} → Fetching subjects", gradeLevel);

        try {
            List<SubjectResponseDTO> subjects = subjectService.getSubjectsByGradeLevel(gradeLevel);
            logger.info("Returning {} subjects for grade level: {}", subjects.size(), gradeLevel);
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            logger.error("Error fetching subjects for grade level: {}", gradeLevel, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch subjects. Please try again later.");
        }
    }

    @GetMapping("/get-subject-by-teacher/{teacherId}")
    public ResponseEntity<?> getSubjectsByTeacher(@PathVariable Long teacherId) {
        logger.info("GET /api/subjects/get-by-teacher/{} → Fetching subjects", teacherId);

        try {
            List<SubjectResponseDTO> subjects = subjectService.getSubjectsByTeacher(teacherId);
            logger.info("Returning {} subjects for teacher ID: {}", subjects.size(), teacherId);
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            logger.error("Error fetching subjects for teacher ID: {}", teacherId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch subjects. Please try again later.");
        }
    }

    @PutMapping("/update-subject/{subjectId}")
    public ResponseEntity<?> updateSubject(@PathVariable Long subjectId,
                                           @RequestBody SubjectCreateRequestDTO requestDTO) {
        logger.info("PUT /api/subjects/update/{} → Updating subject", subjectId);

        try {
            SubjectResponseDTO updatedSubject = subjectService.updateSubject(subjectId, requestDTO);
            logger.info("Subject updated successfully with ID: {}", subjectId);
            return ResponseEntity.ok(updatedSubject);
        } catch (IllegalArgumentException e) {
            logger.warn("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating subject with ID: {}", subjectId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Subject not found or update failed. ID: " + subjectId);
        }
    }

    @PatchMapping("/update-subject-teacher/{subjectId}")
    public ResponseEntity<?> updateSubjectTeacher(@PathVariable Long subjectId,
                                                  @RequestParam Long teacherId) {
        logger.info("PATCH /api/subjects/update-teacher/{} → Teacher ID: {}", subjectId, teacherId);

        try {
            SubjectResponseDTO updatedSubject = subjectService.updateSubjectTeacher(subjectId, teacherId);
            logger.info("Subject teacher updated successfully for ID: {}", subjectId);
            return ResponseEntity.ok(updatedSubject);
        } catch (Exception e) {
            logger.error("Error updating subject teacher for ID: {}", subjectId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Subject not found or update failed. ID: " + subjectId);
        }
    }

    @PatchMapping("/update-subject-status/{subjectId}")
    public ResponseEntity<?> updateSubjectStatus(@PathVariable Long subjectId,
                                                 @RequestParam String status) {
        logger.info("PATCH /api/subjects/update-status/{} → Status: {}", subjectId, status);

        try {
            SubjectResponseDTO updatedSubject = subjectService.updateSubjectStatus(subjectId, status);
            logger.info("Subject status updated successfully for ID: {}", subjectId);
            return ResponseEntity.ok(updatedSubject);
        } catch (Exception e) {
            logger.error("Error updating subject status for ID: {}", subjectId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Subject not found with ID: " + subjectId);
        }
    }

    @PatchMapping("/update-subject-display-order/{subjectId}")
    public ResponseEntity<?> updateSubjectDisplayOrder(@PathVariable Long subjectId,
                                                       @RequestParam Integer displayOrder) {
        logger.info("PATCH /api/subjects/update-order/{} → Display Order: {}", subjectId, displayOrder);

        try {
            SubjectResponseDTO updatedSubject = subjectService.updateSubjectDisplayOrder(subjectId, displayOrder);
            logger.info("Subject display order updated successfully for ID: {}", subjectId);
            return ResponseEntity.ok(updatedSubject);
        } catch (Exception e) {
            logger.error("Error updating subject display order for ID: {}", subjectId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Subject not found with ID: " + subjectId);
        }
    }

    @DeleteMapping("/delete-subject/{subjectId}")
    public ResponseEntity<?> deleteSubject(@PathVariable Long subjectId) {
        logger.info("DELETE /api/subjects/delete/{} → Deleting subject", subjectId);

        try {
            subjectService.softDeleteSubject(subjectId);
            logger.info("Subject marked as deleted with ID: {}", subjectId);
            return ResponseEntity.ok("Subject deleted successfully (soft delete). ID: " + subjectId);
        } catch (Exception e) {
            logger.error("Error deleting subject with ID: {}", subjectId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Subject not found with ID: " + subjectId);
        }
    }

    @GetMapping("/check-subject-code-exists/{subjectCode}")
    public ResponseEntity<?> checkSubjectCodeExists(@PathVariable String subjectCode) {
        logger.info("GET /api/subjects/check-code-exists/{} → Checking subject code", subjectCode);

        try {
            boolean exists = subjectService.isSubjectCodeExists(subjectCode);
            logger.info("Subject code {} exists: {}", subjectCode, exists);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            logger.error("Error checking subject code existence: {}", subjectCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to check subject code. Please try again later.");
        }
    }

    @GetMapping("/get-subject-for-class")
    public ResponseEntity<?> getSubjectsForClass(@RequestParam String gradeLevel,
                                                 @RequestParam String section) {
        logger.info("GET /api/subjects/get-for-class → Grade: {}, Section: {}", gradeLevel, section);

        try {
            List<SubjectResponseDTO> subjects = subjectService.getSubjectsForClass(gradeLevel, section);
            logger.info("Returning {} subjects for class", subjects.size());
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            logger.error("Error fetching subjects for class", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch subjects. Please try again later.");
        }
    }

    @PostMapping("/validate-subject-combination")
    public ResponseEntity<?> validateSubjectCombination(@RequestBody List<Long> subjectIds) {
        logger.info("POST /api/subjects/validate-combination → Validating {} subjects", subjectIds.size());

        try {
            boolean isValid = subjectService.validateSubjectCombination(subjectIds);
            logger.info("Subject combination validation result: {}", isValid);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            logger.error("Error validating subject combination", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to validate subject combination. Please try again later.");
        }
    }

    @GetMapping("/get-subject-statistics")
    public ResponseEntity<?> getSubjectStatistics() {
        logger.info("GET /api/subjects/get-statistics → Fetching statistics");

        try {
            List<SubjectResponseDTO> allSubjects = subjectService.getAllSubjects();

            java.util.Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("totalSubjects", allSubjects.size());

            // Count by type
            java.util.Map<String, Long> subjectsByType = allSubjects.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            SubjectResponseDTO::getSubjectType,
                            java.util.stream.Collectors.counting()
                    ));
            statistics.put("subjectsByType", subjectsByType);

            // Count by grade level
            java.util.Map<String, Long> subjectsByGrade = allSubjects.stream()
                    .filter(s -> s.getGradeLevel() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                            SubjectResponseDTO::getGradeLevel,
                            java.util.stream.Collectors.counting()
                    ));
            statistics.put("subjectsByGrade", subjectsByGrade);

            // Count by status
            long activeSubjects = allSubjects.stream()
                    .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                    .count();
            long inactiveSubjects = allSubjects.stream()
                    .filter(s -> "INACTIVE".equalsIgnoreCase(s.getStatus()))
                    .count();

            statistics.put("activeSubjects", activeSubjects);
            statistics.put("inactiveSubjects", inactiveSubjects);

            logger.info("Subject statistics fetched successfully");
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error fetching subject statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch statistics. Please try again later.");
        }
    }
}