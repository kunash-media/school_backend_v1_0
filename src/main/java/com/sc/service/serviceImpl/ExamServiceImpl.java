
package com.sc.service.serviceImpl;

import com.sc.CustomExceptions.ResourceNotFoundException;
import com.sc.dto.request.ExamCreateRequest;
import com.sc.dto.response.ExamResponse;
import com.sc.entity.ClassEntity;
import com.sc.entity.ExamEntity;
import com.sc.entity.StudentEntity;
import com.sc.entity.NotificationEntity;  // ✅ ADD THIS IMPORT
import com.sc.repository.ClassRepository;
import com.sc.repository.ExamRepository;
import com.sc.repository.StudentRepository;
import com.sc.repository.NotificationRepository;  // ✅ ADD THIS IMPORT
import com.sc.service.ExamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;  // ✅ ADD THIS IMPORT
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {

    private static final Logger logger = LoggerFactory.getLogger(ExamServiceImpl.class);

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private StudentRepository studentRepository;

    // ✅ USE DIRECT REPOSITORY - NO SERVICE NEEDED
    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    @Transactional
    public ExamResponse createExam(ExamCreateRequest request, String createdBy) {
        logger.info("Creating new exam: {}", request.getExamName());

        // Manual validation
        if (request.getExamName() == null || request.getExamName().trim().isEmpty()) {
            throw new RuntimeException("Exam name is required");
        }
        if (request.getExamType() == null) {
            throw new RuntimeException("Exam type is required");
        }
        if (request.getAcademicYear() == null || request.getAcademicYear().trim().isEmpty()) {
            throw new RuntimeException("Academic year is required");
        }
        if (request.getClassId() == null) {
            throw new RuntimeException("Class ID is required");
        }
        if (request.getSection() == null || request.getSection().trim().isEmpty()) {
            throw new RuntimeException("Section is required");
        }
        if (request.getSubjects() == null || request.getSubjects().isEmpty()) {
            throw new RuntimeException("At least one subject is required");
        }

        // Validate class
        ClassEntity classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + request.getClassId()));

        // Generate exam code if not provided
        String examCode = request.getExamCode();
        if (examCode == null || examCode.isEmpty()) {
            examCode = generateExamCode(request);
        }

        // Check if exam code exists
        if (examRepository.existsByExamCode(examCode)) {
            throw new RuntimeException("Exam code already exists: " + examCode);
        }

        // Create exam
        ExamEntity exam = new ExamEntity();
        exam.setExamName(request.getExamName());
        exam.setExamCode(examCode);
        exam.setExamType(request.getExamType());
        exam.setAcademicYear(request.getAcademicYear());
        exam.setClassEntity(classEntity);
        exam.setSection(request.getSection());
        exam.setStartDate(request.getStartDate());
        exam.setEndDate(request.getEndDate());
        exam.setDescription(request.getDescription());
        exam.setStatus("SCHEDULED");
        exam.setCreatedBy(createdBy);

        // Add subjects
        for (ExamEntity.SubjectDetail subject : request.getSubjects()) {
            exam.addSubject(subject);
        }

        // Save exam
        ExamEntity savedExam = examRepository.save(exam);
        logger.info("Exam created successfully with ID: {}", savedExam.getExamId());

        // ✅ SEND NOTIFICATIONS USING DIRECT REPOSITORY
        sendExamNotifications(savedExam);

        return new ExamResponse(savedExam);
    }

    /**
     * ✅ Send exam notifications using DIRECT REPOSITORY (no service needed)
     */
    private void sendExamNotifications(ExamEntity exam) {
        try {
            logger.info("📨 Sending exam notifications for exam ID: {}", exam.getExamId());

            // Get all students from this class and section
            List<StudentEntity> students = studentRepository.findByCurrentClassAndSection(
                    exam.getClassEntity().getClassName(),
                    exam.getSection()
            );

            if (students == null || students.isEmpty()) {
                logger.info("No students found in class {} section {}",
                        exam.getClassEntity().getClassName(), exam.getSection());
                return;
            }

            // Format dates for better readability
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            String startDateStr = exam.getStartDate() != null ? exam.getStartDate().format(formatter) : "TBA";
            String endDateStr = exam.getEndDate() != null ? exam.getEndDate().format(formatter) : "TBA";

            // Get exam type display name
            String examTypeDisplay = getExamTypeDisplay(exam.getExamType());

            // Build subjects list for message
            StringBuilder subjectsList = new StringBuilder();
            if (exam.getSubjects() != null && !exam.getSubjects().isEmpty()) {
                int count = 0;
                for (ExamEntity.SubjectDetail subject : exam.getSubjects()) {
                    if (count++ < 3) { // Show first 3 subjects
                        if (subjectsList.length() > 0) subjectsList.append(", ");
                        subjectsList.append(subject.getSubjectName());
                    }
                }
                if (exam.getSubjects().size() > 3) {
                    subjectsList.append(" and ").append(exam.getSubjects().size() - 3).append(" more");
                }
            }

            // Prepare notification message
            String title = "📅 New Exam Scheduled: " + exam.getExamName();
            String message = String.format(
                    "A new %s has been scheduled for your class.\n\n" +
                            "📋 Exam: %s\n" +
                            "🏫 Class: %s %s\n" +
                            "📅 Dates: %s to %s\n" +
                            "📚 Subjects: %s\n\n" +
                            "Please check the exam schedule for complete details.",
                    examTypeDisplay,
                    exam.getExamName(),
                    exam.getClassEntity().getClassName(),
                    exam.getSection(),
                    startDateStr,
                    endDateStr,
                    subjectsList.toString()
            );

            // ✅ SEND NOTIFICATION TO EACH STUDENT USING DIRECT REPOSITORY
            int notificationCount = 0;
            for (StudentEntity student : students) {
                try {
                    // Create new notification entity
                    NotificationEntity notification = new NotificationEntity();
                    notification.setStudent(student);
                    notification.setTitle(title);
                    notification.setMessage(message);
                    notification.setNotificationType("EXAM_SCHEDULED");
                    notification.setChannel("IN_APP");
                    notification.setStatus("SENT");
                    notification.setPriority("MEDIUM");
                    notification.setSentDate(new Date());

                    // Save directly using repository
                    notificationRepository.save(notification);
                    notificationCount++;

                } catch (Exception e) {
                    logger.error("Failed to send notification to student ID: {}", student.getStdId(), e);
                }
            }

            logger.info("✅ Exam notifications sent to {} students", notificationCount);

        } catch (Exception e) {
            logger.error("❌ Error sending exam notifications", e);
        }
    }

    /**
     * Get display name for exam type
     */
    private String getExamTypeDisplay(ExamEntity.ExamType examType) {
        if (examType == null) return "Exam";

        switch (examType) {
            case TERM1: return "Term 1 Examination";
            case TERM2: return "Term 2 Examination";
            case TERM3: return "Term 3 Examination";
            case UNIT_TEST: return "Unit Test";
            case MID_TERM: return "Mid Term Examination";
            case FINAL: return "Final Examination";
            default: return examType.name();
        }
    }

    private String generateExamCode(ExamCreateRequest request) {
        String prefix = request.getExamType() != null ?
                request.getExamType().name().substring(0, 3) : "EXM";
        String year = request.getAcademicYear() != null ?
                request.getAcademicYear().substring(2, 4) : "24";
        String classStr = request.getClassId() != null ?
                request.getClassId().toString() : "0";

        return prefix + year + classStr + System.currentTimeMillis() % 1000;
    }

    @Override
    public ExamResponse getExamById(Long examId) {
        ExamEntity exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + examId));
        return new ExamResponse(exam);
    }

    @Override
    public ExamResponse getExamByCode(String examCode) {
        ExamEntity exam = examRepository.findByExamCode(examCode)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with code: " + examCode));
        return new ExamResponse(exam);
    }

    @Override
    public List<ExamResponse> getAllExams() {
        return examRepository.findAll().stream()
                .map(ExamResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExamResponse> getExamsByClass(Long classId) {
        return examRepository.findByClassEntity_ClassId(classId).stream()
                .map(ExamResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExamResponse> getExamsByClassAndSection(Long classId, String section) {
        return examRepository.findByClassEntity_ClassIdAndSection(classId, section).stream()
                .map(ExamResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExamResponse> getExamsByAcademicYear(String academicYear) {
        return examRepository.findByAcademicYear(academicYear).stream()
                .map(ExamResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExamResponse> getExamsByStatus(String status) {
        return examRepository.findByStatus(status).stream()
                .map(ExamResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExamResponse> getUpcomingExams() {
        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusMonths(1);
        return examRepository.findByDateRange(today, nextMonth).stream()
                .map(ExamResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExamResponse updateExam(Long examId, ExamCreateRequest request, String updatedBy) {
        ExamEntity exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + examId));

        // Update exam details
        if (request.getExamName() != null) exam.setExamName(request.getExamName());
        if (request.getExamType() != null) exam.setExamType(request.getExamType());
        if (request.getAcademicYear() != null) exam.setAcademicYear(request.getAcademicYear());
        if (request.getSection() != null) exam.setSection(request.getSection());
        if (request.getStartDate() != null) exam.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) exam.setEndDate(request.getEndDate());
        if (request.getDescription() != null) exam.setDescription(request.getDescription());

        // Update subjects if provided
        if (request.getSubjects() != null && !request.getSubjects().isEmpty()) {
            exam.setSubjects(request.getSubjects());
        }

        ExamEntity updatedExam = examRepository.save(exam);
        logger.info("Exam updated successfully with ID: {}", examId);

        return new ExamResponse(updatedExam);
    }

    @Override
    @Transactional
    public ExamResponse updateExamStatus(Long examId, String status) {
        ExamEntity exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + examId));

        exam.setStatus(status);
        ExamEntity updatedExam = examRepository.save(exam);
        logger.info("Exam status updated to {} for ID: {}", status, examId);

        return new ExamResponse(updatedExam);
    }

    @Override
    @Transactional
    public void deleteExam(Long examId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Exam not found with ID: " + examId);
        }
        examRepository.deleteById(examId);
        logger.info("Exam deleted with ID: {}", examId);
    }

    @Override
    public boolean isExamCodeExists(String examCode) {
        return examRepository.existsByExamCode(examCode);
    }
}