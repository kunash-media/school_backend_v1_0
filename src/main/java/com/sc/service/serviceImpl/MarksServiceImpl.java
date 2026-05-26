package com.sc.service.serviceImpl;

import com.sc.CustomExceptions.ResourceNotFoundException;
import com.sc.dto.request.SingleMarksEntryRequest;
import com.sc.dto.request.BulkMarksEntryRequest;
import com.sc.dto.response.StudentMarksResponse;
import com.sc.dto.response.StudentMarksSummaryResponse;
import com.sc.entity.ExamEntity;
import com.sc.entity.ExamMarksEntity;
import com.sc.entity.StudentEntity;
import com.sc.entity.ExamEntity.ExamType;
import com.sc.repository.ExamMarksRepository;
import com.sc.repository.StudentRepository;
import com.sc.service.MarksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MarksServiceImpl implements MarksService {

    private static final Logger logger = LoggerFactory.getLogger(MarksServiceImpl.class);

    @Autowired
    private ExamMarksRepository marksRepository;

    @Autowired
    private StudentRepository studentRepository;

    // ============= ENTER SINGLE STUDENT MARKS =============

    @Override
    @Transactional
    public StudentMarksResponse enterMarks(SingleMarksEntryRequest request, String enteredBy) {
        logger.info("📝 Entering marks for student ID: {}, exam type: {}",
                request.getStudentId(), request.getExamType());

        // Manual validation
        validateSingleMarksRequest(request);

        // Get student
        StudentEntity student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        // Check if marks already exist - Now passing ExamType directly, not converting to String
        if (marksRepository.existsByStudent_StdIdAndExamTypeAndAcademicYear(
                request.getStudentId(), request.getExamType(), request.getAcademicYear())) {
            throw new RuntimeException("Marks already exist for this student, exam type and academic year");
        }

        // Create marks entity
        ExamMarksEntity marks = new ExamMarksEntity();
        marks.setStudent(student);
        marks.setExamType(request.getExamType());
        marks.setExamName(request.getExamName() != null ? request.getExamName() : request.getExamType().toString());
        marks.setAcademicYear(request.getAcademicYear() != null ? request.getAcademicYear() : getCurrentAcademicYear());
        marks.setAssessmentDate(request.getAssessmentDate() != null ? request.getAssessmentDate() : LocalDate.now());
        marks.setTeacherComments(request.getTeacherComments());
        marks.setCreatedBy(enteredBy);

        // Process subjects
        List<ExamMarksEntity.SubjectMark> subjectMarks = processSubjectMarks(request.getSubjects());
        marks.setSubjects(subjectMarks);

        // Calculate totals
        calculateTotals(marks);

        // Save to database
        ExamMarksEntity savedMarks = marksRepository.save(marks);
        logger.info("✅ Marks saved successfully with ID: {}", savedMarks.getMarksId());

        return new StudentMarksResponse(savedMarks);
    }

    // ============= ENTER BULK MARKS =============

    @Override
    @Transactional
    public List<StudentMarksResponse> enterBulkMarks(BulkMarksEntryRequest request, String enteredBy) {
        logger.info("📦 Entering bulk marks for class: {} section: {}, exam type: {}",
                request.getClassName(), request.getSection(), request.getExamType());

        // Manual validation
        if (request.getExamType() == null) {
            throw new RuntimeException("Exam type is required");
        }
        if (request.getStudents() == null || request.getStudents().isEmpty()) {
            throw new RuntimeException("At least one student is required");
        }

        List<StudentMarksResponse> responses = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (BulkMarksEntryRequest.StudentMarksDto studentDto : request.getStudents()) {
            try {
                // Convert bulk DTO to single request
                SingleMarksEntryRequest singleRequest = convertToSingleRequest(studentDto, request);

                // Enter marks
                StudentMarksResponse response = enterMarks(singleRequest, enteredBy);
                responses.add(response);
                successCount++;

            } catch (Exception e) {
                failCount++;
                logger.error("❌ Failed to enter marks for student ID: {} - {}",
                        studentDto.getStudentId(), e.getMessage());
            }
        }

        logger.info("✅ Bulk marks entry completed - Success: {}, Failed: {}", successCount, failCount);
        return responses;
    }

    // ============= GET MARKS BY STUDENT ID =============

    @Override
    public List<StudentMarksResponse> getMarksByStudentId(Long studentId) {
        logger.info("🔍 Fetching all marks for student ID: {}", studentId);

        List<ExamMarksEntity> marksList = marksRepository.findByStudent_StdId(studentId);

        if (marksList.isEmpty()) {
            logger.info("No marks found for student ID: {}", studentId);
            return new ArrayList<>();
        }

        return marksList.stream()
                .map(StudentMarksResponse::new)
                .collect(Collectors.toList());
    }

    // ============= GET MARKS BY STUDENT AND EXAM TYPE =============

    @Override
    public StudentMarksResponse getMarksByStudentAndExamType(Long studentId, ExamType examType, String academicYear) {
        logger.info("🔍 Fetching marks for student ID: {}, exam type: {}, academic year: {}",
                studentId, examType, academicYear);

        // Now passing ExamType directly, not converting to String
        ExamMarksEntity marks = marksRepository
                .findByStudent_StdIdAndExamTypeAndAcademicYear(studentId, examType, academicYear)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Marks not found for student ID: " + studentId + ", exam type: " + examType));

        return new StudentMarksResponse(marks);
    }

    // ============= GET MARKS BY CLASS AND EXAM TYPE =============

    @Override
    public List<StudentMarksResponse> getMarksByClassAndExamType(String className, String section, ExamType examType, String academicYear) {
        logger.info("🔍 Fetching marks for class: {} section: {}, exam type: {}, academic year: {}",
                className, section, examType, academicYear);

        // Now passing ExamType directly, not converting to String
        List<ExamMarksEntity> marksList = marksRepository.findByClassAndSectionAndExamType(className, section, examType, academicYear);

        if (marksList.isEmpty()) {
            logger.info("No marks found for class: {} section: {}", className, section);
            return new ArrayList<>();
        }

        return marksList.stream()
                .map(StudentMarksResponse::new)
                .collect(Collectors.toList());
    }

    // ============= GET STUDENT MARKS SUMMARY =============

    @Override
    public StudentMarksSummaryResponse getStudentMarksSummary(Long studentId, String academicYear) {
        logger.info("📊 Generating marks summary for student ID: {}, academic year: {}", studentId, academicYear);

        // Get student
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        // Get all marks for student in given academic year
        List<ExamMarksEntity> allMarks = marksRepository.findByStudent_StdId(studentId).stream()
                .filter(m -> m.getAcademicYear().equals(academicYear))
                .collect(Collectors.toList());

        // Create summary response
        StudentMarksSummaryResponse summary = new StudentMarksSummaryResponse();
        summary.setStudentId(studentId);
        summary.setStudentName(student.getFirstName() + " " + student.getLastName());
        summary.setRollNumber(student.getStudentRollNumber());
        summary.setClassName(student.getCurrentClass());
        summary.setSection(student.getSection());
        summary.setAcademicYear(academicYear);

        // Process each exam
        List<StudentMarksSummaryResponse.ExamSummaryDto> exams = new ArrayList<>();
        int totalMarks = 0;
        int totalMaxMarks = 0;

        for (ExamMarksEntity marks : allMarks) {
            StudentMarksSummaryResponse.ExamSummaryDto exam = new StudentMarksSummaryResponse.ExamSummaryDto();
            exam.setExamType(marks.getExamType() != null ? marks.getExamType().name() : null);
            exam.setExamName(marks.getExamName());
            exam.setTotalMarks(marks.getTotalMarks());
            exam.setTotalMaxMarks(marks.getTotalMaxMarks());
            exam.setPercentage(marks.getPercentage());
            exam.setGrade(marks.getGrade());
            exam.setResult(marks.getResult());
            exams.add(exam);

            totalMarks += marks.getTotalMarks() != null ? marks.getTotalMarks() : 0;
            totalMaxMarks += marks.getTotalMaxMarks() != null ? marks.getTotalMaxMarks() : 0;
        }

        summary.setExams(exams);

        // Calculate overall summary
        StudentMarksSummaryResponse.OverallSummaryDto overall = new StudentMarksSummaryResponse.OverallSummaryDto();
        overall.setTotalExams(exams.size());
        overall.setTotalMarks(totalMarks);
        overall.setTotalMaxMarks(totalMaxMarks);
        overall.setOverallPercentage(totalMaxMarks > 0 ? (totalMarks * 100.0) / totalMaxMarks : 0);
        overall.setOverallGrade(calculateGrade(overall.getOverallPercentage()));
        summary.setOverall(overall);

        logger.info("✅ Marks summary generated for {} exams", exams.size());
        return summary;
    }

    // ============= UPDATE MARKS =============

    @Override
    @Transactional
    public StudentMarksResponse updateMarks(Long marksId, SingleMarksEntryRequest request, String updatedBy) {
        logger.info("✏️ Updating marks with ID: {}", marksId);

        // Get existing marks
        ExamMarksEntity marks = marksRepository.findById(marksId)
                .orElseThrow(() -> new ResourceNotFoundException("Marks not found with ID: " + marksId));

        // Update fields if provided
        if (request.getExamType() != null) {
            marks.setExamType(request.getExamType());
        }
        if (request.getExamName() != null) {
            marks.setExamName(request.getExamName());
        }
        if (request.getAcademicYear() != null) {
            marks.setAcademicYear(request.getAcademicYear());
        }
        if (request.getAssessmentDate() != null) {
            marks.setAssessmentDate(request.getAssessmentDate());
        }
        if (request.getTeacherComments() != null) {
            marks.setTeacherComments(request.getTeacherComments());
        }

        // Update subjects if provided
        if (request.getSubjects() != null && !request.getSubjects().isEmpty()) {
            List<ExamMarksEntity.SubjectMark> subjectMarks = processSubjectMarks(request.getSubjects());
            marks.setSubjects(subjectMarks);
            calculateTotals(marks);
        }

        marks.setUpdatedBy(updatedBy);
        marks.setUpdatedAt(LocalDateTime.now());

        // Save updated marks
        ExamMarksEntity updatedMarks = marksRepository.save(marks);
        logger.info("✅ Marks updated successfully with ID: {}", marksId);

        return new StudentMarksResponse(updatedMarks);
    }

    // ============= DELETE MARKS =============

    @Override
    @Transactional
    public void deleteMarks(Long marksId) {
        logger.info("🗑️ Deleting marks with ID: {}", marksId);

        if (!marksRepository.existsById(marksId)) {
            throw new ResourceNotFoundException("Marks not found with ID: " + marksId);
        }

        marksRepository.deleteById(marksId);
        logger.info("✅ Marks deleted successfully with ID: {}", marksId);
    }

    // ============= CHECK MARKS EXIST =============

    @Override
    public boolean marksExist(Long studentId, ExamType examType, String academicYear) {
        // Now passing ExamType directly, not converting to String
        return marksRepository.existsByStudent_StdIdAndExamTypeAndAcademicYear(studentId, examType, academicYear);
    }

    // ============= PRIVATE HELPER METHODS =============

    /**
     * Validate single marks entry request
     */
    private void validateSingleMarksRequest(SingleMarksEntryRequest request) {
        if (request.getStudentId() == null) {
            throw new RuntimeException("Student ID is required");
        }
        if (request.getExamType() == null) {
            throw new RuntimeException("Exam type is required");
        }
        if (request.getSubjects() == null || request.getSubjects().isEmpty()) {
            throw new RuntimeException("At least one subject is required");
        }

        // Validate each subject
        for (int i = 0; i < request.getSubjects().size(); i++) {
            SingleMarksEntryRequest.SubjectMarksDto subject = request.getSubjects().get(i);
            if (subject.getSubjectName() == null || subject.getSubjectName().trim().isEmpty()) {
                throw new RuntimeException("Subject name is required for subject at index " + i);
            }
            if (subject.getMarksObtained() == null) {
                throw new RuntimeException("Marks obtained is required for subject: " + subject.getSubjectName());
            }
            if (subject.getMarksObtained() < 0) {
                throw new RuntimeException("Marks obtained cannot be negative for subject: " + subject.getSubjectName());
            }
            if (subject.getMaxMarks() != null && subject.getMarksObtained() > subject.getMaxMarks()) {
                throw new RuntimeException("Marks obtained cannot exceed maximum marks for subject: " + subject.getSubjectName());
            }
        }
    }

    /**
     * Process subject marks and create SubjectMark list
     */
    private List<ExamMarksEntity.SubjectMark> processSubjectMarks(
            List<SingleMarksEntryRequest.SubjectMarksDto> subjectDtos) {

        List<ExamMarksEntity.SubjectMark> subjectMarks = new ArrayList<>();

        for (SingleMarksEntryRequest.SubjectMarksDto subjectDto : subjectDtos) {
            int maxMarks = subjectDto.getMaxMarks() != null ? subjectDto.getMaxMarks() : 100;

            ExamMarksEntity.SubjectMark subject = new ExamMarksEntity.SubjectMark(
                    subjectDto.getSubjectName(),
                    subjectDto.getMarksObtained(),
                    maxMarks
            );
            subject.setRemarks(subjectDto.getRemarks());
            subject.setPerformance(subjectDto.getPerformance());

            subjectMarks.add(subject);
        }

        return subjectMarks;
    }

    /**
     * Calculate total marks, percentage, grade and result
     */
    private void calculateTotals(ExamMarksEntity marks) {
        int totalMarks = 0;
        int totalMaxMarks = 0;

        for (ExamMarksEntity.SubjectMark subject : marks.getSubjects()) {
            totalMarks += subject.getMarksObtained();
            totalMaxMarks += subject.getMaxMarks();
        }

        marks.setTotalMarks(totalMarks);
        marks.setTotalMaxMarks(totalMaxMarks);

        double percentage = totalMaxMarks > 0 ? (totalMarks * 100.0) / totalMaxMarks : 0;
        marks.setPercentage(Math.round(percentage * 100.0) / 100.0); // Round to 2 decimal places
        marks.setGrade(calculateGrade(marks.getPercentage()));
        marks.setResult(marks.getPercentage() >= 33 ? "PASS" : "FAIL");
    }

    /**
     * Calculate grade based on percentage
     */
    private String calculateGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 75) return "A";
        if (percentage >= 60) return "B";
        if (percentage >= 45) return "C";
        if (percentage >= 33) return "D";
        return "F";
    }

    /**
     * Get current academic year (e.g., "2024-2025")
     */
    private String getCurrentAcademicYear() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        if (month >= 4) { // April onwards
            return year + "-" + (year + 1);
        } else { // January to March
            return (year - 1) + "-" + year;
        }
    }

    /**
     * Convert bulk DTO to single request
     */
    private SingleMarksEntryRequest convertToSingleRequest(
            BulkMarksEntryRequest.StudentMarksDto studentDto,
            BulkMarksEntryRequest bulkRequest) {

        SingleMarksEntryRequest singleRequest = new SingleMarksEntryRequest();
        singleRequest.setStudentId(studentDto.getStudentId());
        singleRequest.setExamType(bulkRequest.getExamType());
        singleRequest.setExamName(bulkRequest.getExamName());
        singleRequest.setAcademicYear(bulkRequest.getAcademicYear());
        singleRequest.setAssessmentDate(bulkRequest.getAssessmentDate());
        singleRequest.setTeacherComments(null);

        List<SingleMarksEntryRequest.SubjectMarksDto> subjects = new ArrayList<>();
        if (studentDto.getSubjects() != null) {
            for (BulkMarksEntryRequest.SubjectMarksDto subjectDto : studentDto.getSubjects()) {
                SingleMarksEntryRequest.SubjectMarksDto sub = new SingleMarksEntryRequest.SubjectMarksDto();
                sub.setSubjectName(subjectDto.getSubjectName());
                sub.setMarksObtained(subjectDto.getMarksObtained());
                sub.setMaxMarks(subjectDto.getMaxMarks());
                sub.setRemarks(subjectDto.getRemarks());
                sub.setPerformance(subjectDto.getPerformance());
                subjects.add(sub);
            }
        }
        singleRequest.setSubjects(subjects);

        return singleRequest;
    }
}