package com.sc.controller;

import com.sc.CustomExceptions.ResourceNotFoundException;
import com.sc.dto.request.ClassCreateRequestDTO;
import com.sc.dto.response.ClassResponseDTO;
import com.sc.dto.request.TeacherSubjectAssignmentDTO;
import com.sc.dto.response.ClassSubjectsResponseDTO;
import com.sc.service.ClassService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    private static final Logger logger = LoggerFactory.getLogger(ClassController.class);

    private final ClassService classService;

    @Autowired
    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    // ────────────────────────────────────────────────
    //               CREATE CLASS (FULL)
    // ────────────────────────────────────────────────
    @PostMapping("/create-class")
    public ResponseEntity<?> createClass(@RequestBody ClassCreateRequestDTO classCreateRequestDTO) {
        logger.info("POST /api/classes/create-class → Creating new class: {} {}",
                classCreateRequestDTO.getClassName(), classCreateRequestDTO.getClassCode());

        try {
            ClassResponseDTO createdClass = classService.createClass(classCreateRequestDTO);
            String message = "Class created successfully with ID: " + createdClass.getClassId();
            logger.info(message);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClass);
        } catch (IllegalArgumentException e) {
            logger.warn("Validation failed during class creation: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating class", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create class. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //             CREATE CLASS (BASIC)
    // ────────────────────────────────────────────────
    @PostMapping("/create-class-basic")
    public ResponseEntity<?> createClassBasic(@RequestBody ClassCreateRequestDTO classCreateRequestDTO) {
        logger.info("POST /api/classes/create-class-basic → Creating basic class: {} {}",
                classCreateRequestDTO.getClassName(), classCreateRequestDTO.getClassCode());

        try {
            ClassResponseDTO createdClass = classService.createClassBasic(classCreateRequestDTO);
            String message = "Basic class created successfully with ID: " + createdClass.getClassId();
            logger.info(message);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClass);
        } catch (IllegalArgumentException e) {
            logger.warn("Validation failed during basic class creation: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating basic class", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create basic class. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //                  GET CLASS BY ID
    // ────────────────────────────────────────────────
    @GetMapping("/get-class-by-id/{classId}")
    public ResponseEntity<?> getClassById(@PathVariable Long classId) {
        logger.info("GET /api/classes/get-class-by-id/{} → Fetching class", classId);

        try {
            ClassResponseDTO classResponse = classService.getClassById(classId);
            logger.info("Class found with ID: {}", classId);
            return ResponseEntity.ok(classResponse);
        } catch (Exception e) {
            logger.error("Error fetching class with ID: {}", classId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found with ID: " + classId);
        }
    }

    // ────────────────────────────────────────────────
    //              GET CLASS BY CLASS CODE
    // ────────────────────────────────────────────────
    @GetMapping("/get-class-by-code/{classCode}")
    public ResponseEntity<?> getClassByCode(@PathVariable String classCode) {
        logger.info("GET /api/classes/get-class-by-code/{} → Fetching class", classCode);

        try {
            ClassResponseDTO classResponse = classService.getClassByCode(classCode);
            logger.info("Class found with code: {}", classCode);
            return ResponseEntity.ok(classResponse);
        } catch (Exception e) {
            logger.error("Error fetching class with code: {}", classCode, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found with code: " + classCode);
        }
    }

    // ────────────────────────────────────────────────
    //                  GET ALL CLASSES
    // ────────────────────────────────────────────────
    @GetMapping("/get-all-classes")
    public ResponseEntity<?> getAllClasses() {
        logger.info("GET /api/classes/get-all-classes → Fetching all classes");

        try {
            List<ClassResponseDTO> classes = classService.getAllClasses();
            logger.info("Returning {} classes", classes.size());
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            logger.error("Error fetching all classes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch classes. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //           GET CLASSES BY ACADEMIC YEAR
    // ────────────────────────────────────────────────
    @GetMapping("/get-classes-by-academic-year/{academicYear}")
    public ResponseEntity<?> getClassesByAcademicYear(@PathVariable String academicYear) {
        logger.info("GET /api/classes/get-classes-by-academic-year/{} → Fetching classes", academicYear);

        try {
            List<ClassResponseDTO> classes = classService.getClassesByAcademicYear(academicYear);
            logger.info("Returning {} classes for academic year: {}", classes.size(), academicYear);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            logger.error("Error fetching classes for academic year: {}", academicYear, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch classes. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //             GET CLASSES BY CLASS NAME
    // ────────────────────────────────────────────────
    @GetMapping("/get-classes-by-name/{className}")
    public ResponseEntity<?> getClassesByClassName(@PathVariable String className) {
        logger.info("GET /api/classes/get-classes-by-name/{} → Fetching classes", className);

        try {
            List<ClassResponseDTO> classes = classService.getClassesByClassName(className);
            logger.info("Returning {} classes with name: {}", classes.size(), className);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            logger.error("Error fetching classes with name: {}", className, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch classes. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //            GET CLASSES BY TEACHER ID
    // ────────────────────────────────────────────────
    @GetMapping("/get-classes-by-teacher/{teacherId}")
    public ResponseEntity<?> getClassesByTeacher(@PathVariable Long teacherId) {
        logger.info("GET /api/classes/get-classes-by-teacher/{} → Fetching classes", teacherId);

        try {
            List<ClassResponseDTO> classes = classService.getClassesByTeacher(teacherId);
            logger.info("Returning {} classes for teacher ID: {}", classes.size(), teacherId);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            logger.error("Error fetching classes for teacher ID: {}", teacherId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch classes. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //                 UPDATE CLASS
    // ────────────────────────────────────────────────
    @PutMapping("/update-class/{classId}")
    public ResponseEntity<?> updateClass(
            @PathVariable Long classId,
            @RequestBody ClassCreateRequestDTO classCreateRequestDTO) {
        logger.info("PUT /api/classes/update-class/{} → Updating class", classId);

        try {
            ClassResponseDTO updatedClass = classService.updateClass(classId, classCreateRequestDTO);
            logger.info("Class updated successfully with ID: {}", classId);
            return ResponseEntity.ok(updatedClass);
        } catch (IllegalArgumentException e) {
            logger.warn("Validation failed during class update: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating class with ID: {}", classId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found or update failed. ID: " + classId);
        }
    }

    // ────────────────────────────────────────────────
    //          UPDATE CLASS TEACHER ONLY
    // ────────────────────────────────────────────────
    @PatchMapping("/update-class-teacher/{classId}")
    public ResponseEntity<?> updateClassTeacher(
            @PathVariable Long classId,
            @RequestParam Long teacherId,
            @RequestParam String subject) {
        logger.info("PATCH /api/classes/update-class-teacher/{} → Teacher ID: {}, Subject: {}",
                classId, teacherId, subject);

        try {
            ClassResponseDTO updatedClass = classService.updateClassTeacher(classId, teacherId, subject);
            logger.info("Class teacher updated successfully for class ID: {}", classId);
            return ResponseEntity.ok(updatedClass);
        } catch (Exception e) {
            logger.error("Error updating class teacher for class ID: {}", classId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found or update failed. ID: " + classId);
        }
    }

    // ────────────────────────────────────────────────
    //        UPDATE ASSISTANT TEACHER ONLY
    // ────────────────────────────────────────────────
    @PatchMapping("/update-assistant-teacher/{classId}")
    public ResponseEntity<?> updateAssistantTeacher(
            @PathVariable Long classId,
            @RequestParam Long teacherId,
            @RequestParam String subject) {
        logger.info("PATCH /api/classes/update-assistant-teacher/{} → Teacher ID: {}, Subject: {}",
                classId, teacherId, subject);

        try {
            ClassResponseDTO updatedClass = classService.updateAssistantTeacher(classId, teacherId, subject);
            logger.info("Assistant teacher updated successfully for class ID: {}", classId);
            return ResponseEntity.ok(updatedClass);
        } catch (Exception e) {
            logger.error("Error updating assistant teacher for class ID: {}", classId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found or update failed. ID: " + classId);
        }
    }

    // ────────────────────────────────────────────────
    //       ADD OTHER TEACHER SUBJECT ASSIGNMENT
    // ────────────────────────────────────────────────
    @PostMapping("/add-other-teacher-subject/{classId}")
    public ResponseEntity<?> addOtherTeacherSubject(
            @PathVariable Long classId,
            @RequestBody TeacherSubjectAssignmentDTO teacherSubjectAssignmentDTO) {
        logger.info("POST /api/classes/add-other-teacher-subject/{} → Teacher ID: {}",
                classId, teacherSubjectAssignmentDTO.getTeacherId());

        try {
            ClassResponseDTO updatedClass = classService.addOtherTeacherSubject(classId, teacherSubjectAssignmentDTO);
            logger.info("Other teacher subject added successfully for class ID: {}", classId);
            return ResponseEntity.ok(updatedClass);
        } catch (Exception e) {
            logger.error("Error adding other teacher subject for class ID: {}", classId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found or operation failed. ID: " + classId);
        }
    }

    // ────────────────────────────────────────────────
    //      REMOVE OTHER TEACHER SUBJECT ASSIGNMENT
    // ────────────────────────────────────────────────
    @DeleteMapping("/remove-other-teacher-subject/{classId}")
    public ResponseEntity<?> removeOtherTeacherSubject(
            @PathVariable Long classId,
            @RequestParam String teacherId,
            @RequestParam String subject) {
        logger.info("DELETE /api/classes/remove-other-teacher-subject/{} → Teacher ID: {}, Subject: {}",
                classId, teacherId, subject);

        try {
            ClassResponseDTO updatedClass = classService.removeOtherTeacherSubject(classId, teacherId, subject);
            logger.info("Other teacher subject removed successfully for class ID: {}", classId);
            return ResponseEntity.ok(updatedClass);
        } catch (Exception e) {
            logger.error("Error removing other teacher subject for class ID: {}", classId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found or operation failed. ID: " + classId);
        }
    }

    // ────────────────────────────────────────────────
    //              UPDATE CLASS STATUS
    // ────────────────────────────────────────────────
    @PatchMapping("/update-class-status/{classId}")
    public ResponseEntity<?> updateClassStatus(
            @PathVariable Long classId,
            @RequestParam String status) {
        logger.info("PATCH /api/classes/update-class-status/{} → Status: {}", classId, status);

        try {
            ClassResponseDTO updatedClass = classService.updateClassStatus(classId, status);
            logger.info("Class status updated successfully for ID: {}", classId);
            return ResponseEntity.ok(updatedClass);
        } catch (Exception e) {
            logger.error("Error updating class status for ID: {}", classId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found with ID: " + classId);
        }
    }

    // ────────────────────────────────────────────────
    //              DELETE CLASS (SOFT DELETE)
    // ────────────────────────────────────────────────
    @DeleteMapping("/delete-class/{classId}")
    public ResponseEntity<?> deleteClass(@PathVariable Long classId) {
        logger.info("DELETE /api/classes/delete-class/{} → Deleting class", classId);

        try {
            classService.softDeleteClass(classId);
            logger.info("Class marked as deleted with ID: {}", classId);
            return ResponseEntity.ok("Class deleted successfully (soft delete). ID: " + classId);
        } catch (Exception e) {
            logger.error("Error deleting class with ID: {}", classId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found with ID: " + classId);
        }
    }

    // ────────────────────────────────────────────────
    //           HARD DELETE CLASS (PERMANENT)
    // ────────────────────────────────────────────────
    @DeleteMapping("/hard-delete-class/{classId}")
    public ResponseEntity<?> hardDeleteClass(@PathVariable Long classId) {
        logger.info("DELETE /api/classes/hard-delete-class/{} → Hard deleting class", classId);

        try {
            classService.deleteClass(classId);
            logger.info("Class permanently deleted with ID: {}", classId);
            return ResponseEntity.ok("Class permanently deleted. ID: " + classId);
        } catch (Exception e) {
            logger.error("Error hard deleting class with ID: {}", classId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found with ID: " + classId);
        }
    }

    // ────────────────────────────────────────────────
    //          CHECK IF CLASS CODE EXISTS
    // ────────────────────────────────────────────────
    @GetMapping("/check-class-code-exists/{classCode}")
    public ResponseEntity<?> checkClassCodeExists(@PathVariable String classCode) {
        logger.info("GET /api/classes/check-class-code-exists/{} → Checking class code", classCode);

        try {
            boolean exists = classService.isClassCodeExists(classCode);
            logger.info("Class code {} exists: {}", classCode, exists);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            logger.error("Error checking class code existence: {}", classCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to check class code. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //      CHECK IF TEACHER ASSIGNED TO CLASS
    // ────────────────────────────────────────────────
    @GetMapping("/check-teacher-assigned/{teacherId}")
    public ResponseEntity<?> checkTeacherAssigned(
            @PathVariable Long teacherId,
            @RequestParam(required = false) Long excludeClassId) {
        logger.info("GET /api/classes/check-teacher-assigned/{} → Exclude Class ID: {}",
                teacherId, excludeClassId);

        try {
            boolean isAssigned = classService.isTeacherAssignedToClass(teacherId, excludeClassId);
            logger.info("Teacher ID {} is assigned to class (excluding {}): {}",
                    teacherId, excludeClassId, isAssigned);
            return ResponseEntity.ok(isAssigned);
        } catch (Exception e) {
            logger.error("Error checking teacher assignment for teacher ID: {}", teacherId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to check teacher assignment. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //          GET CLASS STATISTICS
    // ────────────────────────────────────────────────
    @GetMapping("/get-class-statistics")
    public ResponseEntity<?> getClassStatistics() {
        logger.info("GET /api/classes/get-class-statistics → Fetching statistics");

        try {
            List<ClassResponseDTO> allClasses = classService.getAllClasses();

            Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("totalClasses", allClasses.size());

            // Count by status
            long activeClasses = allClasses.stream()
                    .filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus()))
                    .count();
            long inactiveClasses = allClasses.stream()
                    .filter(c -> "INACTIVE".equalsIgnoreCase(c.getStatus()))
                    .count();

            statistics.put("activeClasses", activeClasses);
            statistics.put("inactiveClasses", inactiveClasses);

            // Count by class name
            Map<String, Long> classesByName = allClasses.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            ClassResponseDTO::getClassName,
                            java.util.stream.Collectors.counting()
                    ));
            statistics.put("classesByName", classesByName);

            // Calculate average capacity
            double avgCapacity = allClasses.stream()
                    .filter(c -> c.getMaxStudents() != null && c.getCurrentStudents() != null)
                    .mapToDouble(c -> (c.getCurrentStudents() * 100.0) / c.getMaxStudents())
                    .average()
                    .orElse(0.0);
            statistics.put("averageCapacityPercentage", Math.round(avgCapacity * 100.0) / 100.0);

            logger.info("Class statistics fetched successfully");
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error fetching class statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch statistics. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //         GET CLASSES BY STATUS
    // ────────────────────────────────────────────────
    @GetMapping("/get-classes-by-status/{status}")
    public ResponseEntity<?> getClassesByStatus(@PathVariable String status) {
        logger.info("GET /api/classes/get-classes-by-status/{} → Fetching classes", status);

        try {
            List<ClassResponseDTO> allClasses = classService.getAllClasses();
            List<ClassResponseDTO> filteredClasses = allClasses.stream()
                    .filter(c -> status.equalsIgnoreCase(c.getStatus()))
                    .toList();

            logger.info("Returning {} classes with status: {}", filteredClasses.size(), status);
            return ResponseEntity.ok(filteredClasses);
        } catch (Exception e) {
            logger.error("Error fetching classes by status: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch classes. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //         UPDATE CLASS CAPACITY
    // ────────────────────────────────────────────────
    @PatchMapping("/update-class-capacity/{classId}")
    public ResponseEntity<?> updateClassCapacity(
            @PathVariable Long classId,
            @RequestParam Integer maxStudents,
            @RequestParam(required = false) Integer currentStudents) {
        logger.info("PATCH /api/classes/update-class-capacity/{} → Max: {}, Current: {}",
                classId, maxStudents, currentStudents);

        try {
            ClassResponseDTO existingClass = classService.getClassById(classId);

            ClassCreateRequestDTO updateDTO = new ClassCreateRequestDTO();
            updateDTO.setClassName(existingClass.getClassName());
            updateDTO.setClassCode(existingClass.getClassCode());
            updateDTO.setAcademicYear(existingClass.getAcademicYear());
            updateDTO.setSection(existingClass.getSection());
            updateDTO.setMaxStudents(maxStudents);
            updateDTO.setCurrentStudents(currentStudents != null ? currentStudents : existingClass.getCurrentStudents());
            updateDTO.setRoomNumber(existingClass.getRoomNumber());
            updateDTO.setStartTime(existingClass.getStartTime());
            updateDTO.setEndTime(existingClass.getEndTime());
            updateDTO.setDescription(existingClass.getDescription());
            updateDTO.setClassTeacherId(existingClass.getClassTeacherId());
            updateDTO.setClassTeacherSubject(existingClass.getClassTeacherSubject());
            updateDTO.setAssistantTeacherId(existingClass.getAssistantTeacherId());
            updateDTO.setAssistantTeacherSubject(existingClass.getAssistantTeacherSubject());
            updateDTO.setWorkingDays(existingClass.getWorkingDays());
            updateDTO.setStatus(existingClass.getStatus());
            updateDTO.setOtherTeacherSubject(existingClass.getOtherTeacherSubject());

            ClassResponseDTO updatedClass = classService.updateClass(classId, updateDTO);
            logger.info("Class capacity updated successfully for ID: {}", classId);
            return ResponseEntity.ok(updatedClass);
        } catch (Exception e) {
            logger.error("Error updating class capacity for ID: {}", classId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found or update failed. ID: " + classId);
        }
    }

    // ────────────────────────────────────────────────
    //        UPDATE CLASS SCHEDULE
    // ────────────────────────────────────────────────
    @PatchMapping("/update-class-schedule/{classId}")
    public ResponseEntity<?> updateClassSchedule(
            @PathVariable Long classId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestBody List<String> workingDays) {
        logger.info("PATCH /api/classes/update-class-schedule/{} → Start: {}, End: {}, Days: {}",
                classId, startTime, endTime, workingDays);

        try {
            ClassResponseDTO existingClass = classService.getClassById(classId);

            ClassCreateRequestDTO updateDTO = new ClassCreateRequestDTO();
            updateDTO.setClassName(existingClass.getClassName());
            updateDTO.setClassCode(existingClass.getClassCode());
            updateDTO.setAcademicYear(existingClass.getAcademicYear());
            updateDTO.setSection(existingClass.getSection());
            updateDTO.setMaxStudents(existingClass.getMaxStudents());
            updateDTO.setCurrentStudents(existingClass.getCurrentStudents());
            updateDTO.setRoomNumber(existingClass.getRoomNumber());
            updateDTO.setStartTime(startTime);
            updateDTO.setEndTime(endTime);
            updateDTO.setDescription(existingClass.getDescription());
            updateDTO.setClassTeacherId(existingClass.getClassTeacherId());
            updateDTO.setClassTeacherSubject(existingClass.getClassTeacherSubject());
            updateDTO.setAssistantTeacherId(existingClass.getAssistantTeacherId());
            updateDTO.setAssistantTeacherSubject(existingClass.getAssistantTeacherSubject());
            updateDTO.setWorkingDays(workingDays);
            updateDTO.setStatus(existingClass.getStatus());
            updateDTO.setOtherTeacherSubject(existingClass.getOtherTeacherSubject());

            ClassResponseDTO updatedClass = classService.updateClass(classId, updateDTO);
            logger.info("Class schedule updated successfully for ID: {}", classId);
            return ResponseEntity.ok(updatedClass);
        } catch (Exception e) {
            logger.error("Error updating class schedule for ID: {}", classId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found or update failed. ID: " + classId);
        }
    }

    // ────────────────────────────────────────────────
    //        GET CLASSES WITH AVAILABLE SEATS
    // ────────────────────────────────────────────────
    @GetMapping("/get-classes-with-available-seats")
    public ResponseEntity<?> getClassesWithAvailableSeats(@RequestParam(required = false) Integer minSeats) {
        logger.info("GET /api/classes/get-classes-with-available-seats → Min seats: {}", minSeats);

        try {
            List<ClassResponseDTO> allClasses = classService.getAllClasses();
            List<ClassResponseDTO> filteredClasses = allClasses.stream()
                    .filter(c -> {
                        if (c.getMaxStudents() == null || c.getCurrentStudents() == null) {
                            return false;
                        }
                        int availableSeats = c.getMaxStudents() - c.getCurrentStudents();
                        return minSeats == null ? availableSeats > 0 : availableSeats >= minSeats;
                    })
                    .toList();

            logger.info("Returning {} classes with available seats", filteredClasses.size());
            return ResponseEntity.ok(filteredClasses);
        } catch (Exception e) {
            logger.error("Error fetching classes with available seats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch classes. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //        BULK UPDATE CLASS STATUS
    // ────────────────────────────────────────────────
    @PatchMapping("/bulk-update-class-status")
    public ResponseEntity<?> bulkUpdateClassStatus(
            @RequestParam List<Long> classIds,
            @RequestParam String status) {
        logger.info("PATCH /api/classes/bulk-update-class-status → Updating {} classes to status: {}",
                classIds.size(), status);

        try {
            List<ClassResponseDTO> updatedClasses = new java.util.ArrayList<>();
            for (Long classId : classIds) {
                ClassResponseDTO updatedClass = classService.updateClassStatus(classId, status);
                updatedClasses.add(updatedClass);
            }

            logger.info("Bulk status update completed for {} classes", classIds.size());
            return ResponseEntity.ok(updatedClasses);
        } catch (Exception e) {
            logger.error("Error in bulk status update", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update class statuses. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //          GET CLASSES BY ROOM NUMBER
    // ────────────────────────────────────────────────
    @GetMapping("/get-classes-by-room/{roomNumber}")
    public ResponseEntity<?> getClassesByRoomNumber(@PathVariable String roomNumber) {
        logger.info("GET /api/classes/get-classes-by-room/{} → Fetching classes", roomNumber);

        try {
            List<ClassResponseDTO> allClasses = classService.getAllClasses();
            List<ClassResponseDTO> filteredClasses = allClasses.stream()
                    .filter(c -> roomNumber.equalsIgnoreCase(c.getRoomNumber()))
                    .toList();

            logger.info("Returning {} classes in room: {}", filteredClasses.size(), roomNumber);
            return ResponseEntity.ok(filteredClasses);
        } catch (Exception e) {
            logger.error("Error fetching classes by room: {}", roomNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch classes. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //          SEARCH CLASSES
    // ────────────────────────────────────────────────
    @GetMapping("/search-classes")
    public ResponseEntity<?> searchClasses(@RequestParam String keyword) {
        logger.info("GET /api/classes/search-classes → Searching with keyword: {}", keyword);

        try {
            List<ClassResponseDTO> allClasses = classService.getAllClasses();
            String lowerKeyword = keyword.toLowerCase();

            List<ClassResponseDTO> filteredClasses = allClasses.stream()
                    .filter(c ->
                            (c.getClassName() != null && c.getClassName().toLowerCase().contains(lowerKeyword)) ||
                                    (c.getClassCode() != null && c.getClassCode().toLowerCase().contains(lowerKeyword)) ||
                                    (c.getRoomNumber() != null && c.getRoomNumber().toLowerCase().contains(lowerKeyword)) ||
                                    (c.getDescription() != null && c.getDescription().toLowerCase().contains(lowerKeyword))
                    )
                    .toList();

            logger.info("Found {} classes matching keyword: {}", filteredClasses.size(), keyword);
            return ResponseEntity.ok(filteredClasses);
        } catch (Exception e) {
            logger.error("Error searching classes with keyword: {}", keyword, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to search classes. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //          GET CLASSES CREATED THIS MONTH
    // ────────────────────────────────────────────────
    @GetMapping("/get-recent-classes")
    public ResponseEntity<?> getRecentClasses(@RequestParam(defaultValue = "30") Integer days) {
        logger.info("GET /api/classes/get-recent-classes → Last {} days", days);

        try {
            List<ClassResponseDTO> allClasses = classService.getAllClasses();

            // Calculate date threshold
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_MONTH, -days);
            java.util.Date thresholdDate = cal.getTime();

            List<ClassResponseDTO> recentClasses = allClasses.stream()
                    .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().after(thresholdDate))
                    .toList();

            logger.info("Returning {} recent classes (last {} days)", recentClasses.size(), days);
            return ResponseEntity.ok(recentClasses);
        } catch (Exception e) {
            logger.error("Error fetching recent classes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch recent classes. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //       GET ALL SUBJECTS BY CLASS NAME & SECTION
    // ────────────────────────────────────────────────
    @GetMapping("/subjects/{className}/{section}")
    public ResponseEntity<?> getSubjectsByClassAndSection(
            @PathVariable String className,
            @PathVariable String section) {
        logger.info("GET /api/classes/subjects/{}/{} → Fetching subjects", className, section);

        try {
            ClassSubjectsResponseDTO response = classService.getSubjectsByClassAndSection(className, section);
            logger.info("Found {} subjects for class: {} {}", response.getSubjects().size(), className, section);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            logger.warn("Class not found: {} {}", className, section);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found with name: " + className + " and section: " + section);
        } catch (Exception e) {
            logger.error("Error fetching subjects for class: {} {}", className, section, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch subjects. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //         GET ALL SUBJECTS BY CLASS ID
    // ────────────────────────────────────────────────
    @GetMapping("/subjects/{classId}")
    public ResponseEntity<?> getSubjectsByClassId(@PathVariable Long classId) {
        logger.info("GET /api/classes/subjects/{} → Fetching subjects", classId);

        try {
            ClassSubjectsResponseDTO response = classService.getSubjectsByClassId(classId);
            logger.info("Found {} subjects for class ID: {}", response.getSubjects().size(), classId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            logger.warn("Class not found with ID: {}", classId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found with ID: " + classId);
        } catch (Exception e) {
            logger.error("Error fetching subjects for class ID: {}", classId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch subjects. Please try again later.");
        }
    }

    // ────────────────────────────────────────────────
    //       GET ONLY SUBJECTS LIST BY CLASS ID
    // ────────────────────────────────────────────────
    @GetMapping("/subjects-list/{classId}")
    public ResponseEntity<?> getSubjectsList(@PathVariable Long classId) {
        logger.info("GET /api/classes/subjects-list/{} → Fetching subjects list", classId);

        try {
            ClassSubjectsResponseDTO response = classService.getSubjectsByClassId(classId);
            logger.info("Found {} subjects for class ID: {}", response.getSubjects().size(), classId);
            return ResponseEntity.ok(response.getSubjects());
        } catch (ResourceNotFoundException e) {
            logger.warn("Class not found with ID: {}", classId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Class not found with ID: " + classId);
        } catch (Exception e) {
            logger.error("Error fetching subjects list for class ID: {}", classId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch subjects. Please try again later.");
        }
    }
}