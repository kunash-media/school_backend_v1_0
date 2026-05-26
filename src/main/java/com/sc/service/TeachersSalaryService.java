package com.sc.service;

import com.sc.dto.request.TeachersSalaryRequestDto;
import com.sc.dto.response.TeachersSalaryResponseDto;

import java.time.YearMonth;
import java.util.List;

public interface TeachersSalaryService {

    // Generate salary for a teacher
    TeachersSalaryResponseDto generateSalary(TeachersSalaryRequestDto requestDto);

    // Calculate and generate salary based on attendance
    TeachersSalaryResponseDto calculateAndGenerateSalary(Long teacherId, YearMonth salaryMonth, String generatedBy);

    // Get salary by ID
    TeachersSalaryResponseDto getSalaryById(Long id);

    // Update salary
    TeachersSalaryResponseDto updateSalary(Long id, TeachersSalaryRequestDto requestDto);

    // Delete salary
    void deleteSalary(Long id);

    // Get all salary records
    List<TeachersSalaryResponseDto> getAllSalaries();

    // Get salary by teacher ID
    List<TeachersSalaryResponseDto> getSalariesByTeacherId(Long teacherId);

    // Get salary by month
    List<TeachersSalaryResponseDto> getSalariesByMonth(YearMonth salaryMonth);

    // Get salary by status
    List<TeachersSalaryResponseDto> getSalariesByStatus(String status);

    // Approve salary
    TeachersSalaryResponseDto approveSalary(Long id, String approvedBy);

    // Mark as paid
    TeachersSalaryResponseDto markAsPaid(Long id, String paymentMethod, String transactionId);

    // Get salary statistics for a month
    Object getSalaryStatistics(YearMonth salaryMonth);

    // Get salary summary for a teacher
    Object getSalarySummaryByTeacher(Long teacherId);

    // Get current month salary for a teacher
    TeachersSalaryResponseDto getCurrentMonthSalary(Long teacherId);
}