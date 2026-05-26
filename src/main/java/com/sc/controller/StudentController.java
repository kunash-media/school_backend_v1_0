package com.sc.controller;

import com.sc.bcrypt.BcryptEncoderConfig;
import com.sc.dto.request.StudentRequestDto;
import com.sc.dto.response.StudentResponseDto;
import com.sc.entity.ClassEntity;
import com.sc.entity.StudentClassEnrollment;
import com.sc.entity.StudentEntity;
import com.sc.repository.ClassRepository;
import com.sc.repository.StudentRepository;
import com.sc.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sc.util.StudentIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private StudentIdGenerator studentIdGenerator;

    @Autowired
    private BcryptEncoderConfig passwordEncoder;

    // ============= 📝 CREATE STUDENT =============

    @PostMapping("/create-student")
    public ResponseEntity<?> createStudent(@RequestBody StudentRequestDto requestDto) {
        logger.info("Creating student: {}", requestDto.getStudentRollNumber());
        try {
            validateStudentRequest(requestDto);
            StudentResponseDto response = studentService.createStudent(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ============= 📝 CREATE STUDENT WITH FILES =============

    @PostMapping(value = "/create-student-with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createStudentWithFiles(
            @RequestPart("studentData") String studentDataJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "studentAadharImage", required = false) MultipartFile studentAadharImage,
            @RequestPart(value = "fatherAadharImage", required = false) MultipartFile fatherAadharImage,
            @RequestPart(value = "motherAadharImage", required = false) MultipartFile motherAadharImage,
            @RequestPart(value = "birthCertificateImage", required = false) MultipartFile birthCertificateImage,
            @RequestPart(value = "transferCertificateImage", required = false) MultipartFile transferCertificateImage,
            @RequestPart(value = "markSheetImage", required = false) MultipartFile markSheetImage,
            @RequestPart(value = "incomeCertificateImage", required = false) MultipartFile incomeCertificateImage,
            @RequestPart(value = "casteCertificateImage", required = false) MultipartFile casteCertificateImage) {

        logger.info("Creating student with multipart data");

        try {
            objectMapper.registerModule(new JavaTimeModule());
            StudentRequestDto requestDto = objectMapper.readValue(studentDataJson, StudentRequestDto.class);

            if (profileImage != null && !profileImage.isEmpty()) {
                requestDto.setProfileImage(profileImage);
            }
            if (studentAadharImage != null && !studentAadharImage.isEmpty()) {
                requestDto.setStudentAadharImage(studentAadharImage);
            }
            if (fatherAadharImage != null && !fatherAadharImage.isEmpty()) {
                requestDto.setFatherAadharImage(fatherAadharImage);
            }
            if (motherAadharImage != null && !motherAadharImage.isEmpty()) {
                requestDto.setMotherAadharImage(motherAadharImage);
            }
            if (birthCertificateImage != null && !birthCertificateImage.isEmpty()) {
                requestDto.setBirthCertificateImage(birthCertificateImage);
            }
            if (transferCertificateImage != null && !transferCertificateImage.isEmpty()) {
                requestDto.setTransferCertificateImage(transferCertificateImage);
            }
            if (markSheetImage != null && !markSheetImage.isEmpty()) {
                requestDto.setMarkSheetImage(markSheetImage);
            }
            if (incomeCertificateImage != null && !incomeCertificateImage.isEmpty()) {
                requestDto.setIncomeCertificateImage(incomeCertificateImage);
            }
            if (casteCertificateImage != null && !casteCertificateImage.isEmpty()) {
                requestDto.setCasteCertificateImage(casteCertificateImage);
            }

            validateStudentRequest(requestDto);
            StudentResponseDto response = studentService.createStudent(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating student with files: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/test-create")
    public ResponseEntity<?> testCreate(@RequestBody StudentRequestDto requestDto) {
        Map<String, Object> result = new HashMap<>();

        try {
            result.put("step1", "Received request");
            result.put("classId", requestDto.getClassId());

            Optional<ClassEntity> classOpt = classRepository.findById(requestDto.getClassId());
            if (classOpt.isEmpty()) {
                result.put("error", "Class not found with ID: " + requestDto.getClassId());
                return ResponseEntity.badRequest().body(result);
            }
            result.put("classFound", classOpt.get().getClassName());

            String rollNumber = studentIdGenerator.generateUniqueRollNumber(
                    classOpt.get().getClassName(),
                    classOpt.get().getSection()
            );
            result.put("rollNumber", rollNumber);

            String studentId = studentIdGenerator.generateUniqueStudentId();
            result.put("studentId", studentId);

            StudentEntity student = new StudentEntity();
            student.setStudentId(studentId);
            student.setStudentRollNumber(rollNumber);
            student.setFirstName(requestDto.getFirstName());
            student.setLastName(requestDto.getLastName());
            if (requestDto.getMiddleName() != null) {
                student.setMiddleName(requestDto.getMiddleName());
            }

            if (requestDto.getStudentPassword() != null && !requestDto.getStudentPassword().isEmpty()) {
                String encrypted = passwordEncoder.encode(requestDto.getStudentPassword());
                student.setStudentPassword(encrypted);
                result.put("passwordEncrypted", true);
            } else {
                result.put("passwordEncrypted", false);
                result.put("passwordError", "Password is null or empty");
            }

            student.setRole("STUDENT");
            student.setStatus("Active");
            student.setDateOfBirth(requestDto.getDateOfBirth());
            student.setGender(requestDto.getGender());
            student.setAadharNumber(requestDto.getAadharNumber());
            student.setFatherName(requestDto.getFatherName());
            student.setFatherPhone(requestDto.getFatherPhone());

            StudentClassEnrollment enrollment = new StudentClassEnrollment(student, classOpt.get());
            student.addEnrollment(enrollment);
            student.setCurrentClass(classOpt.get().getClassName());
            student.setSection(classOpt.get().getSection());
            student.setAcademicYear(classOpt.get().getAcademicYear());

            StudentEntity saved = studentRepository.save(student);
            result.put("savedId", saved.getStdId());
            result.put("savedStudentId", saved.getStudentId());
            result.put("savedRollNumber", saved.getStudentRollNumber());

            result.put("success", true);
            result.put("message", "Student created successfully");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            result.put("stackTrace", e.toString());
            e.printStackTrace();
            return ResponseEntity.status(500).body(result);
        }
    }

    // ============= 🔍 GET ALL STUDENTS =============

    @GetMapping("/get-all-students")
    public ResponseEntity<Page<StudentResponseDto>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort;

        if (sortBy != null && !sortBy.isBlank()) {
            Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(dir, sortBy);
        } else {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<StudentResponseDto> studentsPage = studentService.getAllStudents(pageable);
        return ResponseEntity.ok(studentsPage);
    }

    // ============= 🔍 GET STUDENT BY ID =============

    @GetMapping("/get-student-by-id/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable Long id) {
        try {
            StudentResponseDto student = studentService.getStudentById(id);
            if (student != null) {
                return ResponseEntity.ok(student);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error getting student by ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting student: " + e.getMessage());
        }
    }

    // ============= 🔍 GET STUDENT BY ROLL NUMBER =============

    @GetMapping("/get-student-by-roll-number/{rollNumber}")
    public ResponseEntity<?> getStudentByRollNumber(@PathVariable String rollNumber) {
        try {
            StudentResponseDto student = studentService.getStudentByRollNumber(rollNumber);
            if (student != null) {
                return ResponseEntity.ok(student);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student not found with Roll Number: " + rollNumber);
        } catch (Exception e) {
            logger.error("Error getting student by roll number {}: {}", rollNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting student: " + e.getMessage());
        }
    }

    // ============= 🔍 GET STUDENT BY STUDENT ID =============

    @GetMapping("/get-student-by-student-id/{studentId}")
    public ResponseEntity<?> getStudentByStudentId(@PathVariable String studentId) {
        try {
            StudentResponseDto student = studentService.getStudentByStudentId(studentId);
            if (student != null) {
                return ResponseEntity.ok(student);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student not found with Student ID: " + studentId);
        } catch (Exception e) {
            logger.error("Error getting student by student ID {}: {}", studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting student: " + e.getMessage());
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody Map<String, String> credentials) {
        String identifier = credentials.get("identifier");
        if (identifier == null) {
            identifier = credentials.get("studentId");
        }
        String password = credentials.get("password");

        logger.info("Student authentication attempt for: {}", identifier);

        if (identifier == null || identifier.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "authenticated", false,
                    "error", "Student ID or Roll Number is required"
            ));
        }

        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "authenticated", false,
                    "error", "Password is required"
            ));
        }

        try {
            StudentEntity student = studentService.getStudentByIdentifier(identifier);

            if (student == null) {
                logger.warn("Student not found with identifier: {}", identifier);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "authenticated", false,
                        "error", "Invalid Student ID or password"
                ));
            }

            if (!"Active".equals(student.getStatus())) {
                logger.warn("Student account is inactive: {}", identifier);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "authenticated", false,
                        "error", "Student account is inactive. Please contact administrator."
                ));
            }

            boolean authenticated = studentService.authenticateStudent(student.getStudentId(), password);
            logger.info("Password authentication result: {}", authenticated);

            if (authenticated) {
                Map<String, Object> response = new HashMap<>();
                response.put("authenticated", true);
                response.put("studentId", student.getStudentId());
                response.put("rollNumber", student.getStudentRollNumber());
                response.put("name", student.getFirstName() + " " + (student.getLastName() != null ? student.getLastName() : ""));
                response.put("currentClass", student.getCurrentClass());
                response.put("section", student.getSection());
                response.put("role", student.getRole() != null ? student.getRole() : "STUDENT");
                response.put("message", "Login successful");

                logger.info("Student authenticated successfully: {}", student.getStudentId());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Invalid password for student: {}", identifier);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "authenticated", false,
                        "error", "Invalid Student ID or password"
                ));
            }

        } catch (Exception e) {
            logger.error("Error during student authentication", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "authenticated", false,
                    "error", "Authentication failed: " + e.getMessage()
            ));
        }
    }

    // ============= 🔍 GET STUDENTS BY CLASS =============

    @GetMapping("/get-students-by-class/{className}")
    public ResponseEntity<?> getStudentsByClass(@PathVariable String className) {
        try {
            List<StudentResponseDto> students = studentService.getStudentsByClass(className);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            logger.error("Error getting students by class {}: {}", className, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting students: " + e.getMessage());
        }
    }

    // ============= 🔍 GET STUDENTS BY CLASS & SECTION =============

    @GetMapping("/get-students-by-class-section")
    public ResponseEntity<?> getStudentsByClassAndSection(
            @RequestParam String className,
            @RequestParam String section) {
        try {
            List<StudentResponseDto> students = studentService.getStudentsByClassAndSection(className, section);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            logger.error("Error getting students by class {} and section {}: {}", className, section, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting students: " + e.getMessage());
        }
    }

    // ============= 🔍 GET STUDENTS BY STATUS =============

    @GetMapping("/get-students-by-status/{status}")
    public ResponseEntity<?> getStudentsByStatus(@PathVariable String status) {
        try {
            List<StudentResponseDto> students = studentService.getStudentsByStatus(status);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            logger.error("Error getting students by status {}: {}", status, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting students: " + e.getMessage());
        }
    }

    // ============= 🔍 GET STUDENTS BY ADMISSION DATE =============

    @GetMapping("/get-students-by-admission-date")
    public ResponseEntity<?> getStudentsByAdmissionDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date admissionDate) {
        try {
            List<StudentResponseDto> students = studentService.getStudentsByAdmissionDate(admissionDate);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            logger.error("Error getting students by admission date {}: {}", admissionDate, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting students: " + e.getMessage());
        }
    }

    // ============= 🔍 SEARCH STUDENTS =============

    @GetMapping("/search-students")
    public ResponseEntity<?> searchStudents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String fatherName,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String rollNumber) {
        try {
            List<StudentResponseDto> students = studentService.searchStudents(name, fatherName, studentId, rollNumber);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            logger.error("Error searching students: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching students: " + e.getMessage());
        }
    }

    // ============= 📊 GET STUDENT COUNT BY CLASS =============

    @GetMapping("/get-student-count-by-class")
    public ResponseEntity<?> getStudentCountByClass() {
        try {
            Map<String, Long> countByClass = studentService.getStudentCountByClass();
            return ResponseEntity.ok(countByClass);
        } catch (Exception e) {
            logger.error("Error getting student count by class: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting count: " + e.getMessage());
        }
    }

    // ============= 📊 GET STUDENT COUNT BY CLASS & SECTION =============

    @GetMapping("/get-student-count-by-class-section")
    public ResponseEntity<?> getStudentCountByClassAndSection() {
        try {
            Map<String, Map<String, Long>> countByClassSection = studentService.getStudentCountByClassAndSection();
            return ResponseEntity.ok(countByClassSection);
        } catch (Exception e) {
            logger.error("Error getting student count by class and section: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting count: " + e.getMessage());
        }
    }

    // ============= 📊 GET STUDENT STATISTICS =============

    @GetMapping("/get-student-statistics")
    public ResponseEntity<?> getStudentStatistics() {
        try {
            Map<String, Object> statistics = studentService.getStudentStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting student statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting statistics: " + e.getMessage());
        }
    }

    // ============= ✏️ UPDATE STUDENT (FULL) =============

    @PutMapping("/update-student/{id}")
    public ResponseEntity<?> updateStudent(
            @PathVariable Long id,
            @RequestBody StudentRequestDto requestDto) {
        try {
            StudentResponseDto response = studentService.updateStudent(id, requestDto);
            if (response != null) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating student {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating student: " + e.getMessage());
        }
    }

    // ============= ✏️ UPDATE STUDENT WITH FILES =============

    @PatchMapping("/update-student-with-files/{id}")
    public ResponseEntity<?> updateStudentWithFiles(
            @PathVariable Long id,
            @RequestPart("studentData") String studentDataJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "studentAadharImage", required = false) MultipartFile studentAadharImage,
            @RequestPart(value = "fatherAadharImage", required = false) MultipartFile fatherAadharImage,
            @RequestPart(value = "motherAadharImage", required = false) MultipartFile motherAadharImage,
            @RequestPart(value = "birthCertificateImage", required = false) MultipartFile birthCertificateImage,
            @RequestPart(value = "transferCertificateImage", required = false) MultipartFile transferCertificateImage,
            @RequestPart(value = "markSheetImage", required = false) MultipartFile markSheetImage,
            @RequestPart(value = "incomeCertificateImage", required = false) MultipartFile incomeCertificateImage,
            @RequestPart(value = "casteCertificateImage", required = false) MultipartFile casteCertificateImage) {

        try {
            objectMapper.registerModule(new JavaTimeModule());
            StudentRequestDto requestDto = objectMapper.readValue(studentDataJson, StudentRequestDto.class);

            if (profileImage != null && !profileImage.isEmpty()) {
                requestDto.setProfileImage(profileImage);
            }
            if (studentAadharImage != null && !studentAadharImage.isEmpty()) {
                requestDto.setStudentAadharImage(studentAadharImage);
            }
            if (fatherAadharImage != null && !fatherAadharImage.isEmpty()) {
                requestDto.setFatherAadharImage(fatherAadharImage);
            }
            if (motherAadharImage != null && !motherAadharImage.isEmpty()) {
                requestDto.setMotherAadharImage(motherAadharImage);
            }
            if (birthCertificateImage != null && !birthCertificateImage.isEmpty()) {
                requestDto.setBirthCertificateImage(birthCertificateImage);
            }
            if (transferCertificateImage != null && !transferCertificateImage.isEmpty()) {
                requestDto.setTransferCertificateImage(transferCertificateImage);
            }
            if (markSheetImage != null && !markSheetImage.isEmpty()) {
                requestDto.setMarkSheetImage(markSheetImage);
            }
            if (incomeCertificateImage != null && !incomeCertificateImage.isEmpty()) {
                requestDto.setIncomeCertificateImage(incomeCertificateImage);
            }
            if (casteCertificateImage != null && !casteCertificateImage.isEmpty()) {
                requestDto.setCasteCertificateImage(casteCertificateImage);
            }

            StudentResponseDto response = studentService.updateStudent(id, requestDto);
            if (response != null) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student not found with ID: " + id);

        } catch (Exception e) {
            logger.error("Error updating student with files: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating student: " + e.getMessage());
        }
    }

    // ============= ✏️ UPDATE STUDENT (PARTIAL) =============

    @PatchMapping("/update-student-partial/{id}")
    public ResponseEntity<?> updateStudentPartial(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        try {
            StudentResponseDto response = studentService.updateStudentPartial(id, updates);
            if (response != null) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error partially updating student {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating student: " + e.getMessage());
        }
    }

    // ============= ✏️ UPDATE STUDENT STATUS =============

    @PatchMapping("/update-student-status/{id}")
    public ResponseEntity<?> updateStudentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            StudentResponseDto response = studentService.updateStudentStatus(id, status);
            if (response != null) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating student status {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating status: " + e.getMessage());
        }
    }

    // ============= ✏️ UPDATE STUDENT CLASS-SECTION =============

    @PatchMapping("/update-student-class-section/{id}")
    public ResponseEntity<?> updateStudentClassSection(
            @PathVariable Long id,
            @RequestParam String currentClass,
            @RequestParam String section) {
        try {
            StudentResponseDto response = studentService.updateStudentClassSection(id, currentClass, section);
            if (response != null) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating student class-section {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating class-section: " + e.getMessage());
        }
    }

    // ============= ✏️ BULK UPDATE STUDENT STATUS =============

    @PatchMapping("/bulk-update-student-status")
    public ResponseEntity<?> bulkUpdateStudentStatus(
            @RequestBody List<Long> studentIds,
            @RequestParam String status) {
        try {
            List<StudentResponseDto> updatedStudents = studentService.bulkUpdateStudentStatus(studentIds, status);
            return ResponseEntity.ok(updatedStudents);
        } catch (Exception e) {
            logger.error("Error bulk updating student status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error bulk updating status: " + e.getMessage());
        }
    }

    // ============= 🗑️ DELETE STUDENT =============

    @DeleteMapping("/delete-student/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok("Student deleted successfully with ID: " + id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting student: " + e.getMessage());
        }
    }

    // ============= 🖼️ IMAGE SERVING ENDPOINTS =============

    @GetMapping(value = "/{id}/profile-image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long id) {
        try {
            byte[] image = studentService.getProfileImage(id);
            if (image != null && image.length > 0) {
                return ResponseEntity.ok()
                        .contentType(detectImageType(image))
                        .header("Content-Disposition", "inline; filename=\"profile_" + id + ".jpg\"")
                        .body(image);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching profile image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/{id}/aadhar-image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_PDF_VALUE})
    public ResponseEntity<byte[]> getStudentAadharImage(@PathVariable Long id) {
        try {
            byte[] image = studentService.getStudentAadharImage(id);
            if (image != null && image.length > 0) {
                return ResponseEntity.ok()
                        .contentType(detectContentType(image))
                        .header("Content-Disposition", "inline; filename=\"aadhar_" + id + ".jpg\"")
                        .body(image);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching student aadhar image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/{id}/father-aadhar-image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_PDF_VALUE})
    public ResponseEntity<byte[]> getFatherAadharImage(@PathVariable Long id) {
        try {
            byte[] image = studentService.getFatherAadharImage(id);
            if (image != null && image.length > 0) {
                return ResponseEntity.ok()
                        .contentType(detectContentType(image))
                        .header("Content-Disposition", "inline; filename=\"father_aadhar_" + id + ".jpg\"")
                        .body(image);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching father aadhar image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/{id}/mother-aadhar-image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_PDF_VALUE})
    public ResponseEntity<byte[]> getMotherAadharImage(@PathVariable Long id) {
        try {
            byte[] image = studentService.getMotherAadharImage(id);
            if (image != null && image.length > 0) {
                return ResponseEntity.ok()
                        .contentType(detectContentType(image))
                        .header("Content-Disposition", "inline; filename=\"mother_aadhar_" + id + ".jpg\"")
                        .body(image);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching mother aadhar image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/{id}/birth-certificate", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_PDF_VALUE})
    public ResponseEntity<byte[]> getBirthCertificateImage(@PathVariable Long id) {
        try {
            byte[] image = studentService.getBirthCertificateImage(id);
            if (image != null && image.length > 0) {
                return ResponseEntity.ok()
                        .contentType(detectContentType(image))
                        .header("Content-Disposition", "inline; filename=\"birth_certificate_" + id + ".pdf\"")
                        .body(image);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching birth certificate: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/{id}/transfer-certificate", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_PDF_VALUE})
    public ResponseEntity<byte[]> getTransferCertificateImage(@PathVariable Long id) {
        try {
            byte[] image = studentService.getTransferCertificateImage(id);
            if (image != null && image.length > 0) {
                return ResponseEntity.ok()
                        .contentType(detectContentType(image))
                        .header("Content-Disposition", "inline; filename=\"tc_" + id + ".pdf\"")
                        .body(image);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching transfer certificate: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/{id}/marksheet", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_PDF_VALUE})
    public ResponseEntity<byte[]> getMarkSheetImage(@PathVariable Long id) {
        try {
            byte[] image = studentService.getMarkSheetImage(id);
            if (image != null && image.length > 0) {
                return ResponseEntity.ok()
                        .contentType(detectContentType(image))
                        .header("Content-Disposition", "inline; filename=\"marksheet_" + id + ".pdf\"")
                        .body(image);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching marksheet: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ============= 🖼️ INCOME AND CASTE CERTIFICATE ENDPOINTS =============

    @GetMapping(value = "/{id}/income-certificate", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_PDF_VALUE})
    public ResponseEntity<byte[]> getIncomeCertificateImage(@PathVariable Long id) {
        try {
            byte[] image = studentService.getIncomeCertificateImage(id);
            if (image != null && image.length > 0) {
                return ResponseEntity.ok()
                        .contentType(detectContentType(image))
                        .header("Content-Disposition", "inline; filename=\"income_certificate_" + id + ".pdf\"")
                        .body(image);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching income certificate: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/{id}/caste-certificate", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_PDF_VALUE})
    public ResponseEntity<byte[]> getCasteCertificateImage(@PathVariable Long id) {
        try {
            byte[] image = studentService.getCasteCertificateImage(id);
            if (image != null && image.length > 0) {
                return ResponseEntity.ok()
                        .contentType(detectContentType(image))
                        .header("Content-Disposition", "inline; filename=\"caste_certificate_" + id + ".pdf\"")
                        .body(image);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching caste certificate: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ============= 🖼️ IMAGE UPLOAD ENDPOINTS =============

    @PostMapping(value = "/{id}/upload-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfileImage(
            @PathVariable Long id,
            @RequestPart("profileImage") MultipartFile profileImage) {

        try {
            if (profileImage == null || profileImage.isEmpty()) {
                return ResponseEntity.badRequest().body("Profile image is required");
            }
            StudentResponseDto response = studentService.uploadStudentImage(id, profileImage);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error uploading profile image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading image: " + e.getMessage());
        }
    }

    @PostMapping(value = "/{id}/upload-documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadStudentDocuments(
            @PathVariable Long id,
            @RequestPart(value = "studentAadharImage", required = false) MultipartFile studentAadharImage,
            @RequestPart(value = "fatherAadharImage", required = false) MultipartFile fatherAadharImage,
            @RequestPart(value = "motherAadharImage", required = false) MultipartFile motherAadharImage,
            @RequestPart(value = "birthCertificateImage", required = false) MultipartFile birthCertificateImage,
            @RequestPart(value = "transferCertificateImage", required = false) MultipartFile transferCertificateImage,
            @RequestPart(value = "markSheetImage", required = false) MultipartFile markSheetImage,
            @RequestPart(value = "incomeCertificateImage", required = false) MultipartFile incomeCertificateImage,
            @RequestPart(value = "casteCertificateImage", required = false) MultipartFile casteCertificateImage) {

        try {
            StudentResponseDto response = studentService.uploadStudentDocuments(
                    id, studentAadharImage, fatherAadharImage, motherAadharImage,
                    birthCertificateImage, transferCertificateImage, markSheetImage,
                    incomeCertificateImage, casteCertificateImage);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error uploading documents: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading documents: " + e.getMessage());
        }
    }

    // ============= 👨‍🏫 GET STUDENTS BY TEACHER ID =============

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<?> getStudentsByTeacherId(@PathVariable Long teacherId) {
        logger.info("GET /api/students/teacher/{} - Fetching students for teacher", teacherId);
        try {
            List<StudentResponseDto> students = studentService.getStudentsByTeacherId(teacherId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Students retrieved successfully for teacher");
            response.put("data", students);
            response.put("count", students.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching students for teacher {}: {}", teacherId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch students: " + e.getMessage()));
        }
    }

    // ============= 👨‍🏫 GET STUDENTS BY TEACHER CODE =============

    @GetMapping("/teacher/code/{teacherCode}")
    public ResponseEntity<?> getStudentsByTeacherCode(@PathVariable String teacherCode) {
        logger.info("GET /api/students/teacher/code/{} - Fetching students for teacher", teacherCode);
        try {
            List<StudentResponseDto> students = studentService.getStudentsByTeacherCode(teacherCode);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Students retrieved successfully for teacher");
            response.put("data", students);
            response.put("count", students.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching students for teacher code {}: {}", teacherCode, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch students: " + e.getMessage()));
        }
    }

    // ============= 🔍 GET STUDENTS BY CLASS ID =============

    @GetMapping("/class/{classId}/students")
    public ResponseEntity<?> getStudentsByClassId(@PathVariable Long classId) {
        logger.info("GET /api/students/class/{}/students", classId);
        try {
            List<StudentResponseDto> students = studentService.getStudentsByClassId(classId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Students retrieved successfully for class");
            response.put("data", students);
            response.put("count", students.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching students for class {}: {}", classId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch students: " + e.getMessage()));
        }
    }

    // ============= 🎯 HELPER METHODS =============

    private void validateStudentRequest(StudentRequestDto requestDto) {
        if (requestDto.getFirstName() == null || requestDto.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (requestDto.getLastName() == null || requestDto.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (requestDto.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }
        if (requestDto.getCurrentClass() == null || requestDto.getCurrentClass().trim().isEmpty()) {
            throw new IllegalArgumentException("Class is required");
        }
        if (requestDto.getFatherName() == null || requestDto.getFatherName().trim().isEmpty()) {
            throw new IllegalArgumentException("Father's name is required");
        }
        if (requestDto.getFatherPhone() == null || requestDto.getFatherPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Father's phone is required");
        }
    }

    private MediaType detectImageType(byte[] image) {
        if (image.length > 0) {
            if (image[0] == (byte) 0xFF && image[1] == (byte) 0xD8) {
                return MediaType.IMAGE_JPEG;
            } else if (image[0] == (byte) 0x89 && image[1] == (byte) 0x50 &&
                    image[2] == (byte) 0x4E && image[3] == (byte) 0x47) {
                return MediaType.IMAGE_PNG;
            } else if (image[0] == (byte) 0x47 && image[1] == (byte) 0x49 &&
                    image[2] == (byte) 0x46) {
                return MediaType.IMAGE_GIF;
            }
        }
        return MediaType.IMAGE_JPEG;
    }

    private MediaType detectContentType(byte[] data) {
        if (data.length > 0) {
            if (data[0] == (byte) 0x25 && data[1] == (byte) 0x50 &&
                    data[2] == (byte) 0x44 && data[3] == (byte) 0x46) {
                return MediaType.APPLICATION_PDF;
            } else if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8) {
                return MediaType.IMAGE_JPEG;
            } else if (data[0] == (byte) 0x89 && data[1] == (byte) 0x50 &&
                    data[2] == (byte) 0x4E && data[3] == (byte) 0x47) {
                return MediaType.IMAGE_PNG;
            }
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}