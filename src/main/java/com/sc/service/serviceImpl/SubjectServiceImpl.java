package com.sc.service.serviceImpl;

import com.sc.CustomExceptions.ResourceNotFoundException;
import com.sc.dto.request.SubjectCreateRequestDTO;
import com.sc.dto.response.SubjectResponseDTO;
import com.sc.entity.SubjectEntity;
import com.sc.repository.SubjectRepository;
import com.sc.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubjectServiceImpl implements SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Override
    public SubjectResponseDTO createSubject(SubjectCreateRequestDTO request) {
        // Validate subject code uniqueness
        if (isSubjectCodeExists(request.getSubjectCode())) {
            throw new IllegalArgumentException("Subject code already exists: " + request.getSubjectCode());
        }

        // Create new subject entity
        SubjectEntity subjectEntity = new SubjectEntity();
        mapRequestToEntity(request, subjectEntity);

        // Save to database
        SubjectEntity savedEntity = subjectRepository.save(subjectEntity);

        // Convert to response DTO
        return mapEntityToResponse(savedEntity);
    }

    @Override
    public SubjectResponseDTO getSubjectById(Long subjectId) {
        SubjectEntity subjectEntity = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));

        if (Boolean.TRUE.equals(subjectEntity.getIsDeleted())) {
            throw new ResourceNotFoundException("Subject has been deleted");
        }

        return mapEntityToResponse(subjectEntity);
    }

    @Override
    public SubjectResponseDTO getSubjectByCode(String subjectCode) {
        SubjectEntity subjectEntity = subjectRepository.findBySubjectCodeAndNotDeleted(subjectCode)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with code: " + subjectCode));

        return mapEntityToResponse(subjectEntity);
    }

    @Override
    public List<SubjectResponseDTO> getAllSubjects() {
        List<SubjectEntity> subjects = subjectRepository.findAllActiveSubjects();
        return subjects.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectResponseDTO> getSubjectsByType(String subjectType) {
        List<SubjectEntity> subjects = subjectRepository.findBySubjectType(subjectType);
        return subjects.stream()
                .filter(s -> !Boolean.TRUE.equals(s.getIsDeleted()))
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectResponseDTO> getSubjectsByGradeLevel(String gradeLevel) {
        List<SubjectEntity> subjects = subjectRepository.findByGradeLevel(gradeLevel);
        return subjects.stream()
                .filter(s -> !Boolean.TRUE.equals(s.getIsDeleted()))
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectResponseDTO> getSubjectsByTeacher(Long teacherId) {
        List<SubjectEntity> subjects = subjectRepository.findByPrimaryTeacherId(teacherId);
        return subjects.stream()
                .filter(s -> !Boolean.TRUE.equals(s.getIsDeleted()))
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectResponseDTO> getSubjectsOrderedByDisplay() {
        List<SubjectEntity> subjects = subjectRepository.findAllByDisplayOrder();
        return subjects.stream()
                .filter(s -> !Boolean.TRUE.equals(s.getIsDeleted()))
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SubjectResponseDTO updateSubject(Long subjectId, SubjectCreateRequestDTO request) {
        SubjectEntity subjectEntity = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));

        if (Boolean.TRUE.equals(subjectEntity.getIsDeleted())) {
            throw new ResourceNotFoundException("Subject has been deleted");
        }

        // Update fields
        mapRequestToEntity(request, subjectEntity);

        // Save updated entity
        SubjectEntity updatedEntity = subjectRepository.save(subjectEntity);

        return mapEntityToResponse(updatedEntity);
    }

    @Override
    public SubjectResponseDTO updateSubjectTeacher(Long subjectId, Long teacherId) {
        SubjectEntity subjectEntity = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));

        if (Boolean.TRUE.equals(subjectEntity.getIsDeleted())) {
            throw new ResourceNotFoundException("Subject has been deleted");
        }

        subjectEntity.setPrimaryTeacherId(teacherId);

        SubjectEntity updatedEntity = subjectRepository.save(subjectEntity);
        return mapEntityToResponse(updatedEntity);
    }

    @Override
    public SubjectResponseDTO updateSubjectStatus(Long subjectId, String status) {
        SubjectEntity subjectEntity = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));

        if (Boolean.TRUE.equals(subjectEntity.getIsDeleted())) {
            throw new ResourceNotFoundException("Subject has been deleted");
        }

        subjectEntity.setStatus(status);

        SubjectEntity updatedEntity = subjectRepository.save(subjectEntity);
        return mapEntityToResponse(updatedEntity);
    }

    @Override
    public SubjectResponseDTO updateSubjectDisplayOrder(Long subjectId, Integer displayOrder) {
        SubjectEntity subjectEntity = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));

        if (Boolean.TRUE.equals(subjectEntity.getIsDeleted())) {
            throw new ResourceNotFoundException("Subject has been deleted");
        }

        subjectEntity.setDisplayOrder(displayOrder);

        SubjectEntity updatedEntity = subjectRepository.save(subjectEntity);
        return mapEntityToResponse(updatedEntity);
    }

    @Override
    public void deleteSubject(Long subjectId) {
        subjectRepository.deleteById(subjectId);
    }

    @Override
    public void softDeleteSubject(Long subjectId) {
        SubjectEntity subjectEntity = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));

        subjectEntity.setIsDeleted(true);
        subjectRepository.save(subjectEntity);
    }

    @Override
    public boolean isSubjectCodeExists(String subjectCode) {
        return subjectRepository.findBySubjectCode(subjectCode).isPresent();
    }

    @Override
    public List<SubjectResponseDTO> getSubjectsForClass(String gradeLevel, String section) {
        // Get subjects based on grade level
        List<SubjectEntity> subjects = subjectRepository.findByGradeLevelOrdered(gradeLevel);

        // Apply any section-specific logic here
        // For example, some sections might have different subjects

        return subjects.stream()
                .filter(s -> !Boolean.TRUE.equals(s.getIsDeleted()))
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean validateSubjectCombination(List<Long> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return false;
        }

        // Check if all subject IDs exist and are active
        for (Long subjectId : subjectIds) {
            try {
                SubjectEntity subject = subjectRepository.findById(subjectId)
                        .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

                if (Boolean.TRUE.equals(subject.getIsDeleted()) ||
                        !"ACTIVE".equals(subject.getStatus())) {
                    return false;
                }
            } catch (ResourceNotFoundException e) {
                return false;
            }
        }

        // Add additional validation logic here
        // For example: check for duplicate subject types, validate credit hours, etc.

        return true;
    }

    // Helper method to map request DTO to entity
    private void mapRequestToEntity(SubjectCreateRequestDTO request, SubjectEntity entity) {
        entity.setSubjectCode(request.getSubjectCode());
        entity.setSubjectName(request.getSubjectName());
        entity.setDescription(request.getDescription());
        entity.setSubjectType(request.getSubjectType());
        entity.setGradeLevel(request.getGradeLevel());
        entity.setMaxMarks(request.getMaxMarks());
        entity.setPassingMarks(request.getPassingMarks());
        entity.setCreditHours(request.getCreditHours());
        entity.setPeriodsPerWeek(request.getPeriodsPerWeek());
        entity.setColorCode(request.getColorCode());
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setPrimaryTeacherId(request.getPrimaryTeacherId());
        entity.setStatus(request.getStatus());
    }

    // Helper method to map entity to response DTO
    private SubjectResponseDTO mapEntityToResponse(SubjectEntity entity) {
        SubjectResponseDTO response = new SubjectResponseDTO();
        response.setSubjectId(entity.getSubjectId());
        response.setSubjectCode(entity.getSubjectCode());
        response.setSubjectName(entity.getSubjectName());
        response.setDescription(entity.getDescription());
        response.setSubjectType(entity.getSubjectType());
        response.setGradeLevel(entity.getGradeLevel());
        response.setMaxMarks(entity.getMaxMarks());
        response.setPassingMarks(entity.getPassingMarks());
        response.setCreditHours(entity.getCreditHours());
        response.setPeriodsPerWeek(entity.getPeriodsPerWeek());
        response.setColorCode(entity.getColorCode());
        response.setDisplayOrder(entity.getDisplayOrder());
        response.setPrimaryTeacherId(entity.getPrimaryTeacherId());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        return response;
    }
}