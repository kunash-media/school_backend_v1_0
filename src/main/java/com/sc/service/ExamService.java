package com.sc.service;

import com.sc.dto.request.ExamCreateRequest;
import com.sc.dto.response.ExamResponse;
import java.util.List;

public interface ExamService {

    ExamResponse createExam(ExamCreateRequest request, String createdBy);

    ExamResponse getExamById(Long examId);

    ExamResponse getExamByCode(String examCode);

    List<ExamResponse> getAllExams();

    List<ExamResponse> getExamsByClass(Long classId);

    List<ExamResponse> getExamsByClassAndSection(Long classId, String section);

    List<ExamResponse> getExamsByAcademicYear(String academicYear);

    List<ExamResponse> getExamsByStatus(String status);

    List<ExamResponse> getUpcomingExams();

    ExamResponse updateExam(Long examId, ExamCreateRequest request, String updatedBy);

    ExamResponse updateExamStatus(Long examId, String status);

    void deleteExam(Long examId);

    boolean isExamCodeExists(String examCode);


}