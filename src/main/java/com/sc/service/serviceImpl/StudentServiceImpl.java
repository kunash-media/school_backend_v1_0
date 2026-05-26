package com.sc.service.serviceImpl;

import com.sc.bcrypt.BcryptEncoderConfig;
import com.sc.dto.request.FeesRequestDto;
import com.sc.dto.request.StudentRequestDto;
import com.sc.dto.response.FeesResponseDto;
import com.sc.dto.response.StudentResponseDto;
import com.sc.entity.ClassEntity;
import com.sc.entity.StudentClassEnrollment;
import com.sc.entity.StudentEntity;
import com.sc.repository.ClassRepository;
import com.sc.repository.StudentRepository;
import com.sc.service.ClassService;
import com.sc.service.FeesService;
import com.sc.service.StudentService;
import com.sc.util.StudentIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import com.sc.entity.TeacherEntity;
import com.sc.repository.TeacherRepository;

@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentIdGenerator studentIdGenerator;

    @Autowired
    private FeesService feesService;

    @Autowired
    private ClassRepository classRepository;

    // If you prefer a service layer:
    @Autowired
    private ClassService classService;

    // ✅ ADD THIS - Password Encoder
    @Autowired
    private BcryptEncoderConfig passwordEncoder;



    // In StudentServiceImpl
    @Override
    public StudentEntity getStudentByStudentIdEntity(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }
// ============= ✅ AUTHENTICATION METHODS =============

    @Override
    public boolean authenticateStudent(String studentId, String rawPassword) {
        logger.info("Authenticating student with studentId: {}", studentId);

        StudentEntity student = studentRepository.findByStudentId(studentId);

        if (student == null) {
            logger.warn("Student not found with studentId: {}", studentId);
            return false;
        }

        // Check if student is active
        if (!"Active".equals(student.getStatus())) {
            logger.warn("Student account is inactive: {}", studentId);
            return false;
        }

        String storedHash = student.getStudentPassword();

        if (storedHash == null || rawPassword == null) {
            logger.warn("Null password for student: {}", studentId);
            return false;
        }

        boolean matches = passwordEncoder.matches(rawPassword, storedHash);

        if (matches) {
            logger.info("Student authenticated successfully: {}", studentId);
        } else {
            logger.warn("Invalid password for student: {}", studentId);
        }

        return matches;
    }

    @Override
    public boolean authenticateStudentByRollNumber(String rollNumber, String rawPassword) {
        logger.info("Authenticating student with roll number: {}", rollNumber);

        StudentEntity student = studentRepository.findByStudentRollNumber(rollNumber);

        if (student == null) {
            logger.warn("Student not found with roll number: {}", rollNumber);
            return false;
        }

        // Check if student is active
        if (!"Active".equals(student.getStatus())) {
            logger.warn("Student account is inactive: {}", rollNumber);
            return false;
        }

        String storedHash = student.getStudentPassword();

        if (storedHash == null || rawPassword == null) {
            logger.warn("Null password for student: {}", rollNumber);
            return false;
        }

        boolean matches = passwordEncoder.matches(rawPassword, storedHash);

        if (matches) {
            logger.info("Student authenticated successfully by roll number: {}", rollNumber);
        } else {
            logger.warn("Invalid password for student: {}", rollNumber);
        }

        return matches;
    }

    // ============= 👨‍🏫 GET STUDENTS BY TEACHER ID =============

    @Override
    public List<StudentResponseDto> getStudentsByTeacherId(Long teacherId) {
        logger.info("Fetching students for teacher ID: {}", teacherId);

        try {
            // Get classes where this teacher is assigned as Class Teacher or Assistant Teacher
            List<ClassEntity> teacherClasses = classRepository.findByClassTeacherId(teacherId);

            // Also get classes where teacher is assistant teacher (if your ClassEntity has this field)
            // List<ClassEntity> assistantClasses = classRepository.findByAssistantTeacherId(teacherId);
            // teacherClasses.addAll(assistantClasses);

            if (teacherClasses == null || teacherClasses.isEmpty()) {
                logger.info("No classes found for teacher ID: {}", teacherId);
                return new ArrayList<>();
            }

            // Get all students enrolled in these classes
            Set<StudentEntity> uniqueStudents = new HashSet<>();

            for (ClassEntity classEntity : teacherClasses) {
                List<StudentEntity> classStudents = studentRepository.findByCurrentClassAndSection(
                        classEntity.getClassName(),
                        classEntity.getSection()
                );
                uniqueStudents.addAll(classStudents);
            }

            logger.info("Found {} students for teacher ID: {}", uniqueStudents.size(), teacherId);

            return uniqueStudents.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error fetching students for teacher {}: {}", teacherId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch students for teacher: " + e.getMessage());
        }
    }

