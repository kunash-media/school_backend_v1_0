package com.sc.service;

import com.sc.dto.request.SingleMarksEntryRequest;
import com.sc.dto.request.BulkMarksEntryRequest;
import com.sc.dto.response.StudentMarksResponse;
import com.sc.dto.response.StudentMarksSummaryResponse;
import com.sc.entity.ExamEntity.ExamType;

import java.util.List;

public interface MarksService {

    /**
     * Enter marks for a single student
     */
    StudentMarksResponse enterMarks(SingleMarksEntryRequest request, String enteredBy);

    /**
     * Enter marks for multiple students in bulk
     */
    List<StudentMarksResponse> enterBulkMarks(BulkMarksEntryRequest request, String enteredBy);

    /**
     * Get all marks for a student
     */
    List<StudentMarksResponse> getMarksByStudentId(Long studentId);

    /**
     * Get marks for a student by exam type and academic year
     */
    StudentMarksResponse getMarksByStudentAndExamType(Long studentId, ExamType examType, String academicYear);

    /**
     * Get marks for a class by exam type and academic year
     */
    List<StudentMarksResponse> getMarksByClassAndExamType(String className, String section, ExamType examType, String academicYear);

    /**
     * Get complete marks summary for a student (all exams)
     */
    StudentMarksSummaryResponse getStudentMarksSummary(Long studentId, String academicYear);

    /**
     * Update existing marks
     */
    StudentMarksResponse updateMarks(Long marksId, SingleMarksEntryRequest request, String updatedBy);

    /**
     * Delete marks by ID
     */
    void deleteMarks(Long marksId);

    /**
     * Check if marks already exist for student, exam type and academic year
     */
    boolean marksExist(Long studentId, ExamType examType, String academicYear);
}