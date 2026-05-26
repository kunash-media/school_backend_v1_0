package com.sc.controller;

import com.sc.CustomExceptions.ResourceNotFoundException;
import com.sc.dto.request.SingleMarksEntryRequest;
import com.sc.dto.request.BulkMarksEntryRequest;
import com.sc.dto.response.StudentMarksResponse;
import com.sc.dto.response.StudentMarksSummaryResponse;
import com.sc.entity.ExamEntity.ExamType;
import com.sc.entity.StudentEntity;
import com.sc.repository.StudentRepository;
import com.sc.service.MarksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/marks")
public class MarksController {

    private static final Logger logger = LoggerFactory.getLogger(MarksController.class);

    @Autowired
    private MarksService marksService;

    @Autowired
    private StudentRepository studentRepository;

    public MarksController(MarksService marksService, StudentRepository studentRepository) {
        this.marksService = marksService;
        this.studentRepository = studentRepository;
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("timestamp", LocalDate.now().toString());
        return error;
    }

    private Map<String, Object> successResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("timestamp", LocalDate.now().toString());
        return response;
    }

    // ============= EXISTING CRUD METHODS =============

    @PostMapping("/enter")
    public ResponseEntity<?> enterMarks(@RequestBody SingleMarksEntryRequest request) {
        logger.info("POST /api/marks/enter - Entering marks for student ID: {}, exam type: {}",
                request.getStudentId(), request.getExamType());
        try {
            if (request.getStudentId() == null) {
                return ResponseEntity.badRequest().body(errorResponse("Student ID is required"));
            }
            if (request.getExamType() == null) {
                return ResponseEntity.badRequest().body(errorResponse("Exam type is required"));
            }
            if (request.getSubjects() == null || request.getSubjects().isEmpty()) {
                return ResponseEntity.badRequest().body(errorResponse("At least one subject is required"));
            }

            StudentMarksResponse response = marksService.enterMarks(request, getCurrentUser());
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse(response));
        } catch (Exception e) {
            logger.error("Error entering marks: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    @PostMapping("/bulk-enter")
    public ResponseEntity<?> enterBulkMarks(@RequestBody BulkMarksEntryRequest request) {
        logger.info("POST /api/marks/bulk-enter - Entering bulk marks for class: {} section: {}, exam type: {}",
                request.getClassName(), request.getSection(), request.getExamType());
        try {
            if (request.getExamType() == null) {
                return ResponseEntity.badRequest().body(errorResponse("Exam type is required"));
            }
            if (request.getStudents() == null || request.getStudents().isEmpty()) {
                return ResponseEntity.badRequest().body(errorResponse("At least one student is required"));
            }

            List<StudentMarksResponse> responses = marksService.enterBulkMarks(request, getCurrentUser());
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse(responses));
        } catch (Exception e) {
            logger.error("Error entering bulk marks: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getMarksByStudent(@PathVariable Long studentId) {
        logger.info("GET /api/marks/student/{} - Fetching marks for student", studentId);
        try {
            List<StudentMarksResponse> responses = marksService.getMarksByStudentId(studentId);
            return ResponseEntity.ok(successResponse(responses));
        } catch (Exception e) {
            logger.error("Error fetching marks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching marks: " + e.getMessage()));
        }
    }

    @GetMapping("/student/{studentId}/exam/{examType}")
    public ResponseEntity<?> getMarksByStudentAndExamType(
            @PathVariable Long studentId,
            @PathVariable ExamType examType,
            @RequestParam String academicYear) {
        logger.info("GET /api/marks/student/{}/exam/{} - Fetching marks", studentId, examType);
        try {
            StudentMarksResponse response = marksService.getMarksByStudentAndExamType(studentId, examType, academicYear);
            return ResponseEntity.ok(successResponse(response));
        } catch (Exception e) {
            logger.error("Error fetching marks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse(e.getMessage()));
        }
    }

    @GetMapping("/class/{className}/section/{section}/exam/{examType}")
    public ResponseEntity<?> getMarksByClassAndExamType(
            @PathVariable String className,
            @PathVariable String section,
            @PathVariable ExamType examType,
            @RequestParam String academicYear) {
        logger.info("GET /api/marks/class/{}/section/{}/exam/{} - Fetching marks", className, section, examType);
        try {
            List<StudentMarksResponse> responses = marksService.getMarksByClassAndExamType(
                    className, section, examType, academicYear);
            return ResponseEntity.ok(successResponse(responses));
        } catch (Exception e) {
            logger.error("Error fetching marks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching marks: " + e.getMessage()));
        }
    }

    @GetMapping("/student/{studentId}/summary")
    public ResponseEntity<?> getStudentMarksSummary(
            @PathVariable Long studentId,
            @RequestParam String academicYear) {
        logger.info("GET /api/marks/student/{}/summary - Fetching marks summary", studentId);
        try {
            StudentMarksSummaryResponse response = marksService.getStudentMarksSummary(studentId, academicYear);
            return ResponseEntity.ok(successResponse(response));
        } catch (Exception e) {
            logger.error("Error fetching marks summary: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching marks summary: " + e.getMessage()));
        }
    }

    @PutMapping("/update/{marksId}")
    public ResponseEntity<?> updateMarks(
            @PathVariable Long marksId,
            @RequestBody SingleMarksEntryRequest request) {
        logger.info("PUT /api/marks/update/{} - Updating marks", marksId);
        try {
            StudentMarksResponse response = marksService.updateMarks(marksId, request, getCurrentUser());
            return ResponseEntity.ok(successResponse(response));
        } catch (Exception e) {
            logger.error("Error updating marks: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{marksId}")
    public ResponseEntity<?> deleteMarks(@PathVariable Long marksId) {
        logger.info("DELETE /api/marks/delete/{} - Deleting marks", marksId);
        try {
            marksService.deleteMarks(marksId);
            return ResponseEntity.ok(successResponse("Marks deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting marks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse(e.getMessage()));
        }
    }

    @GetMapping("/check-exists")
    public ResponseEntity<?> checkMarksExist(
            @RequestParam Long studentId,
            @RequestParam ExamType examType,
            @RequestParam String academicYear) {
        logger.info("GET /api/marks/check-exists - Checking if marks exist");
        try {
            boolean exists = marksService.marksExist(studentId, examType, academicYear);
            return ResponseEntity.ok(successResponse(exists));
        } catch (Exception e) {
            logger.error("Error checking marks existence: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error checking marks existence"));
        }
    }

    @GetMapping("/exam-types")
    public ResponseEntity<?> getExamTypes() {
        logger.info("GET /api/marks/exam-types - Fetching all exam types");
        try {
            ExamType[] examTypes = ExamType.values();
            return ResponseEntity.ok(successResponse(examTypes));
        } catch (Exception e) {
            logger.error("Error fetching exam types: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching exam types"));
        }
    }

    // =========================================================================
    // 📊 REPORT GENERATION APIS - ADDED HERE
    // =========================================================================

    /**
     * 1. UNIT TEST 1 - SEPARATE REPORT
     * URL: GET /api/marks/reports/unit-test-1/{studentId}
     */
    @GetMapping("/reports/unit-test-1/{studentId}")
    public ResponseEntity<?> getUnitTest1Report(
            @PathVariable Long studentId,
            @RequestParam String academicYear) {

        logger.info("📊 Generating Unit Test 1 report for student ID: {}, academic year: {}",
                studentId, academicYear);

        try {
            StudentEntity student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

            StudentMarksResponse marks = marksService.getMarksByStudentAndExamType(
                    studentId, ExamType.UNIT_TEST, academicYear);

            Map<String, Object> report = generateUnitTestReport(marks, student, "UNIT_TEST_1", 1);

            return ResponseEntity.ok(successResponse(report));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse("Unit Test 1 marks not found for this student"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error generating report: " + e.getMessage()));
        }
    }

    /**
     * 2. UNIT TEST 2 - SEPARATE REPORT
     * URL: GET /api/marks/reports/unit-test-2/{studentId}
     */
    @GetMapping("/reports/unit-test-2/{studentId}")
    public ResponseEntity<?> getUnitTest2Report(
            @PathVariable Long studentId,
            @RequestParam String academicYear) {

        logger.info("📊 Generating Unit Test 2 report for student ID: {}, academic year: {}",
                studentId, academicYear);

        try {
            StudentEntity student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

            StudentMarksResponse marks = marksService.getMarksByStudentAndExamType(
                    studentId, ExamType.UNIT_TEST, academicYear);

            Map<String, Object> report = generateUnitTestReport(marks, student, "UNIT_TEST_2", 2);

            return ResponseEntity.ok(successResponse(report));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse("Unit Test 2 marks not found for this student"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error generating report: " + e.getMessage()));
        }
    }

    /**
     * 3. FIRST TERM - SEPARATE REPORT
     * URL: GET /api/marks/reports/term-1/{studentId}
     */
    @GetMapping("/reports/term-1/{studentId}")
    public ResponseEntity<?> getTerm1Report(
            @PathVariable Long studentId,
            @RequestParam String academicYear) {

        logger.info("📊 Generating Term 1 report for student ID: {}, academic year: {}",
                studentId, academicYear);

        try {
            StudentEntity student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

            StudentMarksResponse marks = marksService.getMarksByStudentAndExamType(
                    studentId, ExamType.TERM1, academicYear);

            // Get Unit Test marks for comparison
            List<StudentMarksResponse> unitTests = marksService.getMarksByStudentId(studentId).stream()
                    .filter(m -> m.getExamType().equals(ExamType.UNIT_TEST.name()))
                    .filter(m -> m.getAcademicYear().equals(academicYear))
                    .collect(Collectors.toList());

            Map<String, Object> report = generateTermReport(marks, student, "TERM_1", "First Term", unitTests);

            return ResponseEntity.ok(successResponse(report));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse("Term 1 marks not found for this student"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error generating report: " + e.getMessage()));
        }
    }

    /**
     * 4. COMBINED REPORT (Unit Test 1 + Unit Test 2 + First Term)
     * URL: GET /api/marks/reports/combined-first-half/{studentId}
     */
    @GetMapping("/reports/combined-first-half/{studentId}")
    public ResponseEntity<?> getCombinedFirstHalfReport(
            @PathVariable Long studentId,
            @RequestParam String academicYear) {

        logger.info("📊 Generating Combined First Half report for student ID: {}, academic year: {}",
                studentId, academicYear);

        try {
            StudentEntity student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

            // Get all marks for the student
            List<StudentMarksResponse> allMarks = marksService.getMarksByStudentId(studentId).stream()
                    .filter(m -> m.getAcademicYear().equals(academicYear))
                    .collect(Collectors.toList());

            // Filter first half exams (UT1, UT2, TERM1)
            List<StudentMarksResponse> firstHalfExams = allMarks.stream()
                    .filter(m -> m.getExamType().equals(ExamType.UNIT_TEST.name()) ||
                            m.getExamType().equals(ExamType.TERM1.name()))
                    .collect(Collectors.toList());

            Map<String, Object> report = generateCombinedReport(firstHalfExams, student, "FIRST_HALF",
                    "Unit Test 1 + Unit Test 2 + First Term");

            return ResponseEntity.ok(successResponse(report));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error generating combined report: " + e.getMessage()));
        }
    }

    /**
     * 5. UNIT TEST 3 - SEPARATE REPORT
     * URL: GET /api/marks/reports/unit-test-3/{studentId}
     */
    @GetMapping("/reports/unit-test-3/{studentId}")
    public ResponseEntity<?> getUnitTest3Report(
            @PathVariable Long studentId,
            @RequestParam String academicYear) {

        logger.info("📊 Generating Unit Test 3 report for student ID: {}, academic year: {}",
                studentId, academicYear);

        try {
            StudentEntity student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

            StudentMarksResponse marks = marksService.getMarksByStudentAndExamType(
                    studentId, ExamType.UNIT_TEST, academicYear);

            Map<String, Object> report = generateUnitTestReport(marks, student, "UNIT_TEST_3", 3);

            return ResponseEntity.ok(successResponse(report));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse("Unit Test 3 marks not found for this student"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error generating report: " + e.getMessage()));
        }
    }

    /**
     * 6. UNIT TEST 4 - SEPARATE REPORT
     * URL: GET /api/marks/reports/unit-test-4/{studentId}
     */
    @GetMapping("/reports/unit-test-4/{studentId}")
    public ResponseEntity<?> getUnitTest4Report(
            @PathVariable Long studentId,
            @RequestParam String academicYear) {

        logger.info("📊 Generating Unit Test 4 report for student ID: {}, academic year: {}",
                studentId, academicYear);

        try {
            StudentEntity student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

            StudentMarksResponse marks = marksService.getMarksByStudentAndExamType(
                    studentId, ExamType.UNIT_TEST, academicYear);

            Map<String, Object> report = generateUnitTestReport(marks, student, "UNIT_TEST_4", 4);

            return ResponseEntity.ok(successResponse(report));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse("Unit Test 4 marks not found for this student"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error generating report: " + e.getMessage()));
        }
    }

    /**
     * 7. SECOND/FINAL TERM - SEPARATE REPORT
     * URL: GET /api/marks/reports/term-2/{studentId}
     */
    @GetMapping("/reports/term-2/{studentId}")
    public ResponseEntity<?> getTerm2Report(
            @PathVariable Long studentId,
            @RequestParam String academicYear) {

        logger.info("📊 Generating Term 2 report for student ID: {}, academic year: {}",
                studentId, academicYear);

        try {
            StudentEntity student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

            StudentMarksResponse marks = marksService.getMarksByStudentAndExamType(
                    studentId, ExamType.TERM2, academicYear);

            // Get Unit Test marks for comparison
            List<StudentMarksResponse> unitTests = marksService.getMarksByStudentId(studentId).stream()
                    .filter(m -> m.getExamType().equals(ExamType.UNIT_TEST.name()))
                    .filter(m -> m.getAcademicYear().equals(academicYear))
                    .collect(Collectors.toList());

            Map<String, Object> report = generateTermReport(marks, student, "TERM_2", "Final Term", unitTests);

            return ResponseEntity.ok(successResponse(report));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse("Term 2 marks not found for this student"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error generating report: " + e.getMessage()));
        }
    }

    /**
     * 8. COMBINED REPORT (Unit Test 3 + Unit Test 4 + Second Term)
     * URL: GET /api/marks/reports/combined-second-half/{studentId}
     */
    @GetMapping("/reports/combined-second-half/{studentId}")
    public ResponseEntity<?> getCombinedSecondHalfReport(
            @PathVariable Long studentId,
            @RequestParam String academicYear) {

        logger.info("📊 Generating Combined Second Half report for student ID: {}, academic year: {}",
                studentId, academicYear);

        try {
            StudentEntity student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

            // Get all marks for the student
            List<StudentMarksResponse> allMarks = marksService.getMarksByStudentId(studentId).stream()
                    .filter(m -> m.getAcademicYear().equals(academicYear))
                    .collect(Collectors.toList());

            // Filter second half exams (UT3, UT4, TERM2)
            List<StudentMarksResponse> secondHalfExams = allMarks.stream()
                    .filter(m -> m.getExamType().equals(ExamType.UNIT_TEST.name()) ||
                            m.getExamType().equals(ExamType.TERM2.name()))
                    .collect(Collectors.toList());

            Map<String, Object> report = generateCombinedReport(secondHalfExams, student, "SECOND_HALF",
                    "Unit Test 3 + Unit Test 4 + Final Term");

            return ResponseEntity.ok(successResponse(report));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error generating combined report: " + e.getMessage()));
        }
    }

    /**
     * BONUS: ANNUAL COMBINED REPORT (All Exams)
     * URL: GET /api/marks/reports/annual/{studentId}
     */
    @GetMapping("/reports/annual/{studentId}")
    public ResponseEntity<?> getAnnualReport(
            @PathVariable Long studentId,
            @RequestParam String academicYear) {

        logger.info("📊 Generating Annual Report for student ID: {}, academic year: {}",
                studentId, academicYear);

        try {
            StudentEntity student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

            // Get all marks for the student
            List<StudentMarksResponse> allMarks = marksService.getMarksByStudentId(studentId).stream()
                    .filter(m -> m.getAcademicYear().equals(academicYear))
                    .collect(Collectors.toList());

            Map<String, Object> report = generateAnnualReport(allMarks, student);

            return ResponseEntity.ok(successResponse(report));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error generating annual report: " + e.getMessage()));
        }
    }

    // =========================================================================
    // PRIVATE REPORT GENERATION METHODS
    // =========================================================================

    /**
     * Generate Unit Test Report
     */
    private Map<String, Object> generateUnitTestReport(
            StudentMarksResponse marks,
            StudentEntity student,
            String examType,
            int testNumber) {

        Map<String, Object> report = new LinkedHashMap<>();

        // Report metadata
        report.put("reportId", "RPT-" + examType + "-" + student.getStdId() + "-" +
                LocalDate.now().getYear());
        report.put("reportType", examType);
        report.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("academicSession", testNumber <= 2 ? "First Half (Apr-Sep)" : "Second Half (Oct-Mar)");

        // Student Info
        Map<String, Object> studentInfo = new LinkedHashMap<>();
        studentInfo.put("studentId", student.getStdId());
        studentInfo.put("studentName", student.getFirstName() + " " + student.getLastName());
        studentInfo.put("rollNumber", student.getStudentRollNumber());
        studentInfo.put("className", student.getCurrentClass());
        studentInfo.put("section", student.getSection());
        studentInfo.put("fatherName", student.getFatherName());
        studentInfo.put("motherName", student.getMotherName());
        report.put("studentInfo", studentInfo);

        // Exam Details
        Map<String, Object> examDetails = new LinkedHashMap<>();
        examDetails.put("examType", examType);
        examDetails.put("examName", "Unit Test " + testNumber);
        examDetails.put("academicYear", marks.getAcademicYear());
        examDetails.put("examDate", marks.getAssessmentDate() != null ?
                marks.getAssessmentDate().toString() : "N/A");
        examDetails.put("term", testNumber <= 2 ? "First Term (Apr-Sep)" : "Second Term (Oct-Mar)");
        examDetails.put("maxMarksPerSubject", 50);
        report.put("examDetails", examDetails);

        // Subject-wise marks
        List<Map<String, Object>> subjects = new ArrayList<>();
        int totalMarks = 0;
        int totalMaxMarks = 0;

        for (StudentMarksResponse.SubjectMarkDto subject : marks.getSubjects()) {
            Map<String, Object> subMap = new LinkedHashMap<>();
            subMap.put("subjectName", subject.getSubjectName());
            subMap.put("maxMarks", subject.getMaxMarks() != null ? subject.getMaxMarks() : 50);
            subMap.put("marksObtained", subject.getMarksObtained());
            subMap.put("percentage", subject.getPercentage());
            subMap.put("grade", subject.getGrade());
            subMap.put("remarks", subject.getRemarks() != null ? subject.getRemarks() : "");
            subMap.put("status", subject.getStatus());

            subjects.add(subMap);

            totalMarks += subject.getMarksObtained();
            totalMaxMarks += (subject.getMaxMarks() != null ? subject.getMaxMarks() : 50);
        }
        report.put("subjectWiseMarks", subjects);

        // Summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalMarks", totalMarks);
        summary.put("totalMaxMarks", totalMaxMarks);
        summary.put("percentage", marks.getPercentage());
        summary.put("grade", marks.getGrade());
        summary.put("result", marks.getResult());
        report.put("summary", summary);

        // Teacher remarks
        report.put("teacherRemarks", marks.getTeacherComments() != null ?
                marks.getTeacherComments() : "Good performance. Keep it up!");

        return report;
    }

    /**
     * Generate Term Report
     */
    private Map<String, Object> generateTermReport(
            StudentMarksResponse marks,
            StudentEntity student,
            String termType,
            String termName,
            List<StudentMarksResponse> unitTests) {

        Map<String, Object> report = new LinkedHashMap<>();

        // Report metadata
        report.put("reportId", "RPT-" + termType + "-" + student.getStdId() + "-" +
                LocalDate.now().getYear());
        report.put("reportType", termType);
        report.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("academicSession", termType.equals("TERM_1") ? "First Half (Apr-Sep)" : "Second Half (Oct-Mar)");

        // Student Info
        Map<String, Object> studentInfo = new LinkedHashMap<>();
        studentInfo.put("studentId", student.getStdId());
        studentInfo.put("studentName", student.getFirstName() + " " + student.getLastName());
        studentInfo.put("rollNumber", student.getStudentRollNumber());
        studentInfo.put("className", student.getCurrentClass());
        studentInfo.put("section", student.getSection());
        report.put("studentInfo", studentInfo);

        // Term Details
        Map<String, Object> termDetails = new LinkedHashMap<>();
        termDetails.put("termName", termName);
        termDetails.put("termType", termType);
        termDetails.put("academicYear", marks.getAcademicYear());
        termDetails.put("session", termType.equals("TERM_1") ? "First Half (Apr-Sep)" : "Second Half (Oct-Mar)");
        termDetails.put("startDate", termType.equals("TERM_1") ? "2024-04-01" : "2024-10-01");
        termDetails.put("endDate", termType.equals("TERM_1") ? "2024-06-30" : "2025-02-10");
        termDetails.put("totalWorkingDays", termType.equals("TERM_1") ? 65 : 85);
        report.put("termDetails", termDetails);

        // Subject-wise marks
        List<Map<String, Object>> subjects = new ArrayList<>();
        int totalMarks = 0;
        int totalMaxMarks = 0;

        for (StudentMarksResponse.SubjectMarkDto subject : marks.getSubjects()) {
            Map<String, Object> subMap = new LinkedHashMap<>();
            subMap.put("subjectName", subject.getSubjectName());
            subMap.put("maxMarks", subject.getMaxMarks() != null ? subject.getMaxMarks() : 100);
            subMap.put("marksObtained", subject.getMarksObtained());
            subMap.put("percentage", subject.getPercentage());
            subMap.put("grade", subject.getGrade());
            subMap.put("remarks", subject.getRemarks() != null ? subject.getRemarks() : "");
            subMap.put("status", subject.getStatus());
            subjects.add(subMap);

            totalMarks += subject.getMarksObtained();
            totalMaxMarks += (subject.getMaxMarks() != null ? subject.getMaxMarks() : 100);
        }
        report.put("subjectWiseMarks", subjects);

        // Summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalMarks", totalMarks);
        summary.put("totalMaxMarks", totalMaxMarks);
        summary.put("percentage", marks.getPercentage());
        summary.put("grade", marks.getGrade());
        summary.put("result", marks.getResult());
        report.put("summary", summary);

        // Unit Tests Summary
        if (!unitTests.isEmpty()) {
            Map<String, Object> unitTestsSummary = new LinkedHashMap<>();
            for (int i = 0; i < unitTests.size(); i++) {
                StudentMarksResponse ut = unitTests.get(i);
                Map<String, Object> utMap = new LinkedHashMap<>();
                utMap.put("percentage", ut.getPercentage());
                utMap.put("grade", ut.getGrade());
                unitTestsSummary.put("unitTest" + (i+1), utMap);
            }

            double avgUnitTest = unitTests.stream()
                    .mapToDouble(StudentMarksResponse::getPercentage)
                    .average().orElse(0);
            unitTestsSummary.put("averageUnitTest", Math.round(avgUnitTest * 100.0) / 100.0);
            unitTestsSummary.put("termPercentage", marks.getPercentage());
            report.put("unitTestsSummary", unitTestsSummary);
        }

        // Teacher remarks
        report.put("teacherRemarks", marks.getTeacherComments() != null ?
                marks.getTeacherComments() : "Good performance. Keep it up!");

        return report;
    }

    /**
     * Generate Combined Report
     */
    private Map<String, Object> generateCombinedReport(
            List<StudentMarksResponse> exams,
            StudentEntity student,
            String halfType,
            String description) {

        Map<String, Object> report = new LinkedHashMap<>();

        // Report metadata
        report.put("reportId", "RPT-COMB-" + halfType + "-" + student.getStdId() + "-" +
                LocalDate.now().getYear());
        report.put("reportType", "COMBINED_" + halfType);
        report.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("academicSession", halfType.equals("FIRST_HALF") ? "First Half (Apr-Sep)" : "Second Half (Oct-Mar)");
        report.put("description", description);

        // Student Info
        Map<String, Object> studentInfo = new LinkedHashMap<>();
        studentInfo.put("studentId", student.getStdId());
        studentInfo.put("studentName", student.getFirstName() + " " + student.getLastName());
        studentInfo.put("rollNumber", student.getStudentRollNumber());
        studentInfo.put("className", student.getCurrentClass());
        studentInfo.put("section", student.getSection());
        report.put("studentInfo", studentInfo);

        report.put("academicYear", exams.isEmpty() ? "2024-2025" : exams.get(0).getAcademicYear());
        report.put("session", halfType.equals("FIRST_HALF") ? "First Half (Apr-Sep)" : "Second Half (Oct-Mar)");

        // Exams list
        List<Map<String, Object>> examsList = new ArrayList<>();
        int totalMarks = 0;
        int totalMaxMarks = 0;

        for (StudentMarksResponse exam : exams) {
            Map<String, Object> examMap = new LinkedHashMap<>();
            examMap.put("examType", exam.getExamType());
            examMap.put("examName", getExamDisplayName(exam.getExamType().name()));
            examMap.put("date", exam.getAssessmentDate() != null ? exam.getAssessmentDate().toString() : "N/A");
            examMap.put("percentage", exam.getPercentage());
            examMap.put("grade", exam.getGrade());
            examMap.put("totalMarks", exam.getTotalMarks());
            examMap.put("maxMarks", exam.getTotalMaxMarks());
            examsList.add(examMap);

            totalMarks += exam.getTotalMarks();
            totalMaxMarks += exam.getTotalMaxMarks();
        }
        report.put("exams", examsList);

        // Cumulative Summary
        Map<String, Object> cumulative = new LinkedHashMap<>();
        cumulative.put("totalExams", exams.size());
        cumulative.put("totalMarks", totalMarks);
        cumulative.put("totalMaxMarks", totalMaxMarks);
        double overallPercent = totalMaxMarks > 0 ? (totalMarks * 100.0) / totalMaxMarks : 0;
        cumulative.put("overallPercentage", Math.round(overallPercent * 100.0) / 100.0);
        cumulative.put("overallGrade", calculateGrade(overallPercent));
        report.put("cumulativeSummary", cumulative);

        // Teacher remarks
        report.put("teacherRemarks", "Good progress shown throughout the " +
                (halfType.equals("FIRST_HALF") ? "first half" : "second half"));

        return report;
    }

    /**
     * Generate Annual Report
     */
    private Map<String, Object> generateAnnualReport(List<StudentMarksResponse> allMarks, StudentEntity student) {
        Map<String, Object> report = new LinkedHashMap<>();

        report.put("reportId", "RPT-ANNUAL-" + student.getStdId() + "-" + LocalDate.now().getYear());
        report.put("reportType", "ANNUAL_COMBINED");
        report.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // Student Info
        Map<String, Object> studentInfo = new LinkedHashMap<>();
        studentInfo.put("studentId", student.getStdId());
        studentInfo.put("studentName", student.getFirstName() + " " + student.getLastName());
        studentInfo.put("rollNumber", student.getStudentRollNumber());
        studentInfo.put("className", student.getCurrentClass());
        studentInfo.put("section", student.getSection());
        report.put("studentInfo", studentInfo);

        report.put("academicYear", allMarks.isEmpty() ? "2024-2025" : allMarks.get(0).getAcademicYear());

        // Exam-wise breakdown
        List<Map<String, Object>> examBreakdown = new ArrayList<>();
        int totalMarks = 0;
        int totalMaxMarks = 0;

        for (StudentMarksResponse exam : allMarks) {
            Map<String, Object> examMap = new LinkedHashMap<>();
            examMap.put("examType", exam.getExamType());
            examMap.put("examName", getExamDisplayName(exam.getExamType().name()));
            examMap.put("percentage", exam.getPercentage());
            examMap.put("grade", exam.getGrade());
            examBreakdown.add(examMap);

            totalMarks += exam.getTotalMarks();
            totalMaxMarks += exam.getTotalMaxMarks();
        }
        report.put("examWiseBreakdown", examBreakdown);

        // Annual Summary
        Map<String, Object> annualSummary = new LinkedHashMap<>();
        annualSummary.put("totalExams", allMarks.size());
        annualSummary.put("totalMarks", totalMarks);
        annualSummary.put("totalMaxMarks", totalMaxMarks);
        double annualPercent = totalMaxMarks > 0 ? (totalMarks * 100.0) / totalMaxMarks : 0;
        annualSummary.put("annualPercentage", Math.round(annualPercent * 100.0) / 100.0);
        annualSummary.put("annualGrade", calculateGrade(annualPercent));
        report.put("annualSummary", annualSummary);

        // Principal remarks
        report.put("principalRemarks", "Excellent academic year. Well done!");

        return report;
    }

    /**
     * Get display name for exam type
     */
    private String getExamDisplayName(String examType) {
        switch (examType) {
            case "UNIT_TEST": return "Unit Test";
            case "TERM1": return "First Term Examination";
            case "TERM2": return "Final Term Examination";
            default: return examType;
        }
    }

    /**
     * Calculate grade based on percentage
     */
    private String calculateGrade(double percentage) {
        if (percentage >= 90) return "A";
        if (percentage >= 75) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 40) return "D";
        return "F";
    }
}