// ============= 👨‍🏫 GET STUDENTS BY TEACHER CODE =============

    @Override
    public List<StudentResponseDto> getStudentsByTeacherCode(String teacherCode) {
        logger.info("Fetching students for teacher code: {}", teacherCode);

        try {
            // First find teacher by code
            Optional<TeacherEntity> teacherOpt = teacherRepository.findByTeacherCode(teacherCode);

            if (teacherOpt.isEmpty()) {
                logger.warn("Teacher not found with code: {}", teacherCode);
                return new ArrayList<>();
            }

            TeacherEntity teacher = teacherOpt.get();
            return getStudentsByTeacherId(teacher.getId());

        } catch (Exception e) {
            logger.error("Error fetching students for teacher code {}: {}", teacherCode, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch students for teacher: " + e.getMessage());
        }
    }

// ============= 🔍 GET STUDENTS BY CLASS ID =============

    @Override
    public List<StudentResponseDto> getStudentsByClassId(Long classId) {
        logger.info("Fetching students for class ID: {}", classId);

        try {
            Optional<ClassEntity> classOpt = classRepository.findById(classId);

            if (classOpt.isEmpty()) {
                logger.warn("Class not found with ID: {}", classId);
                return new ArrayList<>();
            }

            ClassEntity classEntity = classOpt.get();

            List<StudentEntity> students = studentRepository.findByCurrentClassAndSection(
                    classEntity.getClassName(),
                    classEntity.getSection()
            );

            logger.info("Found {} students for class ID: {}", students.size(), classId);

            return students.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error fetching students for class {}: {}", classId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch students for class: " + e.getMessage());
        }
    }

    @Override
    public StudentEntity getStudentByIdentifier(String identifier) {
        logger.info("Getting student by identifier: {}", identifier);

        // Try to find by studentId first
        StudentEntity student = studentRepository.findByStudentId(identifier);

        // If not found, try by roll number
        if (student == null) {
            student = studentRepository.findByStudentRollNumber(identifier);
        }

        return student;
    }

    // ============= 📝 CREATE STUDENT =============

    @Override
    @Transactional
    public StudentResponseDto createStudent(StudentRequestDto requestDto) {
        logger.info("=== START CREATING STUDENT ===");
        logger.info("Request DTO: {}", requestDto);

        try {
            // 1. Validate classId (required)
            if (requestDto.getClassId() == null) {
                logger.error("classId is null");
                throw new IllegalArgumentException("classId is required to assign student to a class");
            }
            logger.info("Class ID: {}", requestDto.getClassId());

            // 2. Fetch the class entity
            logger.info("Fetching class with ID: {}", requestDto.getClassId());
            ClassEntity classEntity = classRepository.findById(requestDto.getClassId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Class not found with ID: " + requestDto.getClassId()));
            logger.info("Class found: {} - {}", classEntity.getClassName(), classEntity.getSection());

            // 3. Auto-generate roll number using class + section
            String autoRollNumber = studentIdGenerator.generateUniqueRollNumber(
                    classEntity.getClassName(),
                    classEntity.getSection()
            );
            requestDto.setStudentRollNumber(autoRollNumber);
            logger.info("Generated roll number: {}", autoRollNumber);

            // 4. Convert DTO to Entity
            logger.info("Converting DTO to Entity...");
            StudentEntity student = convertToEntity(requestDto);
            logger.info("Entity created with studentId: {}", student.getStudentId());

            // 5. Create enrollment (link student ↔ class)
            logger.info("Creating enrollment...");
            StudentClassEnrollment enrollment = new StudentClassEnrollment(student, classEntity);
            student.addEnrollment(enrollment);
            logger.info("Enrollment created");

            // 6. Sync denormalized fields
            student.setCurrentClass(classEntity.getClassName());
            student.setSection(classEntity.getSection());
            logger.info("Set class: {}, section: {}", student.getCurrentClass(), student.getSection());

            // 7. Process images
            logger.info("Processing images...");
            processImages(student, requestDto);
            logger.info("Images processed");

            // 8. Save student
            logger.info("Saving student to database...");
            StudentEntity savedStudent = studentRepository.save(student);
            logger.info("Student saved with ID: {} and Roll Number: {}",
                    savedStudent.getStdId(), savedStudent.getStudentRollNumber());

            // 9. Create fees if provided
            if (hasFeesData(requestDto)) {
                logger.info("Creating fees for student...");
                createFeesForStudent(savedStudent.getStdId(), requestDto);
                logger.info("Fees created");
            }

            logger.info("=== STUDENT CREATED SUCCESSFULLY ===");
            return convertToDto(savedStudent);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Error creating student: {}", e.getMessage(), e);
            e.printStackTrace(); // This will print full stack trace to console
            throw new RuntimeException("Failed to create student: " + e.getMessage(), e);
        }
    }


    // ============= 🔍 GET ALL STUDENTS =============

//    @Override
//    public List<StudentResponseDto> getAllStudents() {
//        try {
//            return studentRepository.findAll().stream()
//                    .map(this::convertToDto)
//                    .collect(Collectors.toList());
//        } catch (Exception e) {
//            logger.error("Error getting all students: {}", e.getMessage());
//            throw new RuntimeException("Failed to get students: " + e.getMessage());
//        }
//    }


    @Override
    public Page<StudentResponseDto> getAllStudents(Pageable pageable) {
        try {
            return studentRepository.findAll(pageable)
                    .map(this::convertToDto);   // map entity → DTO (keeps pagination metadata)
        } catch (Exception e) {
            logger.error("Error getting paginated students: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch students: " + e.getMessage(), e);
        }
    }

    // ============= 🔍 GET STUDENT BY ID =============

    @Override
    public StudentResponseDto getStudentById(Long id) {
        try {
            return studentRepository.findById(id)
                    .map(this::convertToDto)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting student by ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get student: " + e.getMessage());
        }
    }

    // ============= 🔍 GET STUDENT BY ROLL NUMBER =============

    @Override
    public StudentResponseDto getStudentByRollNumber(String rollNumber) {
        try {
            StudentEntity student = studentRepository.findByStudentRollNumber(rollNumber);
            return student != null ? convertToDto(student) : null;
        } catch (Exception e) {
            logger.error("Error getting student by roll number {}: {}", rollNumber, e.getMessage());
            throw new RuntimeException("Failed to get student: " + e.getMessage());
        }
    }

    // ============= 🔍 GET STUDENT BY STUDENT ID =============

    @Override
    public StudentResponseDto getStudentByStudentId(String studentId) {
        try {
            StudentEntity student = studentRepository.findByStudentId(studentId);
            return student != null ? convertToDto(student) : null;
        } catch (Exception e) {
            logger.error("Error getting student by student ID {}: {}", studentId, e.getMessage());
            throw new RuntimeException("Failed to get student: " + e.getMessage());
        }
    }

    // ============= 🔍 GET STUDENTS BY CLASS =============

    @Override
    public List<StudentResponseDto> getStudentsByClass(String className) {
        try {
            return studentRepository.findByCurrentClass(className).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting students by class {}: {}", className, e.getMessage());
            throw new RuntimeException("Failed to get students: " + e.getMessage());
        }
    }

    // ============= 🔍 GET STUDENTS BY CLASS & SECTION =============

    @Override
    public List<StudentResponseDto> getStudentsByClassAndSection(String className, String section) {
        try {
            return studentRepository.findByCurrentClassAndSection(className, section).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting students by class {} and section {}: {}", className, section, e.getMessage());
            throw new RuntimeException("Failed to get students: " + e.getMessage());
        }
    }

    // ============= 🔍 GET STUDENTS BY STATUS =============

    @Override
    public List<StudentResponseDto> getStudentsByStatus(String status) {
        try {
            return studentRepository.findByStatus(status).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting students by status {}: {}", status, e.getMessage());
            throw new RuntimeException("Failed to get students: " + e.getMessage());
        }
    }

    // ============= 🔍 GET STUDENTS BY ADMISSION DATE =============

    @Override
    public List<StudentResponseDto> getStudentsByAdmissionDate(Date admissionDate) {
        try {
            return studentRepository.findByAdmissionDate(admissionDate).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting students by admission date {}: {}", admissionDate, e.getMessage());
            throw new RuntimeException("Failed to get students: " + e.getMessage());
        }
    }

    // ============= 🔍 SEARCH STUDENTS =============

    @Override
    public List<StudentResponseDto> searchStudents(String name, String fatherName, String studentId, String rollNumber) {
        try {
            return studentRepository.searchStudents(name, fatherName, studentId, rollNumber).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error searching students: {}", e.getMessage());
            throw new RuntimeException("Failed to search students: " + e.getMessage());
        }
    }

    // ============= 📊 GET STUDENT COUNT BY CLASS =============

    @Override
    public Map<String, Long> getStudentCountByClass() {
        try {
            List<Object[]> results = studentRepository.getStudentCountByClass();
            Map<String, Long> countMap = new HashMap<>();
            for (Object[] result : results) {
                String className = (String) result[0];
                Long count = (Long) result[1];
                countMap.put(className, count);
            }
            return countMap;
        } catch (Exception e) {
            logger.error("Error getting student count by class: {}", e.getMessage());
            throw new RuntimeException("Failed to get student count: " + e.getMessage());
        }
    }

    // ============= 📊 GET STUDENT COUNT BY CLASS & SECTION =============

    @Override
    public Map<String, Map<String, Long>> getStudentCountByClassAndSection() {
        try {
            List<Object[]> results = studentRepository.getStudentCountByClassAndSection();
            Map<String, Map<String, Long>> classSectionMap = new HashMap<>();

            for (Object[] result : results) {
                String className = (String) result[0];
                String section = (String) result[1];
                Long count = (Long) result[2];

                classSectionMap.computeIfAbsent(className, k -> new HashMap<>());
                classSectionMap.get(className).put(section, count);
            }
            return classSectionMap;
        } catch (Exception e) {
            logger.error("Error getting student count by class and section: {}", e.getMessage());
            throw new RuntimeException("Failed to get student count: " + e.getMessage());
        }
    }

    // ============= 📊 GET STUDENT STATISTICS =============

    @Override
    public Map<String, Object> getStudentStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // Total students
            long totalStudents = studentRepository.count();
            statistics.put("totalStudents", totalStudents);

            // Active students
            long activeStudents = studentRepository.countByStatus("Active");
            statistics.put("activeStudents", activeStudents);

            // Inactive students
            long inactiveStudents = studentRepository.countByStatus("Inactive");
            statistics.put("inactiveStudents", inactiveStudents);

            // Students by class
            Map<String, Long> byClass = getStudentCountByClass();
            statistics.put("studentsByClass", byClass);

            // Students by class and section
            Map<String, Map<String, Long>> byClassSection = getStudentCountByClassAndSection();
            statistics.put("studentsByClassSection", byClassSection);

            // Gender distribution
            long maleStudents = studentRepository.countByGender("Male");
            long femaleStudents = studentRepository.countByGender("Female");
            statistics.put("maleStudents", maleStudents);
            statistics.put("femaleStudents", femaleStudents);

            // Total fees collection (if needed)
            // This would come from FeesService

            return statistics;
        } catch (Exception e) {
            logger.error("Error getting student statistics: {}", e.getMessage());
            throw new RuntimeException("Failed to get statistics: " + e.getMessage());
        }
    }

    // ============= ✏️ UPDATE STUDENT (FULL) =============

