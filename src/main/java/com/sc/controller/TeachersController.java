package com.sc.controller;

import com.sc.bcrypt.BcryptEncoderConfig;
import com.sc.dto.request.TeacherRequestDto;
import com.sc.dto.response.TeacherResponseDto;
import com.sc.entity.TeacherEntity;
import com.sc.service.TeacherService;
import com.sc.repository.TeacherRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/teachers")
public class TeachersController {

    private static final Logger logger = LoggerFactory.getLogger(TeachersController.class);
    private final TeacherService teacherService;

    // Directory where files will be saved
    private static final String UPLOAD_DIR = "uploads/teachers";

    public TeachersController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @Autowired
    private BcryptEncoderConfig passwordEncoder;

    @Autowired
    private TeacherRepository teacherRepository;

    // ========== CREATE ENDPOINT ==========
    @PostMapping(value = "/create-teacher", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createTeacher(
            @RequestPart("teacherData") String teacherDataJson,
            @RequestPart(value = "teacherPhoto", required = false) MultipartFile teacherPhoto,
            @RequestPart(value = "aadharDocument", required = false) MultipartFile aadharDocument,
            @RequestPart(value = "panDocument", required = false) MultipartFile panDocument,
            @RequestPart(value = "educationDocument", required = false) MultipartFile educationDocument,
            @RequestPart(value = "bedDocument", required = false) MultipartFile bedDocument,
            @RequestPart(value = "experienceDocument", required = false) MultipartFile experienceDocument,
            @RequestPart(value = "policeVerificationDocument", required = false) MultipartFile policeVerificationDocument,
            @RequestPart(value = "medicalFitnessDocument", required = false) MultipartFile medicalFitnessDocument,
            @RequestPart(value = "resumeDocument", required = false) MultipartFile resumeDocument,
            @RequestHeader(value = "X-Created-By", defaultValue = "system") String createdBy) {

        logger.info("POST /api/teachers/create-teacher - Creating teacher");
        logger.info("Received teacher data JSON: {}", teacherDataJson);

        try {
            // Parse JSON to DTO with proper date handling
            TeacherRequestDto requestDto = parseTeacherRequestDto(teacherDataJson);

            // Validate required fields
            validateRequiredFields(requestDto);

            // Create files map
            Map<String, MultipartFile> files = new HashMap<>();
            addFileToMap(files, "teacherPhoto", teacherPhoto);
            addFileToMap(files, "aadharDocument", aadharDocument);
            addFileToMap(files, "panDocument", panDocument);
            addFileToMap(files, "educationDocument", educationDocument);
            addFileToMap(files, "bedDocument", bedDocument);
            addFileToMap(files, "experienceDocument", experienceDocument);
            addFileToMap(files, "policeVerificationDocument", policeVerificationDocument);
            addFileToMap(files, "medicalFitnessDocument", medicalFitnessDocument);
            addFileToMap(files, "resumeDocument", resumeDocument);

            // Log file information
            logFileInfo("teacherPhoto", teacherPhoto);
            logFileInfo("aadharDocument", aadharDocument);
            logFileInfo("panDocument", panDocument);
            logFileInfo("educationDocument", educationDocument);

            // Call service
            TeacherResponseDto responseDto = teacherService.createTeacher(requestDto, files, createdBy);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createSuccessResponse("Teacher created successfully", responseDto));

        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Validation error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating teacher", e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create teacher: " + e.getMessage()));
        }
    }

    // Add this to your TeachersController.java
    @GetMapping("/all")
    public ResponseEntity<?> getAllTeachersAlternate() {
        logger.info("GET /api/teachers/all - Fetching all teachers (alternate endpoint)");
        try {
            List<TeacherResponseDto> teachers = teacherService.getAllTeachers();
            return ResponseEntity.ok(createSuccessResponse("Teachers retrieved successfully", teachers));
        } catch (Exception e) {
            logger.error("Error fetching teachers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch teachers"));
        }
    }

    // ========== READ ENDPOINTS ==========

    // GET ALL TEACHERS
    @GetMapping("/get-all-teachers")
    public ResponseEntity<?> getAllTeachers() {
        logger.info("GET /api/teachers/get-all-teachers - Fetching all teachers");
        try {
            List<TeacherResponseDto> teachers = teacherService.getAllTeachers();
            return ResponseEntity.ok(createSuccessResponse("Teachers retrieved successfully", teachers));
        } catch (Exception e) {
            logger.error("Error fetching teachers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch teachers"));
        }
    }

    // GET TEACHER BY ID
    @GetMapping("/get-teacher-by-id/{id}")
    public ResponseEntity<?> getTeacherById(@PathVariable Long id) {
        logger.info("GET /api/teachers/get-teacher-by-id/{}", id);
        try {
            TeacherResponseDto teacher = teacherService.getTeacherById(id);
            return ResponseEntity.ok(createSuccessResponse("Teacher retrieved successfully", teacher));
        } catch (Exception e) {
            logger.error("Error fetching teacher: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Teacher not found with ID: " + id));
        }
    }

    // GET TEACHER BY TEACHER CODE
    @GetMapping("/get-teacher-by-code/{teacherCode}")
    public ResponseEntity<?> getTeacherByTeacherCode(@PathVariable String teacherCode) {
        logger.info("GET /api/teachers/get-teacher-by-code/{}", teacherCode);
        try {
            TeacherResponseDto teacher = teacherService.getTeacherByTeacherCode(teacherCode);
            return ResponseEntity.ok(createSuccessResponse("Teacher retrieved successfully", teacher));
        } catch (Exception e) {
            logger.error("Error fetching teacher: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Teacher not found with code: " + teacherCode));
        }
    }

    // GET TEACHER BY EMPLOYEE ID
    @GetMapping("/get-teacher-by-employee-id/{employeeId}")
    public ResponseEntity<?> getTeacherByEmployeeId(@PathVariable String employeeId) {
        logger.info("GET /api/teachers/get-teacher-by-employee-id/{}", employeeId);
        try {
            TeacherResponseDto teacher = teacherService.getTeacherByEmployeeId(employeeId);
            return ResponseEntity.ok(createSuccessResponse("Teacher retrieved successfully", teacher));
        } catch (Exception e) {
            logger.error("Error fetching teacher: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Teacher not found with employee ID: " + employeeId));
        }
    }

    // GET ACTIVE TEACHERS
    @GetMapping("/get-active-teachers")
    public ResponseEntity<?> getActiveTeachers() {
        logger.info("GET /api/teachers/get-active-teachers");
        try {
            List<TeacherResponseDto> teachers = teacherService.getActiveTeachers();
            return ResponseEntity.ok(createSuccessResponse("Active teachers retrieved successfully", teachers));
        } catch (Exception e) {
            logger.error("Error fetching active teachers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch active teachers"));
        }
    }

    // GET TEACHERS BY STATUS
    @GetMapping("/get-teachers-by-status/{status}")
    public ResponseEntity<?> getTeachersByStatus(@PathVariable String status) {
        logger.info("GET /api/teachers/get-teachers-by-status/{}", status);
        try {
            List<TeacherResponseDto> teachers = teacherService.getTeachersByStatus(status);
            return ResponseEntity.ok(createSuccessResponse("Teachers retrieved successfully", teachers));
        } catch (Exception e) {
            logger.error("Error fetching teachers by status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch teachers"));
        }
    }

    // GET TEACHERS BY DEPARTMENT
    @GetMapping("/get-teachers-by-department/{department}")
    public ResponseEntity<?> getTeachersByDepartment(@PathVariable String department) {
        logger.info("GET /api/teachers/get-teachers-by-department/{}", department);
        try {
            List<TeacherResponseDto> teachers = teacherService.getTeachersByDepartment(department);
            return ResponseEntity.ok(createSuccessResponse("Teachers retrieved successfully", teachers));
        } catch (Exception e) {
            logger.error("Error fetching teachers by department: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch teachers"));
        }
    }

    // SEARCH TEACHERS BY NAME
    @GetMapping("/search-teachers")
    public ResponseEntity<?> searchTeachers(@RequestParam String name) {
        logger.info("GET /api/teachers/search-teachers?name={}", name);
        try {
            List<TeacherResponseDto> teachers = teacherService.searchTeachersByName(name);
            return ResponseEntity.ok(createSuccessResponse("Teachers found successfully", teachers));
        } catch (Exception e) {
            logger.error("Error searching teachers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to search teachers"));
        }
    }

    // ========== UPDATE ENDPOINTS ==========

    // UPDATE TEACHER (without files)
    @PutMapping("/update-teacher/{id}")
    public ResponseEntity<?> updateTeacher(
            @PathVariable Long id,
            @RequestBody TeacherRequestDto requestDto) {
        logger.info("PUT /api/teachers/update-teacher/{}", id);
        try {
            TeacherResponseDto response = teacherService.updateTeacher(id, requestDto);
            return ResponseEntity.ok(createSuccessResponse("Teacher updated successfully", response));
        } catch (Exception e) {
            logger.error("Error updating teacher: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // UPDATE TEACHER WITH FILES
    @PutMapping(value = "/update-with-files/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateTeacherWithFiles(
            @PathVariable Long id,
            @RequestPart("teacherData") String teacherDataJson,
            @RequestPart(value = "teacherPhoto", required = false) MultipartFile teacherPhoto,
            @RequestPart(value = "aadharDocument", required = false) MultipartFile aadharDocument,
            @RequestPart(value = "panDocument", required = false) MultipartFile panDocument,
            @RequestPart(value = "educationDocument", required = false) MultipartFile educationDocument,
            @RequestPart(value = "bedDocument", required = false) MultipartFile bedDocument,
            @RequestPart(value = "experienceDocument", required = false) MultipartFile experienceDocument,
            @RequestPart(value = "policeVerificationDocument", required = false) MultipartFile policeVerificationDocument,
            @RequestPart(value = "medicalFitnessDocument", required = false) MultipartFile medicalFitnessDocument,
            @RequestPart(value = "resumeDocument", required = false) MultipartFile resumeDocument) {

        logger.info("PUT /api/teachers/update-with-files/{}", id);
        try {
            TeacherRequestDto requestDto = parseTeacherRequestDto(teacherDataJson);

            Map<String, MultipartFile> files = new HashMap<>();
            addFileToMap(files, "teacherPhoto", teacherPhoto);
            addFileToMap(files, "aadharDocument", aadharDocument);
            addFileToMap(files, "panDocument", panDocument);
            addFileToMap(files, "educationDocument", educationDocument);
            addFileToMap(files, "bedDocument", bedDocument);
            addFileToMap(files, "experienceDocument", experienceDocument);
            addFileToMap(files, "policeVerificationDocument", policeVerificationDocument);
            addFileToMap(files, "medicalFitnessDocument", medicalFitnessDocument);
            addFileToMap(files, "resumeDocument", resumeDocument);

            TeacherResponseDto response = teacherService.updateTeacherWithFiles(id, requestDto, files);
            return ResponseEntity.ok(createSuccessResponse("Teacher updated successfully with files", response));
        } catch (Exception e) {
            logger.error("Error updating teacher with files: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // UPDATE TEACHER STATUS
    @PatchMapping("/update-teacher-status/{teacherCode}")
    public ResponseEntity<?> updateTeacherStatus(
            @PathVariable String teacherCode,
            @RequestParam String status) {
        logger.info("PATCH /api/teachers/update-teacher-status/{}?status={}", teacherCode, status);
        try {
            TeacherResponseDto response = teacherService.updateTeacherStatus(teacherCode, status);
            return ResponseEntity.ok(createSuccessResponse("Teacher status updated successfully", response));
        } catch (Exception e) {
            logger.error("Error updating teacher status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/authenticate-by-phone")
    public ResponseEntity<?> authenticateByPhone(@RequestBody Map<String, String> credentials) {
        String contactNumber = credentials.get("contactNumber");
        String password = credentials.get("password");

        logger.info("Authenticating teacher by phone: {}", contactNumber);

        if (contactNumber == null || contactNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Contact number is required",
                    "authenticated", false
            ));
        }

        try {
            Optional<TeacherEntity> teacherOpt = teacherRepository.findByContactNumber(contactNumber);

            if (teacherOpt.isEmpty()) {
                logger.warn("Teacher not found with contact number: {}", contactNumber);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "authenticated", false,
                        "error", "Invalid contact number or password"
                ));
            }

            TeacherEntity teacher = teacherOpt.get();

            if (teacher.isDeleted()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "authenticated", false,
                        "error", "Teacher account has been deleted"
                ));
            }

            if (!Boolean.TRUE.equals(teacher.getActive())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "authenticated", false,
                        "error", "Teacher account is inactive"
                ));
            }

            boolean authenticated = teacherService.authenticateTeacher(teacher.getEmployeeId(), password);

            if (authenticated) {
                TeacherResponseDto teacherDto = teacherService.getTeacherByEmployeeId(teacher.getEmployeeId());
                Map<String, Object> response = new HashMap<>();
                response.put("authenticated", true);
                response.put("teacher", teacherDto);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "authenticated", false,
                        "error", "Invalid contact number or password"
                ));
            }

        } catch (Exception e) {
            logger.error("Error authenticating by phone", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "authenticated", false,
                    "error", "Authentication failed"
            ));
        }
    }

    // UPDATE TEACHER PASSWORD
    @PatchMapping("/update-teacher-password/{teacherCode}")
    public ResponseEntity<?> updateTeacherPassword(
            @PathVariable String teacherCode,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword) {
        logger.info("PATCH /api/teachers/update-teacher-password/{}", teacherCode);
        try {
            TeacherResponseDto response = teacherService.updateTeacherPassword(teacherCode, newPassword, confirmPassword);
            return ResponseEntity.ok(createSuccessResponse("Teacher password updated successfully", response));
        } catch (Exception e) {
            logger.error("Error updating teacher password: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // ========== DOCUMENT OPERATIONS ==========

    // UPLOAD DOCUMENTS (update document names)
    @PostMapping("/upload-documents/{teacherCode}")
    public ResponseEntity<?> uploadDocuments(
            @PathVariable String teacherCode,
            @RequestBody Map<String, String> fileNames) {
        logger.info("POST /api/teachers/upload-documents/{}", teacherCode);
        try {
            TeacherResponseDto response = teacherService.uploadDocuments(teacherCode, fileNames);
            return ResponseEntity.ok(createSuccessResponse("Documents uploaded successfully", response));
        } catch (Exception e) {
            logger.error("Error uploading documents: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // UPDATE SINGLE DOCUMENT
    @PostMapping("/update-single-document/{teacherCode}/{documentType}")
    public ResponseEntity<?> updateSingleDocument(
            @PathVariable String teacherCode,
            @PathVariable String documentType,
            @RequestParam("file") MultipartFile file) {
        logger.info("POST /api/teachers/update-single-document/{}/{}", teacherCode, documentType);
        try {
            TeacherResponseDto response = teacherService.updateSingleDocument(teacherCode, documentType, file);
            return ResponseEntity.ok(createSuccessResponse("Document updated successfully", response));
        } catch (Exception e) {
            logger.error("Error updating document: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // ========== DELETE OPERATIONS ==========

    // SOFT DELETE TEACHER
    @DeleteMapping("/soft-delete-teacher/{teacherCode}")
    public ResponseEntity<?> softDeleteTeacher(@PathVariable String teacherCode) {
        logger.info("DELETE /api/teachers/soft-delete-teacher/{}", teacherCode);
        try {
            teacherService.softDeleteTeacher(teacherCode);
            return ResponseEntity.ok(createMessageResponse("Teacher soft deleted successfully"));
        } catch (Exception e) {
            logger.error("Error soft deleting teacher: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Teacher not found with code: " + teacherCode));
        }
    }

    // ========== PHOTO SERVING ENDPOINT (FALLBACK) ==========
    @GetMapping("/photo/{id}")
    public ResponseEntity<byte[]> getTeacherPhoto(@PathVariable Long id) {
        logger.info("GET /api/teachers/photo/{}", id);
        try {
            Optional<TeacherEntity> teacherOpt = teacherRepository.findById(id);
            if (teacherOpt.isPresent()) {
                TeacherEntity teacher = teacherOpt.get();
                if (teacher.getTeacherPhoto() != null && teacher.getTeacherPhoto().length > 0) {
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .body(teacher.getTeacherPhoto());
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching teacher photo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // HARD DELETE TEACHER
    @DeleteMapping("/delete-teacher/{id}")
    public ResponseEntity<?> hardDeleteTeacher(@PathVariable Long id) {
        logger.info("DELETE /api/teachers/delete-teacher/{}", id);
        try {
            teacherService.deleteTeacher(id);
            return ResponseEntity.ok(createMessageResponse("Teacher permanently deleted"));
        } catch (Exception e) {
            logger.error("Error deleting teacher: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Teacher not found with ID: " + id));
        }
    }

    // ========== VALIDATION ENDPOINTS ==========

    @GetMapping("/check-email-exists/{email}")
    public ResponseEntity<?> checkEmailExists(@PathVariable String email) {
        boolean exists = teacherService.isEmailExists(email);
        return ResponseEntity.ok(Map.of("exists", exists, "email", email));
    }

    @GetMapping("/check-contact-exists/{contactNumber}")
    public ResponseEntity<?> checkContactExists(@PathVariable String contactNumber) {
        boolean exists = teacherService.isContactNumberExists(contactNumber);
        return ResponseEntity.ok(Map.of("exists", exists, "contactNumber", contactNumber));
    }

    @GetMapping("/check-aadhar-exists/{aadharNumber}")
    public ResponseEntity<?> checkAadharExists(@PathVariable String aadharNumber) {
        boolean exists = teacherService.isAadharExists(aadharNumber);
        return ResponseEntity.ok(Map.of("exists", exists, "aadharNumber", aadharNumber));
    }

    @GetMapping("/check-pan-exists/{panNumber}")
    public ResponseEntity<?> checkPanExists(@PathVariable String panNumber) {
        boolean exists = teacherService.isPanExists(panNumber);
        return ResponseEntity.ok(Map.of("exists", exists, "panNumber", panNumber));
    }

    @GetMapping("/check-employee-id-exists/{employeeId}")
    public ResponseEntity<?> checkEmployeeIdExists(@PathVariable String employeeId) {
        boolean exists = teacherService.isEmployeeIdExists(employeeId);
        return ResponseEntity.ok(Map.of("exists", exists, "employeeId", employeeId));
    }

    @GetMapping("/check-teacher-code-exists/{teacherCode}")
    public ResponseEntity<?> checkTeacherCodeExists(@PathVariable String teacherCode) {
        boolean exists = teacherService.isTeacherCodeExists(teacherCode);
        return ResponseEntity.ok(Map.of("exists", exists, "teacherCode", teacherCode));
    }

    // ========== UTILITY ENDPOINTS ==========

    @GetMapping("/generate-teacher-code")
    public ResponseEntity<?> generateTeacherCode() {
        String teacherCode = teacherService.generateTeacherCode();
        return ResponseEntity.ok(Map.of("teacherCode", teacherCode));
    }

    @GetMapping("/get-teacher-count")
    public ResponseEntity<?> getTeacherCount() {
        long count = teacherService.getTeacherCount();
        return ResponseEntity.ok(Map.of("total", count));
    }

    @GetMapping("/get-active-teacher-count")
    public ResponseEntity<?> getActiveTeacherCount() {
        long count = teacherService.getActiveTeacherCount();
        return ResponseEntity.ok(Map.of("active", count));
    }

    @GetMapping("/get-all-departments")
    public ResponseEntity<?> getAllDepartments() {
        List<String> departments = teacherService.getAllDepartments();
        return ResponseEntity.ok(Map.of("departments", departments));
    }

    // ========== AUTHENTICATION ENDPOINTS ==========

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody Map<String, String> credentials) {
        String employeeId = credentials.get("employeeId");
        String password = credentials.get("password");

        logger.info("=== TEACHER AUTHENTICATION DEBUG ===");
        logger.info("Employee ID received: {}", employeeId);
        logger.info("Password received: {}", password != null ? "YES (length: " + password.length() + ")" : "NO");

        if (employeeId == null || employeeId.trim().isEmpty()) {
            logger.warn("Employee ID is missing");
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Employee ID is required",
                    "authenticated", false
            ));
        }

        if (password == null || password.trim().isEmpty()) {
            logger.warn("Password is missing for employee: {}", employeeId);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Password is required",
                    "authenticated", false
            ));
        }

        try {
            // First check if teacher exists in database
            Optional<TeacherEntity> teacherOpt = teacherRepository.findByEmployeeId(employeeId);

            if (teacherOpt.isEmpty()) {
                logger.warn("Teacher NOT FOUND with employeeId: {}", employeeId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "authenticated", false,
                        "error", "Invalid employee ID or password"
                ));
            }

            TeacherEntity teacher = teacherOpt.get();
            logger.info("Teacher found: {}", teacher.getEmployeeId());
            logger.info("Teacher active status: {}", teacher.getActive());
            logger.info("Teacher deleted status: {}", teacher.isDeleted());
            logger.info("Stored password hash exists: {}", teacher.getTeacherPassword() != null);

            if (teacher.getTeacherPassword() != null) {
                logger.info("Stored password hash: {}", teacher.getTeacherPassword().substring(0, Math.min(30, teacher.getTeacherPassword().length())) + "...");
            }

            // Check if teacher is deleted
            if (teacher.isDeleted()) {
                logger.warn("Teacher account is deleted: {}", employeeId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "authenticated", false,
                        "error", "Teacher account has been deleted. Please contact administrator."
                ));
            }

            // Check if teacher is active
            if (!Boolean.TRUE.equals(teacher.getActive())) {
                logger.warn("Teacher account is inactive: {}", employeeId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "authenticated", false,
                        "error", "Teacher account is inactive. Please contact administrator."
                ));
            }

            // Verify password
            boolean authenticated = teacherService.authenticateTeacher(employeeId, password);
            logger.info("Password authentication result: {}", authenticated);

            if (authenticated) {
                TeacherResponseDto teacherDto = teacherService.getTeacherByEmployeeId(employeeId);
                Map<String, Object> response = new HashMap<>();
                response.put("authenticated", true);
                response.put("teacher", teacherDto);
                response.put("message", "Authentication successful");

                logger.info("Teacher authenticated successfully: {}", employeeId);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Invalid password for teacher: {}", employeeId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "authenticated", false,
                        "error", "Invalid employee ID or password"
                ));
            }

        } catch (Exception e) {
            logger.error("Unexpected error during teacher authentication", e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "authenticated", false,
                    "error", "Authentication failed: " + e.getMessage(),
                    "errorType", e.getClass().getSimpleName()
            ));
        }
    }

    @PostMapping("/authenticate-by-code")
    public ResponseEntity<?> authenticateByCode(@RequestBody Map<String, String> credentials) {
        String teacherCode = credentials.get("teacherCode");
        String password = credentials.get("password");

        logger.info("POST /api/teachers/authenticate-by-code - Authenticating code: {}", teacherCode);

        try {
            boolean authenticated = teacherService.authenticateTeacherByCode(teacherCode, password);

            if (authenticated) {
                TeacherResponseDto teacher = teacherService.getTeacherByTeacherCode(teacherCode);
                Map<String, Object> response = new HashMap<>();
                response.put("authenticated", true);
                response.put("teacher", teacher);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid credentials"));
            }
        } catch (Exception e) {
            logger.error("Error authenticating: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Authentication failed"));
        }
    }

    // ========== FILE SERVING ENDPOINT ==========

    @GetMapping("/uploads/teachers/{fileName}")
    public ResponseEntity<?> getTeacherDocument(@PathVariable String fileName) {
        logger.info("GET /api/teachers/uploads/teachers/{}", fileName);
        try {
            String uploadDir = "uploads/teachers/";
            Path filePath = Paths.get(uploadDir + fileName);

            if (!Files.exists(filePath)) {
                logger.warn("File not found: {}", filePath.toString());
                return ResponseEntity.notFound().build();
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                    .body(fileBytes);

        } catch (IOException e) {
            logger.error("Error serving file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error reading file: " + e.getMessage()));
        }
    }

    // ========== DEBUG ENDPOINT ==========

    @PostMapping("/debug-parse")
    public ResponseEntity<?> debugParse(@RequestBody String json) {
        try {
            logger.info("Debug parsing JSON: {}", json);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            JsonNode rootNode = objectMapper.readTree(json);
            TeacherRequestDto dto = objectMapper.treeToValue(rootNode, TeacherRequestDto.class);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("employeeId", dto.getEmployeeId());
            result.put("firstName", dto.getFirstName());
            result.put("email", dto.getEmail());
            result.put("passwordPresent", dto.getTeacherPassword() != null);
            result.put("confirmPasswordPresent", dto.getConfirmTeacherPassword() != null);
            result.put("rawJsonFields", rootNode.fieldNames());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Debug parse error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Parse error: " + e.getMessage()));
        }
    }

    // ========== HEALTH CHECK ==========

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "Teacher Management API");
        return ResponseEntity.ok(response);
    }

    // ========== TEACHER CLASSES ENDPOINT ==========
