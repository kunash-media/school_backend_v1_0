package com.sc.service;

import com.sc.dto.request.ClassCreateRequestDTO;
import com.sc.dto.response.ClassResponseDTO;
import com.sc.dto.request.TeacherSubjectAssignmentDTO;
import com.sc.dto.response.ClassSubjectsResponseDTO;

import java.util.List;

public interface ClassService {

    // Create operations
    ClassResponseDTO createClass(ClassCreateRequestDTO request);
    ClassResponseDTO createClassBasic(ClassCreateRequestDTO request);

    // Read operations
    ClassResponseDTO getClassById(Long classId);
    ClassResponseDTO getClassByCode(String classCode);
    List<ClassResponseDTO> getAllClasses();
    List<ClassResponseDTO> getClassesByAcademicYear(String academicYear);
    List<ClassResponseDTO> getClassesByClassName(String className);
    List<ClassResponseDTO> getClassesByTeacher(Long teacherId);

    // Update operations
    ClassResponseDTO updateClass(Long classId, ClassCreateRequestDTO request);
    ClassResponseDTO updateClassTeacher(Long classId, Long teacherId, String subject);
    ClassResponseDTO updateAssistantTeacher(Long classId, Long teacherId, String subject);
    ClassResponseDTO addOtherTeacherSubject(Long classId, TeacherSubjectAssignmentDTO assignment);
    ClassResponseDTO removeOtherTeacherSubject(Long classId, String teacherId, String subject);

    // Delete operations
    void deleteClass(Long classId);
    void softDeleteClass(Long classId);

    // Status operations
    ClassResponseDTO updateClassStatus(Long classId, String status);

    // Validation
    boolean isClassCodeExists(String classCode);
    boolean isTeacherAssignedToClass(Long teacherId, Long excludeClassId);

    // Get all subjects from a class
    ClassSubjectsResponseDTO getSubjectsByClassAndSection(String className, String section);
    ClassSubjectsResponseDTO getSubjectsByClassId(Long classId);
}