//    @Override
//    @Transactional
//    public StudentResponseDto updateStudent(Long id, StudentRequestDto requestDto) {
//        try {
//            return studentRepository.findById(id)
//                    .map(student -> {
//                        updateStudentFields(student, requestDto);
//                        try {
//                            processImages(student, requestDto);
//                        } catch (IOException e) {
//                            logger.error("Error processing images: {}", e.getMessage());
//                        }
//                        StudentEntity updated = studentRepository.save(student);
//
//                        if (hasFeesData(requestDto)) {
//                            updateOrCreateFees(id, requestDto);
//                        }
//
//                        return convertToDto(updated);
//                    })
//                    .orElse(null);
//        } catch (Exception e) {
//            logger.error("Error updating student {}: {}", id, e.getMessage());
//            throw new RuntimeException("Failed to update student: " + e.getMessage());
//        }
//    }


    @Override
    @Transactional
    public StudentResponseDto updateStudent(Long id, StudentRequestDto requestDto) {
        logger.info("Updating student with ID: {}", id);

        try {
            return studentRepository.findById(id)
                    .map(student -> {
                        // 1. Update core fields
                        updateStudentFields(student, requestDto);

                        // 2. Handle class change if classId provided
                        if (requestDto.getClassId() != null) {
                            // Fetch new class
                            ClassEntity newClassEntity = classRepository.findById(requestDto.getClassId())
                                    .orElseThrow(() -> new IllegalArgumentException("Class not found with ID: " + requestDto.getClassId()));

                            // Deactivate existing active enrollments (one active per student)
                            student.getClassEnrollments().stream()
                                    .filter(enrollment -> "ACTIVE".equals(enrollment.getStatus()))
                                    .forEach(enrollment -> enrollment.setStatus("INACTIVE"));

                            // Create new enrollment
                            StudentClassEnrollment newEnrollment = new StudentClassEnrollment(student, newClassEntity);
                            student.addEnrollment(newEnrollment);

                            // Sync denormalized fields
                            student.setCurrentClass(newClassEntity.getClassName());
                            student.setSection(newClassEntity.getSection());
                            student.setAcademicYear(newClassEntity.getAcademicYear());
                        }

                        // 3. Process images if updated
                        try {
                            processImages(student, requestDto);
                        } catch (IOException e) {
                            logger.error("Error processing images during update: {}", e.getMessage(), e);
                        }

                        // 4. Save updated student
                        StudentEntity updated = studentRepository.save(student);

                        // 5. Handle fees if provided
                        if (hasFeesData(requestDto)) {
                            updateOrCreateFees(id, requestDto);
                        }

                        // 6. Return DTO
                        return convertToDto(updated);
                    })
                    .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + id));
        } catch (Exception e) {
            logger.error("Error updating student {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update student: " + e.getMessage());
        }
    }

    // ============= ✏️ UPDATE STUDENT (PARTIAL) =============

    @Override
    @Transactional
    public StudentResponseDto updateStudentPartial(Long id, Map<String, Object> updates) {
        try {
            Optional<StudentEntity> optional = studentRepository.findById(id);
            if (optional.isPresent()) {
                StudentEntity student = optional.get();

                // Apply partial updates
                updates.forEach((key, value) -> {
                    switch (key) {
                        case "firstName":
                            student.setFirstName((String) value);
                            break;
                        case "middleName":
                            student.setMiddleName((String) value);
                            break;
                        case "lastName":
                            student.setLastName((String) value);
                            break;
                        case "studentPassword":
                            String plainPassword = (String) value;
                            if (plainPassword != null && !plainPassword.isEmpty()) {
                                String encryptedPassword = passwordEncoder.encode(plainPassword);
                                student.setStudentPassword(encryptedPassword);
                            }
                            break;
                        case "gender":
                            student.setGender((String) value);
                            break;
                        case "bloodGroup":
                            student.setBloodGroup((String) value);
                            break;
                        case "aadharNumber":
                            student.setAadharNumber((String) value);
                            break;
                        case "medicalInfo":
                            student.setMedicalInfo((String) value);
                            break;
                        case "localAddress":
                            student.setLocalAddress((String) value);
                            break;
                        case "localCity":
                            student.setLocalCity((String) value);
                            break;
                        case "localState":
                            student.setLocalState((String) value);
                            break;
                        case "localPincode":
                            student.setLocalPincode((String) value);
                            break;
                        case "fatherName":
                            student.setFatherName((String) value);
                            break;
                        case "fatherPhone":
                            student.setFatherPhone((String) value);
                            break;
                        case "fatherEmail":
                            student.setFatherEmail((String) value);
                            break;
                        case "motherName":
                            student.setMotherName((String) value);
                            break;
                        case "motherPhone":
                            student.setMotherPhone((String) value);
                            break;
                        case "currentClass":
                            student.setCurrentClass((String) value);
                            break;
                        case "section":
                            student.setSection((String) value);
                            break;
                        case "academicYear":
                            student.setAcademicYear((String) value);
                            break;
                        case "status":
                            student.setStatus((String) value);
                            break;
                        default:
                            logger.debug("Unknown field: {}", key);
                    }
                });

                StudentEntity updated = studentRepository.save(student);
                return convertToDto(updated);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error partially updating student {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update student: " + e.getMessage());
        }
    }

    // ============= ✏️ UPDATE STUDENT STATUS =============

    @Override
    @Transactional
    public StudentResponseDto updateStudentStatus(Long id, String status) {
        try {
            Optional<StudentEntity> optional = studentRepository.findById(id);
            if (optional.isPresent()) {
                StudentEntity student = optional.get();
                student.setStatus(status);
                StudentEntity updated = studentRepository.save(student);
                return convertToDto(updated);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error updating student status {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update status: " + e.getMessage());
        }
    }

    // ============= ✏️ UPDATE STUDENT CLASS-SECTION =============

    @Override
    @Transactional
    public StudentResponseDto updateStudentClassSection(Long id, String currentClass, String section) {
        try {
            Optional<StudentEntity> optional = studentRepository.findById(id);
            if (optional.isPresent()) {
                StudentEntity student = optional.get();
                student.setCurrentClass(currentClass);
                student.setSection(section);
                StudentEntity updated = studentRepository.save(student);
                return convertToDto(updated);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error updating student class-section {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update class-section: " + e.getMessage());
        }
    }

    // ============= ✏️ BULK UPDATE STUDENT STATUS =============

    @Override
    @Transactional
    public List<StudentResponseDto> bulkUpdateStudentStatus(List<Long> studentIds, String status) {
        try {
            List<StudentEntity> students = studentRepository.findAllById(studentIds);
            students.forEach(student -> student.setStatus(status));
            List<StudentEntity> updatedStudents = studentRepository.saveAll(students);

            return updatedStudents.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error bulk updating student status: {}", e.getMessage());
            throw new RuntimeException("Failed to bulk update status: " + e.getMessage());
        }
    }

    // ============= 🗑️ DELETE STUDENT =============

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        try {
            if (!studentRepository.existsById(id)) {
                throw new IllegalArgumentException("Student not found with ID: " + id);
            }
            studentRepository.deleteById(id);
            logger.info("Student deleted successfully with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting student {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete student: " + e.getMessage());
        }
    }

    // ============= 🖼️ IMAGE UPLOAD METHODS =============

    @Override
    @Transactional
    public StudentResponseDto uploadStudentImage(Long id, MultipartFile profileImage) {
        try {
            return studentRepository.findById(id)
                    .map(student -> {
                        try {
                            if (profileImage != null && !profileImage.isEmpty()) {
                                student.setProfileImage(profileImage.getBytes());
                                return convertToDto(studentRepository.save(student));
                            }
                            return convertToDto(student);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to upload image");
                        }
                    })
                    .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + id));
        } catch (Exception e) {
            logger.error("Error uploading profile image for student {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public StudentResponseDto uploadStudentDocuments(Long id, MultipartFile studentAadharImage,
                                                     MultipartFile fatherAadharImage,
                                                     MultipartFile motherAadharImage,
                                                     MultipartFile birthCertificateImage,
                                                     MultipartFile transferCertificateImage,
                                                     MultipartFile markSheetImage,
                                                     MultipartFile incomeCertificateImage,
                                                     MultipartFile casteCertificateImage) {
        try {
            return studentRepository.findById(id)
                    .map(student -> {
                        try {
                            if (studentAadharImage != null && !studentAadharImage.isEmpty()) {
                                student.setStudentAadharImage(studentAadharImage.getBytes());
                            }
                            if (fatherAadharImage != null && !fatherAadharImage.isEmpty()) {
                                student.setFatherAadharImage(fatherAadharImage.getBytes());
                            }
                            if (motherAadharImage != null && !motherAadharImage.isEmpty()) {
                                student.setMotherAadharImage(motherAadharImage.getBytes());
                            }
                            if (birthCertificateImage != null && !birthCertificateImage.isEmpty()) {
                                student.setBirthCertificateImage(birthCertificateImage.getBytes());
                            }
                            if (transferCertificateImage != null && !transferCertificateImage.isEmpty()) {
                                student.setTransferCertificateImage(transferCertificateImage.getBytes());
                            }
                            if (markSheetImage != null && !markSheetImage.isEmpty()) {
                                student.setMarkSheetImage(markSheetImage.getBytes());
                            }
                            if (incomeCertificateImage != null && !incomeCertificateImage.isEmpty()) {
                                student.setIncomeCertificateImage(incomeCertificateImage.getBytes());
                            }
                            if (casteCertificateImage != null && !casteCertificateImage.isEmpty()) {
                                student.setCasteCertificateImage(casteCertificateImage.getBytes());
                            }
                            return convertToDto(studentRepository.save(student));
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to upload documents");
                        }
                    })
                    .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + id));
        } catch (Exception e) {
            logger.error("Error uploading documents for student {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to upload documents: " + e.getMessage());
        }
    }

    // ============= 🖼️ IMAGE RETRIEVAL METHODS =============

    @Override
    public byte[] getProfileImage(Long id) {
        try {
            return studentRepository.findById(id)
                    .map(StudentEntity::getProfileImage)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting profile image for student {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get profile image: " + e.getMessage());
        }
    }

    @Override
    public byte[] getIncomeCertificateImage(Long id) {
        try {
            return studentRepository.findById(id)
                    .map(StudentEntity::getIncomeCertificateImage)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting income certificate for student {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get income certificate: " + e.getMessage());
        }
    }

    @Override
    public byte[] getCasteCertificateImage(Long id) {
        try {
            return studentRepository.findById(id)
                    .map(StudentEntity::getCasteCertificateImage)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting caste certificate for student {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get caste certificate: " + e.getMessage());
        }
    }

    @Override
    public byte[] getStudentAadharImage(Long id) {
        try {
            return studentRepository.findById(id)
                    .map(StudentEntity::getStudentAadharImage)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting aadhar image for student {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get aadhar image: " + e.getMessage());
        }
    }

    @Override
    public byte[] getFatherAadharImage(Long id) {
        try {
            return studentRepository.findById(id)
                    .map(StudentEntity::getFatherAadharImage)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting father aadhar image for student {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get father aadhar image: " + e.getMessage());
        }
    }

    @Override
    public byte[] getMotherAadharImage(Long id) {
        try {
            return studentRepository.findById(id)
                    .map(StudentEntity::getMotherAadharImage)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting mother aadhar image for student {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get mother aadhar image: " + e.getMessage());
        }
    }

    @Override
    public byte[] getBirthCertificateImage(Long id) {
        try {
            return studentRepository.findById(id)
                    .map(StudentEntity::getBirthCertificateImage)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting birth certificate for student {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get birth certificate: " + e.getMessage());
        }
    }

    @Override
    public byte[] getTransferCertificateImage(Long id) {
        try {
            return studentRepository.findById(id)
                    .map(StudentEntity::getTransferCertificateImage)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting transfer certificate for student {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get transfer certificate: " + e.getMessage());
        }
    }

    @Override
    public byte[] getMarkSheetImage(Long id) {
        try {
            return studentRepository.findById(id)
                    .map(StudentEntity::getMarkSheetImage)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting marksheet for student {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get marksheet: " + e.getMessage());
        }
    }

    // ============= 💰 FEES METHODS =============

    @Override
    @Transactional
    public StudentResponseDto updateStudentFees(Long id, StudentRequestDto requestDto) {
        try {
            if (!studentRepository.existsById(id)) {
                throw new IllegalArgumentException("Student not found with ID: " + id);
            }
            updateOrCreateFees(id, requestDto);
            return getStudentById(id);
        } catch (Exception e) {
            logger.error("Error updating student fees {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update fees: " + e.getMessage());
        }
    }

    // ============= 🎯 PRIVATE HELPER METHODS =============

    private boolean hasFeesData(StudentRequestDto dto) {
        return dto.getAdmissionFees() != null ||
                dto.getUniformFees() != null ||
                dto.getBookFees() != null ||
                dto.getTuitionFees() != null ||
                dto.getInitialAmount() != null ||
                (dto.getAdditionalFeesList() != null && !dto.getAdditionalFeesList().isEmpty()) ||
                (dto.getInstallmentsList() != null && !dto.getInstallmentsList().isEmpty());
    }

    private void createFeesForStudent(Long studentId, StudentRequestDto studentDto) {
        try {
            FeesRequestDto feesDto = new FeesRequestDto();
            feesDto.setStudentId(studentId);
            feesDto.setAdmissionFees(studentDto.getAdmissionFees());
            feesDto.setUniformFees(studentDto.getUniformFees());
            feesDto.setBookFees(studentDto.getBookFees());
            feesDto.setTuitionFees(studentDto.getTuitionFees());
            feesDto.setAcademicYear(studentDto.getAcademicYear());

            if (studentDto.getAdditionalFeesList() != null) {
                feesDto.setAdditionalFeesList(studentDto.getAdditionalFeesList());
            } else {
                feesDto.setAdditionalFeesList(new HashMap<>());
            }

            feesDto.setInitialAmount(studentDto.getInitialAmount());
            feesDto.setPaymentMode(studentDto.getPaymentMode());

            if (studentDto.getInstallmentsList() != null) {
                feesDto.setInstallmentsList(studentDto.getInstallmentsList());
            } else {
                feesDto.setInstallmentsList(new ArrayList<>());
            }

            feesDto.setCashierName(studentDto.getCashierName());
            feesDto.setTransactionId(studentDto.getTransactionId());

            feesService.createFees(feesDto);
            logger.info("Fees created for student ID: {}", studentId);

        } catch (Exception e) {
            logger.error("Failed to create fees for student {}: {}", studentId, e.getMessage(), e);
        }
    }

    private void updateOrCreateFees(Long studentId, StudentRequestDto studentDto) {
        try {
            FeesResponseDto existingFees = feesService.getFeesByStudentId(studentId);
            if (existingFees != null) {
                FeesRequestDto feesDto = convertToFeesRequestDto(studentId, studentDto);
                feesDto.setAcademicYear(studentDto.getAcademicYear());
                feesService.updateFees(existingFees.getId(), feesDto);
            } else {
                createFeesForStudent(studentId, studentDto);
            }
        } catch (Exception e) {
            logger.error("Error updating fees for student {}: {}", studentId, e.getMessage(), e);
        }
    }

    private FeesRequestDto convertToFeesRequestDto(Long studentId, StudentRequestDto studentDto) {

        FeesRequestDto feesDto = new FeesRequestDto();

        feesDto.setStudentId(studentId);
        feesDto.setAdmissionFees(studentDto.getAdmissionFees());
        feesDto.setUniformFees(studentDto.getUniformFees());
        feesDto.setBookFees(studentDto.getBookFees());
        feesDto.setTuitionFees(studentDto.getTuitionFees());
        feesDto.setAcademicYear(studentDto.getAcademicYear());

        if (studentDto.getAdditionalFeesList() != null) {
            feesDto.setAdditionalFeesList(studentDto.getAdditionalFeesList());
        } else {
            feesDto.setAdditionalFeesList(new HashMap<>());
        }

        feesDto.setInitialAmount(studentDto.getInitialAmount());
        feesDto.setPaymentMode(studentDto.getPaymentMode());

        if (studentDto.getInstallmentsList() != null) {
            feesDto.setInstallmentsList(studentDto.getInstallmentsList());
        } else {
            feesDto.setInstallmentsList(new ArrayList<>());
        }

        feesDto.setCashierName(studentDto.getCashierName());
        feesDto.setTransactionId(studentDto.getTransactionId());
        return feesDto;
    }

    private StudentEntity convertToEntity(StudentRequestDto dto) {
        String studentId = studentIdGenerator.generateUniqueStudentId();

        StudentEntity entity = new StudentEntity();

        entity.setStudentId(studentId);
        entity.setStudentRollNumber(dto.getStudentRollNumber());
        entity.setFirstName(dto.getFirstName());
        entity.setMiddleName(dto.getMiddleName());
        entity.setLastName(dto.getLastName());
        // ✅ CRITICAL: Encrypt and set password
        if (dto.getStudentPassword() != null && !dto.getStudentPassword().isEmpty()) {
            String encryptedPassword = passwordEncoder.encode(dto.getStudentPassword());
            entity.setStudentPassword(encryptedPassword);
            logger.info("Password encrypted for student: {}", studentId);
            logger.debug("Original password: {}", dto.getStudentPassword());
            logger.debug("Encrypted password: {}", encryptedPassword);
        } else {
            logger.error("Password is null or empty for student: {}", studentId);
            throw new IllegalArgumentException("Student password is required");
        }

        // ✅ ADD THIS - SET ROLE
        entity.setRole("STUDENT");

        // ✅ ADD THIS - SET DEFAULT STATUS
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "Active");

        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setGender(dto.getGender());
        entity.setBloodGroup(dto.getBloodGroup());
        entity.setAadharNumber(dto.getAadharNumber());
        entity.setCasteCategory(dto.getCasteCategory());
        entity.setMedicalInfo(dto.getMedicalInfo());

        if (dto.getSportsActivity() != null) {
            entity.setSportsActivity(dto.getSportsActivity());
        } else {
            entity.setSportsActivity(new ArrayList<>());
        }

        entity.setLocalAddress(dto.getLocalAddress());
        entity.setLocalCity(dto.getLocalCity());
        entity.setLocalState(dto.getLocalState());
        entity.setLocalPincode(dto.getLocalPincode());
        entity.setPermanentAddress(dto.getPermanentAddress());
        entity.setPermanentCity(dto.getPermanentCity());
        entity.setPermanentState(dto.getPermanentState());
        entity.setPermanentPincode(dto.getPermanentPincode());

        entity.setFatherName(dto.getFatherName());
        entity.setFatherOccupation(dto.getFatherOccupation());
        entity.setFatherPhone(dto.getFatherPhone());
        entity.setFatherEmail(dto.getFatherEmail());
        entity.setMotherName(dto.getMotherName());
        entity.setMotherOccupation(dto.getMotherOccupation());
        entity.setMotherPhone(dto.getMotherPhone());
        entity.setMotherEmail(dto.getMotherEmail());
        entity.setGuardianName(dto.getGuardianName());
        entity.setGuardianRelation(dto.getGuardianRelation());
        entity.setGuardianPhone(dto.getGuardianPhone());
        entity.setGuardianEmail(dto.getGuardianEmail());
        entity.setEmergencyContact(dto.getEmergencyContact());
        entity.setEmergencyRelation(dto.getEmergencyRelation());

        // Removed: currentClass, section – handled in service after fetching classEntity
        entity.setAcademicYear(dto.getAcademicYear());
        entity.setAdmissionDate(dto.getAdmissionDate());
        entity.setClassTeacher(dto.getClassTeacher());
        entity.setPreviousSchool(dto.getPreviousSchool());
        entity.setStudentCreateBy(dto.getStudentCreateBy());
        entity.setReferenceBy(dto.getReferenceBy());

        if (dto.getSubjects() != null) {
            entity.setSubjects(dto.getSubjects());
        } else {
            entity.setSubjects(new ArrayList<>());
        }

        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "Active");
        entity.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "System");
        entity.setStudentReferral(dto.getStudentReferral());

        return entity;
    }

    private void processImages(StudentEntity entity, StudentRequestDto dto) throws IOException {
        if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
            entity.setProfileImage(dto.getProfileImage().getBytes());
        }
        if (dto.getStudentAadharImage() != null && !dto.getStudentAadharImage().isEmpty()) {
            entity.setStudentAadharImage(dto.getStudentAadharImage().getBytes());
        }
        if (dto.getFatherAadharImage() != null && !dto.getFatherAadharImage().isEmpty()) {
            entity.setFatherAadharImage(dto.getFatherAadharImage().getBytes());
        }
        if (dto.getMotherAadharImage() != null && !dto.getMotherAadharImage().isEmpty()) {
            entity.setMotherAadharImage(dto.getMotherAadharImage().getBytes());
        }
        if (dto.getBirthCertificateImage() != null && !dto.getBirthCertificateImage().isEmpty()) {
            entity.setBirthCertificateImage(dto.getBirthCertificateImage().getBytes());
        }
        if (dto.getTransferCertificateImage() != null && !dto.getTransferCertificateImage().isEmpty()) {
            entity.setTransferCertificateImage(dto.getTransferCertificateImage().getBytes());
        }
        if (dto.getMarkSheetImage() != null && !dto.getMarkSheetImage().isEmpty()) {
            entity.setMarkSheetImage(dto.getMarkSheetImage().getBytes());
        }
        if (dto.getIncomeCertificateImage() != null && !dto.getIncomeCertificateImage().isEmpty()) {
            entity.setIncomeCertificateImage(dto.getIncomeCertificateImage().getBytes());
        }
        if (dto.getCasteCertificateImage() != null && !dto.getCasteCertificateImage().isEmpty()) {
            entity.setCasteCertificateImage(dto.getCasteCertificateImage().getBytes());
        }
    }

    private void updateStudentFields(StudentEntity entity, StudentRequestDto dto) {
        if (dto.getFirstName() != null) entity.setFirstName(dto.getFirstName());
        if (dto.getMiddleName() != null) entity.setMiddleName(dto.getMiddleName());
        if (dto.getLastName() != null) entity.setLastName(dto.getLastName());
// ✅ Encrypt password if being updated
        if (dto.getStudentPassword() != null && !dto.getStudentPassword().isEmpty()) {
            String encryptedPassword = passwordEncoder.encode(dto.getStudentPassword());
            entity.setStudentPassword(encryptedPassword);
            logger.debug("Password updated for student: {}", entity.getStudentId());
        }
        if (dto.getDateOfBirth() != null) entity.setDateOfBirth(dto.getDateOfBirth());

        if (dto.getGender() != null) entity.setGender(dto.getGender());
        if (dto.getBloodGroup() != null) entity.setBloodGroup(dto.getBloodGroup());
        if (dto.getAadharNumber() != null) entity.setAadharNumber(dto.getAadharNumber());
        if (dto.getCasteCategory() != null) entity.setCasteCategory(dto.getCasteCategory());
        if (dto.getMedicalInfo() != null) entity.setMedicalInfo(dto.getMedicalInfo());
        if (dto.getLocalAddress() != null) entity.setLocalAddress(dto.getLocalAddress());
        if (dto.getLocalCity() != null) entity.setLocalCity(dto.getLocalCity());
        if (dto.getLocalState() != null) entity.setLocalState(dto.getLocalState());
        if (dto.getLocalPincode() != null) entity.setLocalPincode(dto.getLocalPincode());
        if (dto.getPermanentAddress() != null) entity.setPermanentAddress(dto.getPermanentAddress());
        if (dto.getPermanentCity() != null) entity.setPermanentCity(dto.getPermanentCity());
        if (dto.getPermanentState() != null) entity.setPermanentState(dto.getPermanentState());
        if (dto.getPermanentPincode() != null) entity.setPermanentPincode(dto.getPermanentPincode());
        if (dto.getFatherName() != null) entity.setFatherName(dto.getFatherName());
        if (dto.getFatherOccupation() != null) entity.setFatherOccupation(dto.getFatherOccupation());
        if (dto.getFatherPhone() != null) entity.setFatherPhone(dto.getFatherPhone());
        if (dto.getFatherEmail() != null) entity.setFatherEmail(dto.getFatherEmail());
        if (dto.getMotherName() != null) entity.setMotherName(dto.getMotherName());
        if (dto.getMotherOccupation() != null) entity.setMotherOccupation(dto.getMotherOccupation());
        if (dto.getMotherPhone() != null) entity.setMotherPhone(dto.getMotherPhone());
        if (dto.getMotherEmail() != null) entity.setMotherEmail(dto.getMotherEmail());
        if (dto.getGuardianName() != null) entity.setGuardianName(dto.getGuardianName());
        if (dto.getGuardianRelation() != null) entity.setGuardianRelation(dto.getGuardianRelation());
        if (dto.getGuardianPhone() != null) entity.setGuardianPhone(dto.getGuardianPhone());
        if (dto.getGuardianEmail() != null) entity.setGuardianEmail(dto.getGuardianEmail());
        if (dto.getEmergencyContact() != null) entity.setEmergencyContact(dto.getEmergencyContact());
        if (dto.getEmergencyRelation() != null) entity.setEmergencyRelation(dto.getEmergencyRelation());

        // Removed: currentClass, section – handled via classId in service
        if (dto.getAcademicYear() != null) entity.setAcademicYear(dto.getAcademicYear());
        if (dto.getAdmissionDate() != null) entity.setAdmissionDate(dto.getAdmissionDate());
        if (dto.getClassTeacher() != null) entity.setClassTeacher(dto.getClassTeacher());
        if (dto.getPreviousSchool() != null) entity.setPreviousSchool(dto.getPreviousSchool());
        if (dto.getStudentCreateBy() != null) entity.setStudentCreateBy(dto.getStudentCreateBy());
        if (dto.getReferenceBy() != null) entity.setReferenceBy(dto.getReferenceBy());
        if (dto.getSportsActivity() != null) entity.setSportsActivity(dto.getSportsActivity());
        if (dto.getSubjects() != null) entity.setSubjects(dto.getSubjects());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getCreatedBy() != null) entity.setCreatedBy(dto.getCreatedBy());
        if (dto.getStudentReferral() != null) entity.setStudentReferral(dto.getStudentReferral());
    }

    private StudentResponseDto convertToDto(StudentEntity entity) {
        StudentResponseDto dto = new StudentResponseDto();

        // Map all student fields
        dto.setStdId(entity.getStdId());
        dto.setStudentId(entity.getStudentId());
        dto.setStudentRollNumber(entity.getStudentRollNumber());
        dto.setFirstName(entity.getFirstName());
        dto.setMiddleName(entity.getMiddleName());
        dto.setLastName(entity.getLastName());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setGender(entity.getGender());
        dto.setBloodGroup(entity.getBloodGroup());
        dto.setAadharNumber(entity.getAadharNumber());
        dto.setCasteCategory(entity.getCasteCategory());
        dto.setMedicalInfo(entity.getMedicalInfo());

        if (entity.getSportsActivity() != null) {
            dto.setSportsActivity(entity.getSportsActivity());
        } else {
            dto.setSportsActivity(new ArrayList<>());
        }

        dto.setLocalAddress(entity.getLocalAddress());
        dto.setLocalCity(entity.getLocalCity());
        dto.setLocalState(entity.getLocalState());
        dto.setLocalPincode(entity.getLocalPincode());
        dto.setPermanentAddress(entity.getPermanentAddress());
        dto.setPermanentCity(entity.getPermanentCity());
        dto.setPermanentState(entity.getPermanentState());
        dto.setPermanentPincode(entity.getPermanentPincode());

        dto.setFatherName(entity.getFatherName());
        dto.setFatherOccupation(entity.getFatherOccupation());
        dto.setFatherPhone(entity.getFatherPhone());
        dto.setFatherEmail(entity.getFatherEmail());
        dto.setMotherName(entity.getMotherName());
        dto.setMotherOccupation(entity.getMotherOccupation());
        dto.setMotherPhone(entity.getMotherPhone());
        dto.setMotherEmail(entity.getMotherEmail());
        dto.setGuardianName(entity.getGuardianName());
        dto.setGuardianRelation(entity.getGuardianRelation());
        dto.setGuardianPhone(entity.getGuardianPhone());
        dto.setGuardianEmail(entity.getGuardianEmail());
        dto.setEmergencyContact(entity.getEmergencyContact());
        dto.setEmergencyRelation(entity.getEmergencyRelation());

        dto.setCurrentClass(entity.getCurrentClass());
        dto.setSection(entity.getSection());
        dto.setAcademicYear(entity.getAcademicYear());
        dto.setAdmissionDate(entity.getAdmissionDate());
        dto.setClassTeacher(entity.getClassTeacher());
        dto.setPreviousSchool(entity.getPreviousSchool());
        dto.setStudentCreateBy(entity.getStudentCreateBy());
        dto.setReferenceBy(entity.getReferenceBy());

        if (entity.getSubjects() != null) {
            dto.setSubjects(entity.getSubjects());
        } else {
            dto.setSubjects(new ArrayList<>());
        }

        // ============= ✅ RELATIVE URLs ONLY =============
        if (entity.getProfileImage() != null && entity.getProfileImage().length > 0) {
            dto.setProfileImageUrl("/api/students/" + entity.getStdId() + "/profile-image");
        }

        if (entity.getStudentAadharImage() != null && entity.getStudentAadharImage().length > 0) {
            dto.setStudentAadharImageUrl("/api/students/" + entity.getStdId() + "/aadhar-image");
        }

        if (entity.getFatherAadharImage() != null && entity.getFatherAadharImage().length > 0) {
            dto.setFatherAadharImageUrl("/api/students/" + entity.getStdId() + "/father-aadhar-image");
        }

        if (entity.getMotherAadharImage() != null && entity.getMotherAadharImage().length > 0) {
            dto.setMotherAadharImageUrl("/api/students/" + entity.getStdId() + "/mother-aadhar-image");
        }

        if (entity.getBirthCertificateImage() != null && entity.getBirthCertificateImage().length > 0) {
            dto.setBirthCertificateImageUrl("/api/students/" + entity.getStdId() + "/birth-certificate");
        }

        if (entity.getTransferCertificateImage() != null && entity.getTransferCertificateImage().length > 0) {
            dto.setTransferCertificateImageUrl("/api/students/" + entity.getStdId() + "/transfer-certificate");
        }

        if (entity.getMarkSheetImage() != null && entity.getMarkSheetImage().length > 0) {
            dto.setMarkSheetImageUrl("/api/students/" + entity.getStdId() + "/marksheet");
        }

        // ============= ✅ ADD INCOME AND CASTE CERTIFICATE URLs =============
        if (entity.getIncomeCertificateImage() != null && entity.getIncomeCertificateImage().length > 0) {
            dto.setIncomeCertificateImageUrl("/api/students/" + entity.getStdId() + "/income-certificate");
        }

        if (entity.getCasteCertificateImage() != null && entity.getCasteCertificateImage().length > 0) {
            dto.setCasteCertificateImageUrl("/api/students/" + entity.getStdId() + "/caste-certificate");
        }

        dto.setStatus(entity.getStatus());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setStudentReferral(entity.getStudentReferral());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Get fees from existing FeesService
        try {
            FeesResponseDto fees = feesService.getFeesByStudentId(entity.getStdId());
            dto.setFeesDetails(fees);
        } catch (Exception e) {
            logger.debug("No fees found for student {}", entity.getStdId());
        }
        return dto;
    }
}