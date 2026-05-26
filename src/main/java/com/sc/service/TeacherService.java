package com.sc.service;

import com.sc.dto.request.TeacherRequestDto;
import com.sc.dto.response.TeacherResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface TeacherService {

    // Create
    TeacherResponseDto createTeacher(TeacherRequestDto requestDto, Map<String, MultipartFile> files, String createdBy);

    // Read - Updated parameter names
    TeacherResponseDto getTeacherById(Long id);
    TeacherResponseDto getTeacherByTeacherCode(String teacherCode);  // Changed from getTeacherByTeacherId
    TeacherResponseDto getTeacherByEmployeeId(String employeeId);
    List<TeacherResponseDto> getAllTeachers();
    List<TeacherResponseDto> getActiveTeachers();
    List<TeacherResponseDto> getTeachersByStatus(String status);
    List<TeacherResponseDto> getTeachersByDepartment(String department);
    List<TeacherResponseDto> searchTeachersByName(String name);

    // Update - Updated parameter names
    TeacherResponseDto updateTeacher(Long id, TeacherRequestDto requestDto);
    TeacherResponseDto updateTeacherWithFiles(Long id, TeacherRequestDto requestDto, Map<String, MultipartFile> files);
    TeacherResponseDto updateTeacherStatus(String teacherCode, String status);  // Changed parameter name
    TeacherResponseDto updateTeacherPassword(String teacherCode, String newPassword, String confirmPassword);  // Changed parameter name

    // Document Operations - Updated parameter names
    TeacherResponseDto uploadDocuments(String teacherCode, Map<String, String> fileNames);  // Changed parameter name
    TeacherResponseDto updateSingleDocument(String teacherCode, String documentType, MultipartFile file);  // Changed parameter name

    // Delete - Updated parameter names
    void deleteTeacher(Long id);
    void softDeleteTeacher(String teacherCode);  // Changed parameter name

    // Validation & Business Logic
    boolean isEmailExists(String email);
    boolean isContactNumberExists(String contactNumber);
    boolean isAadharExists(String aadharNumber);
    boolean isPanExists(String panNumber);
    boolean isEmployeeIdExists(String employeeId);
    boolean isTeacherCodeExists(String teacherCode);  // NEW method

    // Utility
    String generateTeacherCode();  // Changed from generateTeacherId
    long getTeacherCount();
    long getActiveTeacherCount();
    List<String> getAllDepartments();

    // ========== ADD THESE TWO METHODS HERE ==========
    // Authentication methods
    boolean authenticateTeacher(String employeeId, String rawPassword);
    boolean authenticateTeacherByCode(String teacherCode, String rawPassword);
    // Add to TeacherService.java interface (around line 55, after searchTeachersByName)

    // Get classes assigned to teacher
    List<?> getTeacherClasses(Long teacherId);
}