package com.sc.service;

import com.sc.dto.request.ClassCreateRequestDTO;


import com.sc.dto.request.SubjectCreateRequestDTO;
import com.sc.dto.response.SubjectResponseDTO;

import java.util.List;

public interface SubjectService {

    // Create operations

    SubjectResponseDTO createSubject(SubjectCreateRequestDTO request);

    // Read operations

    SubjectResponseDTO getSubjectById(Long subjectId);

    SubjectResponseDTO getSubjectByCode(String subjectCode);

    List<SubjectResponseDTO> getAllSubjects();

    List<SubjectResponseDTO> getSubjectsByType(String subjectType);

    List<SubjectResponseDTO> getSubjectsByGradeLevel(String gradeLevel);

    List<SubjectResponseDTO> getSubjectsByTeacher(Long teacherId);

    List<SubjectResponseDTO> getSubjectsOrderedByDisplay();

    // Update operations

    SubjectResponseDTO updateSubject(Long subjectId, SubjectCreateRequestDTO request);

    SubjectResponseDTO updateSubjectTeacher(Long subjectId, Long teacherId);

    SubjectResponseDTO updateSubjectStatus(Long subjectId, String status);

    SubjectResponseDTO updateSubjectDisplayOrder(Long subjectId, Integer displayOrder);

    // Delete operations

    void deleteSubject(Long subjectId);

    void softDeleteSubject(Long subjectId);

    // Validation

    boolean isSubjectCodeExists(String subjectCode);

    // Business logic

    List<SubjectResponseDTO> getSubjectsForClass(String gradeLevel, String section);

    boolean validateSubjectCombination(List<Long> subjectIds);

}