// Add this after your getTeacherByEmployeeId endpoint (around line 130-140)

    @GetMapping("/{teacherId}/classes")
    public ResponseEntity<?> getTeacherClasses(@PathVariable Long teacherId) {
        logger.info("GET /api/teachers/{}/classes", teacherId);
        try {
            // First verify teacher exists
            TeacherResponseDto teacher = teacherService.getTeacherById(teacherId);

            // Get classes from teacher's assigned classes
            List<?> classes = teacherService.getTeacherClasses(teacherId);

            return ResponseEntity.ok(createSuccessResponse("Classes retrieved successfully", classes));
        } catch (Exception e) {
            logger.error("Error fetching teacher classes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch classes: " + e.getMessage()));
        }
    }

    // ========== HELPER METHODS ==========

    private TeacherRequestDto parseTeacherRequestDto(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Register JavaTimeModule for better date handling
            objectMapper.registerModule(new JavaTimeModule());

            // Set the date format to accept yyyy-MM-dd
            objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

            // Configure to not fail on unknown properties
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // Parse JSON
            JsonNode rootNode = objectMapper.readTree(json);
            logger.info("JSON root field names: {}", rootNode.fieldNames());

            // Log date fields for debugging
            if (rootNode.has("dob")) {
                logger.info("DOB value: {}", rootNode.get("dob").asText());
            }
            if (rootNode.has("joiningDate")) {
                logger.info("JoiningDate value: {}", rootNode.get("joiningDate").asText());
            }

            // Ensure confirmTeacherPassword is set
            if (rootNode.has("teacherPassword") && !rootNode.has("confirmTeacherPassword")) {
                ((com.fasterxml.jackson.databind.node.ObjectNode) rootNode)
                        .put("confirmTeacherPassword", rootNode.get("teacherPassword").asText());
            }

            // Convert to DTO
            TeacherRequestDto dto = objectMapper.treeToValue(rootNode, TeacherRequestDto.class);

            // Log DTO state
            logger.info("Parsed DTO - FirstName: {}, DOB: {}, JoiningDate: {}",
                    dto.getFirstName(), dto.getDob(), dto.getJoiningDate());

            return dto;
        } catch (Exception e) {
            logger.error("Error parsing teacher JSON: {}", e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid teacher data format: " + e.getMessage());
        }
    }

    private void validateRequiredFields(TeacherRequestDto dto) {
        List<String> missingFields = new ArrayList<>();

        if (dto.getTeacherPassword() == null || dto.getTeacherPassword().trim().isEmpty()) {
            missingFields.add("teacherPassword");
        }
        if (dto.getConfirmTeacherPassword() == null || dto.getConfirmTeacherPassword().trim().isEmpty()) {
            missingFields.add("confirmTeacherPassword");
        }
        if (dto.getEmployeeId() == null || dto.getEmployeeId().trim().isEmpty()) {
            missingFields.add("employeeId");
        }
        if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) {
            missingFields.add("firstName");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            missingFields.add("email");
        }
        if (dto.getAadharNumber() == null || dto.getAadharNumber().trim().isEmpty()) {
            missingFields.add("aadharNumber");
        }

        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException("Missing required fields: " + String.join(", ", missingFields));
        }
    }

    private void addFileToMap(Map<String, MultipartFile> files, String key, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            files.put(key, file);
        }
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    private Map<String, Object> createMessageResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    private void logFileInfo(String fileType, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            logger.info("  - {}: {} ({} bytes)", fileType, file.getOriginalFilename(), file.getSize());
        }
    }
}