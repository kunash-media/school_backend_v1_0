package com.sc.service.serviceImpl;

import com.sc.entity.ClassEntity;
import com.sc.repository.ClassRepository;
import com.sc.dto.request.TeacherRequestDto;
import com.sc.dto.response.TeacherResponseDto;
import com.sc.entity.TeacherEntity;
import com.sc.repository.TeacherRepository;
import com.sc.service.TeacherService;
import com.sc.util.TeacherIdGenerator;
import com.sc.bcrypt.BcryptEncoderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private static final Logger logger = LoggerFactory.getLogger(TeacherServiceImpl.class);


    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private BcryptEncoderConfig passwordEncoder;

    // Directory where files will be saved
    private static final String UPLOAD_DIR = "uploads/teachers";

    // ========== CREATE ==========
    @Override
    public TeacherResponseDto createTeacher(TeacherRequestDto requestDto, Map<String, MultipartFile> files, String createdBy) {
        // Validate unique fields
        validateUniqueFields(requestDto);

        // Validate password - ADD NULL CHECK
        if (requestDto.getTeacherPassword() == null || requestDto.getConfirmTeacherPassword() == null) {
            throw new RuntimeException("Password and confirm password are required");
        }

        if (!requestDto.getTeacherPassword().equals(requestDto.getConfirmTeacherPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(requestDto.getTeacherPassword());
        requestDto.setTeacherPassword(hashedPassword);
        // Convert DTO to Entity
        TeacherEntity teacherEntity = convertToEntity(requestDto);

        // Set audit fields
        teacherEntity.setCreatedBy(createdBy);
        teacherEntity.setCreatedAt(new Date());
        teacherEntity.setLastUpdated(new Date());
        teacherEntity.setTeacherCode(TeacherIdGenerator.generateTeacherId());

        // Set status if not provided
        if (teacherEntity.getStatus() == null) {
            teacherEntity.setStatus("Active");
        }

        // Process files from map (saves both bytes AND file names)
        processFilesWithNames(teacherEntity, files);

        // Calculate gross salary
        teacherEntity.calculateGrossSalary();

        // Save to database
        TeacherEntity savedEntity = teacherRepository.save(teacherEntity);

        // Convert to Response DTO and return
        return convertToResponseDto(savedEntity);
    }

    // ========== READ OPERATIONS ==========

    @Override
    public TeacherResponseDto getTeacherById(Long id) {
        Optional<TeacherEntity> teacherOpt = teacherRepository.findById(id);
        if (teacherOpt.isEmpty() || teacherOpt.get().isDeleted()) {
            throw new RuntimeException("Teacher not found with ID: " + id);
        }
        return convertToResponseDto(teacherOpt.get());
    }

    @Override
    public TeacherResponseDto getTeacherByTeacherCode(String teacherCode) {
        Optional<TeacherEntity> teacherOpt = teacherRepository.findByTeacherCode(teacherCode);
        if (teacherOpt.isEmpty() || teacherOpt.get().isDeleted()) {
            throw new RuntimeException("Teacher not found with Teacher Code: " + teacherCode);
        }
        return convertToResponseDto(teacherOpt.get());
    }

    @Override
    public TeacherResponseDto getTeacherByEmployeeId(String employeeId) {
        Optional<TeacherEntity> teacherOpt = teacherRepository.findByEmployeeId(employeeId);
        if (teacherOpt.isEmpty() || teacherOpt.get().isDeleted()) {
            throw new RuntimeException("Teacher not found with Employee ID: " + employeeId);
        }
        return convertToResponseDto(teacherOpt.get());
    }

    @Override
    public List<TeacherResponseDto> getAllTeachers() {
        List<TeacherEntity> teachers = teacherRepository.findByIsDeletedFalse();
        logger.info("=== Found {} teachers in database ===", teachers.size());

        for (TeacherEntity teacher : teachers) {
            logger.info("Teacher ID: {}, PhotoName: '{}', HasBytes: {}",
                    teacher.getId(),
                    teacher.getTeacherPhotoName(),
                    teacher.getTeacherPhoto() != null && teacher.getTeacherPhoto().length > 0);
        }

        return teachers.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherResponseDto> getActiveTeachers() {
        List<TeacherEntity> teachers = teacherRepository.findByStatusAndIsDeletedFalse("Active");
        return teachers.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherResponseDto> getTeachersByStatus(String status) {
        List<TeacherEntity> teachers = teacherRepository.findByStatusAndIsDeletedFalse(status);
        return teachers.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherResponseDto> getTeachersByDepartment(String department) {
        List<TeacherEntity> teachers = teacherRepository.findByDepartmentAndStatus(department, "Active");
        return teachers.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherResponseDto> searchTeachersByName(String name) {
        List<TeacherEntity> teachers = teacherRepository.searchByName(name);
        return teachers.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // ========== UPDATE OPERATIONS ==========

    @Override
    public TeacherResponseDto updateTeacher(Long id, TeacherRequestDto requestDto) {
        Optional<TeacherEntity> existingTeacherOpt = teacherRepository.findById(id);
        if (existingTeacherOpt.isEmpty() || existingTeacherOpt.get().isDeleted()) {
            throw new RuntimeException("Teacher not found with ID: " + id);
        }

        TeacherEntity existingTeacher = existingTeacherOpt.get();

        // Check if email is being changed
        if (!existingTeacher.getEmail().equals(requestDto.getEmail()) &&
                isEmailExists(requestDto.getEmail())) {
            throw new RuntimeException("Email already exists: " + requestDto.getEmail());
        }

        // Check if contact number is being changed
        if (!existingTeacher.getContactNumber().equals(requestDto.getContactNumber()) &&
                isContactNumberExists(requestDto.getContactNumber())) {
            throw new RuntimeException("Contact number already exists: " + requestDto.getContactNumber());
        }

        // Hash password if it's being updated
        if (requestDto.getTeacherPassword() != null && !requestDto.getTeacherPassword().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(requestDto.getTeacherPassword());
            requestDto.setTeacherPassword(hashedPassword);
        }

        // Update fields from DTO
        updateEntityFromDto(existingTeacher, requestDto);

        // Recalculate gross salary
        existingTeacher.calculateGrossSalary();

        // Update timestamp
        existingTeacher.setLastUpdated(new Date());

        // Save updated entity
        TeacherEntity updatedEntity = teacherRepository.save(existingTeacher);

        return convertToResponseDto(updatedEntity);
    }

    @Override
    public TeacherResponseDto updateTeacherWithFiles(Long id, TeacherRequestDto requestDto, Map<String, MultipartFile> files) {
        Optional<TeacherEntity> existingTeacherOpt = teacherRepository.findById(id);
        if (existingTeacherOpt.isEmpty() || existingTeacherOpt.get().isDeleted()) {
            throw new RuntimeException("Teacher not found with ID: " + id);
        }

        TeacherEntity existingTeacher = existingTeacherOpt.get();

        // Check if email is being changed
        if (!existingTeacher.getEmail().equals(requestDto.getEmail()) &&
                isEmailExists(requestDto.getEmail())) {
            throw new RuntimeException("Email already exists: " + requestDto.getEmail());
        }

        // Check if contact number is being changed
        if (!existingTeacher.getContactNumber().equals(requestDto.getContactNumber()) &&
                isContactNumberExists(requestDto.getContactNumber())) {
            throw new RuntimeException("Contact number already exists: " + requestDto.getContactNumber());
        }

        // Hash password if it's being updated
        if (requestDto.getTeacherPassword() != null && !requestDto.getTeacherPassword().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(requestDto.getTeacherPassword());
            requestDto.setTeacherPassword(hashedPassword);
        }

        // Update fields from DTO
        updateEntityFromDto(existingTeacher, requestDto);

        // Process files if provided (saves both bytes AND file names)
        if (files != null && !files.isEmpty()) {
            processFilesWithNames(existingTeacher, files);
        }

        // Recalculate gross salary
        existingTeacher.calculateGrossSalary();

        // Update timestamp
        existingTeacher.setLastUpdated(new Date());

        // Save updated entity
        TeacherEntity updatedEntity = teacherRepository.save(existingTeacher);

        return convertToResponseDto(updatedEntity);
    }

    @Override
    public TeacherResponseDto updateTeacherStatus(String teacherCode, String status) {
        Optional<TeacherEntity> teacherOpt = teacherRepository.findByTeacherCode(teacherCode);
        if (teacherOpt.isEmpty() || teacherOpt.get().isDeleted()) {
            throw new RuntimeException("Teacher not found with Teacher Code: " + teacherCode);
        }

        TeacherEntity teacher = teacherOpt.get();
        teacher.setStatus(status);
        teacher.setLastUpdated(new Date());

        TeacherEntity updatedEntity = teacherRepository.save(teacher);
        return convertToResponseDto(updatedEntity);
    }

    @Override
    public TeacherResponseDto updateTeacherPassword(String teacherCode, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Passwords do not match");
        }

        Optional<TeacherEntity> teacherOpt = teacherRepository.findByTeacherCode(teacherCode);
        if (teacherOpt.isEmpty() || teacherOpt.get().isDeleted()) {
            throw new RuntimeException("Teacher not found with Teacher Code: " + teacherCode);
        }

        TeacherEntity teacher = teacherOpt.get();

        // Hash the new password
        String hashedPassword = passwordEncoder.encode(newPassword);
        teacher.setTeacherPassword(hashedPassword);
        teacher.setLastUpdated(new Date());

        TeacherEntity updatedEntity = teacherRepository.save(teacher);
        return convertToResponseDto(updatedEntity);
    }

    // ========== DOCUMENT OPERATIONS ==========

    @Override
    public TeacherResponseDto uploadDocuments(String teacherCode, Map<String, String> fileNames) {
        Optional<TeacherEntity> teacherOpt = teacherRepository.findByTeacherCode(teacherCode);
        if (teacherOpt.isEmpty() || teacherOpt.get().isDeleted()) {
            throw new RuntimeException("Teacher not found with Teacher Code: " + teacherCode);
        }

        TeacherEntity teacher = teacherOpt.get();
        logger.info("Received file names for teacher {}: {}", teacherCode, fileNames);
        teacher.setLastUpdated(new Date());

        TeacherEntity updatedEntity = teacherRepository.save(teacher);
        return convertToResponseDto(updatedEntity);
    }

    @Override
    public TeacherResponseDto updateSingleDocument(String teacherCode, String documentType, MultipartFile file) {
        Optional<TeacherEntity> teacherOpt = teacherRepository.findByTeacherCode(teacherCode);
        if (teacherOpt.isEmpty() || teacherOpt.get().isDeleted()) {
            throw new RuntimeException("Teacher not found with Teacher Code: " + teacherCode);
        }

        TeacherEntity teacher = teacherOpt.get();

        try {
            String fileName = saveFileToDisk(file, documentType);

            switch (documentType.toLowerCase()) {
                case "teacherphoto":
                    teacher.setTeacherPhoto(file.getBytes());
                    teacher.setTeacherPhotoName(fileName);
                    break;
                case "aadhardocument":
                    teacher.setAadharDocument(file.getBytes());
                    teacher.setAadharDocumentName(fileName);
                    break;
                case "pandocument":
                    teacher.setPanDocument(file.getBytes());
                    teacher.setPanDocumentName(fileName);
                    break;
                case "educationdocument":
                    teacher.setEducationDocument(file.getBytes());
                    teacher.setEducationDocumentName(fileName);
                    break;
                case "beddocument":
                    teacher.setBedDocument(file.getBytes());
                    teacher.setBedDocumentName(fileName);
                    break;
                case "experiencedocument":
                    teacher.setExperienceDocument(file.getBytes());
                    teacher.setExperienceDocumentName(fileName);
                    break;
                case "policeverificationdocument":
                    teacher.setPoliceVerificationDocument(file.getBytes());
                    teacher.setPoliceVerificationDocumentName(fileName);
                    break;
                case "medicalfitnessdocument":
                    teacher.setMedicalFitnessDocument(file.getBytes());
                    teacher.setMedicalFitnessDocumentName(fileName);
                    break;
                case "resumedocument":
                    teacher.setResumeDocument(file.getBytes());
                    teacher.setResumeDocumentName(fileName);
                    break;
                default:
                    throw new RuntimeException("Invalid document type: " + documentType);
            }

            teacher.setLastUpdated(new Date());
            TeacherEntity updatedEntity = teacherRepository.save(teacher);
            return convertToResponseDto(updatedEntity);

        } catch (IOException e) {
            throw new RuntimeException("Error processing uploaded file: " + e.getMessage());
        }
    }

    // ========== DELETE OPERATIONS ==========

    @Override
    public void deleteTeacher(Long id) {
        Optional<TeacherEntity> teacherOpt = teacherRepository.findById(id);
        if (teacherOpt.isEmpty()) {
            throw new RuntimeException("Teacher not found with ID: " + id);
        }
        teacherRepository.deleteById(id);
    }

    @Override
    public void softDeleteTeacher(String teacherCode) {
        int updated = teacherRepository.softDeleteByTeacherCode(teacherCode);
        if (updated == 0) {
            throw new RuntimeException("Teacher not found with Teacher Code: " + teacherCode);
        }
    }

    // ========== VALIDATION METHODS ==========

    @Override
    public boolean isEmailExists(String email) {
        return teacherRepository.existsByEmailAndIsDeletedFalse(email);
    }

    @Override
    public boolean isContactNumberExists(String contactNumber) {
        return teacherRepository.existsByContactNumberAndIsDeletedFalse(contactNumber);
    }

    @Override
    public boolean isAadharExists(String aadharNumber) {
        return teacherRepository.existsByAadharNumberAndIsDeletedFalse(aadharNumber);
    }

    @Override
    public boolean isPanExists(String panNumber) {
        return teacherRepository.existsByPanNumberAndIsDeletedFalse(panNumber);
    }

    @Override
    public boolean isEmployeeIdExists(String employeeId) {
        return teacherRepository.existsByEmployeeIdAndIsDeletedFalse(employeeId);
    }

    @Override
    public boolean isTeacherCodeExists(String teacherCode) {
        return teacherRepository.existsByTeacherCodeAndIsDeletedFalse(teacherCode);
    }

    // ========== TEACHER CLASSES METHOD ==========
// Add this to TeacherServiceImpl.java


    @Override
    public List<?> getTeacherClasses(Long teacherId) {
        logger.info("Fetching classes for teacher ID: {}", teacherId);

        try {
            Optional<TeacherEntity> teacherOpt = teacherRepository.findById(teacherId);
            if (teacherOpt.isEmpty()) {
                logger.warn("Teacher not found with ID: {}", teacherId);
                return new ArrayList<>();
            }

            TeacherEntity teacher = teacherOpt.get();
            List<String> classNames = teacher.getClasses();

            if (classNames == null || classNames.isEmpty()) {
                logger.info("No classes assigned to teacher ID: {}", teacherId);
                return new ArrayList<>();
            }

            List<Map<String, Object>> classList = new ArrayList<>();

            for (String className : classNames) {
                try {
                    // Find the actual class from database by className
                    List<ClassEntity> matchingClasses = classRepository.findByClassName(className);

                    for (ClassEntity classEntity : matchingClasses) {
                        Map<String, Object> classInfo = new HashMap<>();
                        // Use REAL database values
                        classInfo.put("id", classEntity.getClassId());
                        classInfo.put("classId", classEntity.getClassId());
                        classInfo.put("className", classEntity.getClassName());
                        classInfo.put("section", classEntity.getSection()); // Use actual section from database
                        classInfo.put("classCode", classEntity.getClassCode());
                        classInfo.put("startTime", classEntity.getStartTime() != null ? classEntity.getStartTime() : "08:30");
                        classInfo.put("endTime", classEntity.getEndTime() != null ? classEntity.getEndTime() : "13:30");
                        classInfo.put("roomNumber", classEntity.getRoomNumber() != null ? classEntity.getRoomNumber() : "Not assigned");

                        if (classEntity.getWorkingDays() != null && !classEntity.getWorkingDays().isEmpty()) {
                            classInfo.put("workingDays", classEntity.getWorkingDays());
                        } else {
                            classInfo.put("workingDays", Arrays.asList("monday", "tuesday", "wednesday", "thursday", "friday"));
                        }

                        classList.add(classInfo);
                        logger.info("Added class: ID={}, Name={}, Section={}",
                                classEntity.getClassId(), classEntity.getClassName(), classEntity.getSection());
                    }
                } catch (Exception e) {
                    logger.error("Error processing class name '{}': {}", className, e.getMessage());
                }
            }

            logger.info("Found {} real classes for teacher ID: {}", classList.size(), teacherId);
            return classList;

        } catch (Exception e) {
            logger.error("Error fetching teacher classes: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    // ========== UTILITY METHODS ==========

    @Override
    public String generateTeacherCode() {
        return TeacherIdGenerator.generateTeacherId();
    }

    @Override
    public long getTeacherCount() {
        return teacherRepository.countByIsDeletedFalse();
    }

    @Override
    public long getActiveTeacherCount() {
        return teacherRepository.countByStatusAndIsDeletedFalse("Active");
    }

    @Override
    public List<String> getAllDepartments() {
        return teacherRepository.findAllDepartments();
    }

    // ========== AUTHENTICATION METHODS ==========

    @Override
    public boolean authenticateTeacher(String employeeId, String rawPassword) {
        Optional<TeacherEntity> teacherOpt = teacherRepository.findByEmployeeId(employeeId);
        if (teacherOpt.isEmpty() || teacherOpt.get().isDeleted()) {
            return false;
        }

        TeacherEntity teacher = teacherOpt.get();
        String storedHashedPassword = teacher.getTeacherPassword();

        if (storedHashedPassword == null || rawPassword == null) {
            return false;
        }

        return passwordEncoder.matches(rawPassword, storedHashedPassword);
    }

    @Override
    public boolean authenticateTeacherByCode(String teacherCode, String rawPassword) {
        Optional<TeacherEntity> teacherOpt = teacherRepository.findByTeacherCode(teacherCode);
        if (teacherOpt.isEmpty() || teacherOpt.get().isDeleted()) {
            return false;
        }

        TeacherEntity teacher = teacherOpt.get();
        String storedHashedPassword = teacher.getTeacherPassword();

        if (storedHashedPassword == null || rawPassword == null) {
            return false;
        }

        return passwordEncoder.matches(rawPassword, storedHashedPassword);
    }

    // ========== PRIVATE HELPER METHODS ==========

    private void validateUniqueFields(TeacherRequestDto requestDto) {
        if (isEmailExists(requestDto.getEmail())) {
            throw new RuntimeException("Email already exists: " + requestDto.getEmail());
        }
        if (isContactNumberExists(requestDto.getContactNumber())) {
            throw new RuntimeException("Contact number already exists: " + requestDto.getContactNumber());
        }
        if (isAadharExists(requestDto.getAadharNumber())) {
            throw new RuntimeException("Aadhar number already exists: " + requestDto.getAadharNumber());
        }
        if (isPanExists(requestDto.getPanNumber())) {
            throw new RuntimeException("PAN number already exists: " + requestDto.getPanNumber());
        }
        if (isEmployeeIdExists(requestDto.getEmployeeId())) {
            throw new RuntimeException("Employee ID already exists: " + requestDto.getEmployeeId());
        }
    }

    // ✅ NEW METHOD: Process files and save both bytes AND file names
    private void processFilesWithNames(TeacherEntity entity, Map<String, MultipartFile> files) {
        try {
            for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
                String fileType = entry.getKey();
                MultipartFile file = entry.getValue();

                if (file != null && !file.isEmpty()) {
                    // Generate unique file name
                    String fileName = generateFileName(file, fileType);

                    // Save file to disk
                    saveFileToDisk(file, fileName);

                    // Set both bytes and file name in entity
                    switch (fileType) {
                        case "teacherPhoto":
                            entity.setTeacherPhoto(file.getBytes());
                            entity.setTeacherPhotoName(fileName);
                            logger.info("Saved teacher photo: {}", fileName);
                            break;
                        case "aadharDocument":
                            entity.setAadharDocument(file.getBytes());
                            entity.setAadharDocumentName(fileName);
                            logger.info("Saved Aadhar document: {}", fileName);
                            break;
                        case "panDocument":
                            entity.setPanDocument(file.getBytes());
                            entity.setPanDocumentName(fileName);
                            logger.info("Saved PAN document: {}", fileName);
                            break;
                        case "educationDocument":
                            entity.setEducationDocument(file.getBytes());
                            entity.setEducationDocumentName(fileName);
                            logger.info("Saved Education document: {}", fileName);
                            break;
                        case "bedDocument":
                            entity.setBedDocument(file.getBytes());
                            entity.setBedDocumentName(fileName);
                            logger.info("Saved B.Ed document: {}", fileName);
                            break;
                        case "experienceDocument":
                            entity.setExperienceDocument(file.getBytes());
                            entity.setExperienceDocumentName(fileName);
                            logger.info("Saved Experience document: {}", fileName);
                            break;
                        case "policeVerificationDocument":
                            entity.setPoliceVerificationDocument(file.getBytes());
                            entity.setPoliceVerificationDocumentName(fileName);
                            logger.info("Saved Police Verification document: {}", fileName);
                            break;
                        case "medicalFitnessDocument":
                            entity.setMedicalFitnessDocument(file.getBytes());
                            entity.setMedicalFitnessDocumentName(fileName);
                            logger.info("Saved Medical Fitness document: {}", fileName);
                            break;
                        case "resumeDocument":
                            entity.setResumeDocument(file.getBytes());
                            entity.setResumeDocumentName(fileName);
                            logger.info("Saved Resume document: {}", fileName);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error processing uploaded files: {}", e.getMessage());
            throw new RuntimeException("Error processing uploaded files: " + e.getMessage());
        }
    }

    // ✅ NEW METHOD: Generate unique file name
    private String generateFileName(MultipartFile file, String prefix) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomStr = UUID.randomUUID().toString().substring(0, 8);
        return prefix + "_" + timestamp + "_" + randomStr + extension;
    }

    // ✅ NEW METHOD: Save file to disk
    private String saveFileToDisk(MultipartFile file, String fileName) throws IOException {
        // Create directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("Created upload directory: {}", uploadPath.toAbsolutePath());
        }

        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());
        logger.info("File saved to: {}", filePath.toAbsolutePath());

        return fileName;
    }


    private TeacherEntity convertToEntity(TeacherRequestDto dto) {
        TeacherEntity entity = new TeacherEntity();

        // Basic Info
        entity.setEmployeeId(dto.getEmployeeId());
        entity.setFirstName(dto.getFirstName());
        entity.setMiddleName(dto.getMiddleName());
        entity.setLastName(dto.getLastName());
        entity.setDob(dto.getDob());
        entity.setGender(dto.getGender());
        entity.setBloodGroup(dto.getBloodGroup());

        // Contact & Address
        entity.setEmail(dto.getEmail());
        entity.setContactNumber(dto.getContactNumber());
        entity.setAddress(dto.getAddress());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setPincode(dto.getPincode());

        // Emergency Contact
        entity.setEmergencyContactName(dto.getEmergencyContactName());
        entity.setEmergencyContactNumber(dto.getEmergencyContactNumber());

        // Government IDs
        entity.setAadharNumber(dto.getAadharNumber());
        entity.setPanNumber(dto.getPanNumber());

        // Medical
        entity.setMedicalInfo(dto.getMedicalInfo());

        // Professional Details
        entity.setJoiningDate(dto.getJoiningDate());
        entity.setDesignation(dto.getDesignation());
        entity.setTotalExperience(dto.getTotalExperience());
        entity.setDepartment(dto.getDepartment());
        entity.setEmploymentType(dto.getEmploymentType());

        // Experience
        if (dto.getPreviousExperience() != null) {
            List<TeacherEntity.PreviousExperience> experienceList = dto.getPreviousExperience().stream()
                    .map(exp -> {
                        TeacherEntity.PreviousExperience pe = new TeacherEntity.PreviousExperience();
                        pe.setSchool(exp.getSchool());
                        pe.setPosition(exp.getPosition());
                        pe.setDuration(exp.getDuration());
                        return pe;
                    })
                    .collect(Collectors.toList());
            entity.setPreviousExperience(experienceList);
        }

        // Qualifications
        if (dto.getQualifications() != null) {
            List<TeacherEntity.Qualification> qualificationList = dto.getQualifications().stream()
                    .map(qual -> {
                        TeacherEntity.Qualification q = new TeacherEntity.Qualification();
                        q.setDegree(qual.getDegree());
                        q.setSpecialization(qual.getSpecialization());
                        q.setUniversity(qual.getUniversity());
                        q.setCompletionYear(qual.getCompletionYear());
                        return q;
                    })
                    .collect(Collectors.toList());
            entity.setQualifications(qualificationList);
        }

        // Teaching Details
        entity.setPrimarySubject(dto.getPrimarySubject());
        entity.setAdditionalSubjects(dto.getAdditionalSubjects());
        entity.setClasses(dto.getClasses());

        // Salary
        entity.setBasicSalary(dto.getBasicSalary());
        entity.setHra(dto.getHra());
        entity.setDa(dto.getDa());
        entity.setTa(dto.getTa());

        // Allowances
        if (dto.getAdditionalAllowances() != null) {
            List<TeacherEntity.AdditionalAllowance> allowanceList = dto.getAdditionalAllowances().stream()
                    .map(all -> {
                        TeacherEntity.AdditionalAllowance aa = new TeacherEntity.AdditionalAllowance();
                        aa.setName(all.getName());
                        aa.setAmount(all.getAmount());
                        return aa;
                    })
                    .collect(Collectors.toList());
            entity.setAdditionalAllowances(allowanceList);
        }

        // Bank Details
        entity.setBankName(dto.getBankName());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setIfscCode(dto.getIfscCode());
        entity.setBranchName(dto.getBranchName());

        // Account - Password is already hashed
        entity.setTeacherPassword(dto.getTeacherPassword());

        // Status
        entity.setStatus(dto.getStatus());

        return entity;
    }

    // ✅ UPDATED convertToResponseDto with document names and photo URL
    private TeacherResponseDto convertToResponseDto(TeacherEntity entity) {
        TeacherResponseDto dto = new TeacherResponseDto();

        // IDs
        dto.setId(entity.getId());
        dto.setTeacherCode(entity.getTeacherCode());
        dto.setEmployeeId(entity.getEmployeeId());

        // Personal Info
        dto.setFirstName(entity.getFirstName());
        dto.setMiddleName(entity.getMiddleName());
        dto.setLastName(entity.getLastName());
        dto.setFullName(entity.getFullName());
        dto.setDob(entity.getDob());
        dto.setFormattedDob(entity.getFormattedDob());
        dto.setGender(entity.getGender());
        dto.setBloodGroup(entity.getBloodGroup());

        // Contact & Address
        dto.setEmail(entity.getEmail());
        dto.setContactNumber(entity.getContactNumber());
        dto.setAddress(entity.getAddress());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setPincode(entity.getPincode());

        // Emergency Contact
        dto.setEmergencyContactName(entity.getEmergencyContactName());
        dto.setEmergencyContactNumber(entity.getEmergencyContactNumber());

        // Government IDs
        dto.setAadharNumber(entity.getAadharNumber());
        dto.setPanNumber(entity.getPanNumber());

        // Medical
        dto.setMedicalInfo(entity.getMedicalInfo());

        // Professional Details
        dto.setJoiningDate(entity.getJoiningDate());
        dto.setFormattedJoiningDate(entity.getFormattedJoiningDate());
        dto.setDesignation(entity.getDesignation());
        dto.setTotalExperience(entity.getTotalExperience());
        dto.setDepartment(entity.getDepartment());
        dto.setEmploymentType(entity.getEmploymentType());

        // Experience
        if (entity.getPreviousExperience() != null) {
            List<TeacherResponseDto.ExperienceDto> experienceList = entity.getPreviousExperience().stream()
                    .map(exp -> {
                        TeacherResponseDto.ExperienceDto ed = new TeacherResponseDto.ExperienceDto();
                        ed.setSchool(exp.getSchool());
                        ed.setPosition(exp.getPosition());
                        ed.setDuration(exp.getDuration());
                        return ed;
                    })
                    .collect(Collectors.toList());
            dto.setPreviousExperience(experienceList);
        }

        // Qualifications
        if (entity.getQualifications() != null) {
            List<TeacherResponseDto.QualificationDto> qualificationList = entity.getQualifications().stream()
                    .map(qual -> {
                        TeacherResponseDto.QualificationDto qd = new TeacherResponseDto.QualificationDto();
                        qd.setDegree(qual.getDegree());
                        qd.setSpecialization(qual.getSpecialization());
                        qd.setUniversity(qual.getUniversity());
                        qd.setCompletionYear(qual.getCompletionYear());
                        return qd;
                    })
                    .collect(Collectors.toList());
            dto.setQualifications(qualificationList);
        }

        // Teaching Details
        dto.setPrimarySubject(entity.getPrimarySubject());
        dto.setAdditionalSubjects(entity.getAdditionalSubjects());
        dto.setClasses(entity.getClasses());

        // Salary
        dto.setBasicSalary(entity.getBasicSalary());
        dto.setHra(entity.getHra());
        dto.setDa(entity.getDa());
        dto.setTa(entity.getTa());

        // Allowances
        if (entity.getAdditionalAllowances() != null) {
            List<TeacherResponseDto.AllowanceDto> allowanceList = entity.getAdditionalAllowances().stream()
                    .map(all -> {
                        TeacherResponseDto.AllowanceDto ad = new TeacherResponseDto.AllowanceDto();
                        ad.setName(all.getName());
                        ad.setAmount(all.getAmount());
                        return ad;
                    })
                    .collect(Collectors.toList());
            dto.setAdditionalAllowances(allowanceList);
        }

        // Gross Salary
        dto.setGrossSalary(entity.getGrossSalary());

        // Bank Details
        dto.setBankName(entity.getBankName());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setIfscCode(entity.getIfscCode());
        dto.setBranchName(entity.getBranchName());

        // Status & Admin
        dto.setStatus(entity.getStatus());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setLastUpdated(entity.getLastUpdated());

        // ✅ DOCUMENT NAMES
        dto.setAadharDocumentName(entity.getAadharDocumentName());
        dto.setPanDocumentName(entity.getPanDocumentName());
        dto.setEducationDocumentName(entity.getEducationDocumentName());
        dto.setBedDocumentName(entity.getBedDocumentName());
        dto.setExperienceDocumentName(entity.getExperienceDocumentName());
        dto.setPoliceVerificationDocumentName(entity.getPoliceVerificationDocumentName());
        dto.setMedicalFitnessDocumentName(entity.getMedicalFitnessDocumentName());
        dto.setResumeDocumentName(entity.getResumeDocumentName());

        // ========== DEBUG LOGS ==========
        logger.info("=== DEBUG convertToResponseDto for Teacher ID: {} ===", entity.getId());
        logger.info("TeacherPhotoName: '{}'", entity.getTeacherPhotoName());
        logger.info("TeacherPhoto bytes exist: {}", entity.getTeacherPhoto() != null && entity.getTeacherPhoto().length > 0);
        if (entity.getTeacherPhoto() != null && entity.getTeacherPhoto().length > 0) {
            logger.info("TeacherPhoto bytes length: {} bytes", entity.getTeacherPhoto().length);
        }

        // ✅ PHOTO URL - CRITICAL FOR FRONTEND
        if (entity.getTeacherPhotoName() != null && !entity.getTeacherPhotoName().isEmpty()) {
            String photoUrl = "/api/teachers/uploads/teachers/" + entity.getTeacherPhotoName();
            dto.setTeacherPhotoUrl(photoUrl);
            logger.info("✅ Setting photo URL from filename: {}", photoUrl);
        } else if (entity.getTeacherPhoto() != null && entity.getTeacherPhoto().length > 0) {
            String photoUrl = "/api/teachers/photo/" + entity.getId();
            dto.setTeacherPhotoUrl(photoUrl);
            logger.info("✅ Setting photo URL from bytes (fallback): {}", photoUrl);
        } else {
            logger.info("❌ No photo found for teacher ID: {}", entity.getId());
            dto.setTeacherPhotoUrl(null);
        }

        return dto;
    }

    private void updateEntityFromDto(TeacherEntity entity, TeacherRequestDto dto) {


        // Update basic fields
        if (dto.getFirstName() != null) entity.setFirstName(dto.getFirstName());
        if (dto.getMiddleName() != null) entity.setMiddleName(dto.getMiddleName());
        if (dto.getLastName() != null) entity.setLastName(dto.getLastName());
        if (dto.getDob() != null) entity.setDob(dto.getDob());
        if (dto.getGender() != null) entity.setGender(dto.getGender());
        if (dto.getBloodGroup() != null) entity.setBloodGroup(dto.getBloodGroup());

        if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
        if (dto.getContactNumber() != null) entity.setContactNumber(dto.getContactNumber());
        if (dto.getAddress() != null) entity.setAddress(dto.getAddress());
        if (dto.getCity() != null) entity.setCity(dto.getCity());
        if (dto.getState() != null) entity.setState(dto.getState());
        if (dto.getPincode() != null) entity.setPincode(dto.getPincode());

        if (dto.getEmergencyContactName() != null) entity.setEmergencyContactName(dto.getEmergencyContactName());
        if (dto.getEmergencyContactNumber() != null) entity.setEmergencyContactNumber(dto.getEmergencyContactNumber());

        // Professional fields
        if (dto.getJoiningDate() != null) entity.setJoiningDate(dto.getJoiningDate());
        if (dto.getDesignation() != null) entity.setDesignation(dto.getDesignation());
        if (dto.getTotalExperience() != null) entity.setTotalExperience(dto.getTotalExperience());
        if (dto.getDepartment() != null) entity.setDepartment(dto.getDepartment());
        if (dto.getEmploymentType() != null) entity.setEmploymentType(dto.getEmploymentType());

        // Teaching fields
        if (dto.getPrimarySubject() != null) entity.setPrimarySubject(dto.getPrimarySubject());
        if (dto.getAdditionalSubjects() != null) entity.setAdditionalSubjects(dto.getAdditionalSubjects());
        if (dto.getClasses() != null) entity.setClasses(dto.getClasses());

        // Salary fields
        if (dto.getBasicSalary() != null) entity.setBasicSalary(dto.getBasicSalary());
        if (dto.getHra() != null) entity.setHra(dto.getHra());
        if (dto.getDa() != null) entity.setDa(dto.getDa());
        if (dto.getTa() != null) entity.setTa(dto.getTa());

        // Collections
        if (dto.getPreviousExperience() != null) {
            List<TeacherEntity.PreviousExperience> experienceList = dto.getPreviousExperience().stream()
                    .map(exp -> {
                        TeacherEntity.PreviousExperience pe = new TeacherEntity.PreviousExperience();
                        pe.setSchool(exp.getSchool());
                        pe.setPosition(exp.getPosition());
                        pe.setDuration(exp.getDuration());
                        return pe;
                    })
                    .collect(Collectors.toList());
            entity.setPreviousExperience(experienceList);
        }

        if (dto.getQualifications() != null) {
            List<TeacherEntity.Qualification> qualificationList = dto.getQualifications().stream()
                    .map(qual -> {
                        TeacherEntity.Qualification q = new TeacherEntity.Qualification();
                        q.setDegree(qual.getDegree());
                        q.setSpecialization(qual.getSpecialization());
                        q.setUniversity(qual.getUniversity());
                        q.setCompletionYear(qual.getCompletionYear());
                        return q;
                    })
                    .collect(Collectors.toList());
            entity.setQualifications(qualificationList);
        }

        if (dto.getAdditionalAllowances() != null) {
            List<TeacherEntity.AdditionalAllowance> allowanceList = dto.getAdditionalAllowances().stream()
                    .map(all -> {
                        TeacherEntity.AdditionalAllowance aa = new TeacherEntity.AdditionalAllowance();
                        aa.setName(all.getName());
                        aa.setAmount(all.getAmount());
                        return aa;
                    })
                    .collect(Collectors.toList());
            entity.setAdditionalAllowances(allowanceList);
        }

        // Bank fields
        if (dto.getBankName() != null) entity.setBankName(dto.getBankName());
        if (dto.getAccountNumber() != null) entity.setAccountNumber(dto.getAccountNumber());
        if (dto.getIfscCode() != null) entity.setIfscCode(dto.getIfscCode());
        if (dto.getBranchName() != null) entity.setBranchName(dto.getBranchName());

        // Password - Only update if provided
        if (dto.getTeacherPassword() != null && !dto.getTeacherPassword().isEmpty()) {
            entity.setTeacherPassword(dto.getTeacherPassword());
        }

        // Status
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }
}