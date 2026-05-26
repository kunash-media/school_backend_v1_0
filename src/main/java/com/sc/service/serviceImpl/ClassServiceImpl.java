package com.sc.service.serviceImpl;

import com.sc.CustomExceptions.ResourceNotFoundException;
import com.sc.dto.request.ClassCreateRequestDTO;
import com.sc.dto.request.SubjectDetailDTO;
import com.sc.dto.response.ClassResponseDTO;
import com.sc.dto.request.TeacherSubjectAssignmentDTO;
import com.sc.dto.response.ClassSubjectsResponseDTO;
import com.sc.entity.ClassEntity;
import com.sc.repository.ClassRepository;
import com.sc.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClassServiceImpl implements ClassService {

    @Autowired
    private ClassRepository classRepository;

    @Override
    public ClassResponseDTO createClass(ClassCreateRequestDTO request) {
        if (isClassCodeExists(request.getClassCode())) {
            throw new IllegalArgumentException("Class code already exists: " + request.getClassCode());
        }

        ClassEntity classEntity = new ClassEntity();
        mapRequestToEntity(request, classEntity);

        classEntity.onCreate();           // populate JSON

        ClassEntity savedEntity = classRepository.save(classEntity);
        return mapEntityToResponse(savedEntity);
    }

    @Override
    public ClassResponseDTO createClassBasic(ClassCreateRequestDTO request) {
        if (request.getClassName() == null || request.getClassName().isEmpty()) {
            throw new IllegalArgumentException("Class name is required");
        }
        if (request.getClassCode() == null || request.getClassCode().isEmpty()) {
            throw new IllegalArgumentException("Class code is required");
        }

        if (isClassCodeExists(request.getClassCode())) {
            throw new IllegalArgumentException("Class code already exists: " + request.getClassCode());
        }

        ClassEntity classEntity = new ClassEntity(
                request.getClassName(),
                request.getClassCode(),
                request.getAcademicYear() != null ? request.getAcademicYear() : "2024-2025",
                request.getSection() != null ? request.getSection() : "A"
        );

        classEntity.setMaxStudents(request.getMaxStudents() != null ? request.getMaxStudents() : 0);
        classEntity.setCurrentStudents(request.getCurrentStudents() != null ? request.getCurrentStudents() : 0);

        classEntity.onCreate();

        ClassEntity savedEntity = classRepository.save(classEntity);
        return mapEntityToResponse(savedEntity);
    }

    @Override
    public ClassResponseDTO getClassById(Long classId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (Boolean.TRUE.equals(classEntity.getIsDeleted())) {
            throw new ResourceNotFoundException("Class has been deleted");
        }

        return mapEntityToResponse(classEntity);
    }

    @Override
    public ClassResponseDTO getClassByCode(String classCode) {
        ClassEntity classEntity = classRepository.findByClassCodeAndNotDeleted(classCode)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with code: " + classCode));

        return mapEntityToResponse(classEntity);
    }

    @Override
    public List<ClassResponseDTO> getAllClasses() {
        List<ClassEntity> classes = classRepository.findAllActiveClasses();
        return classes.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassResponseDTO> getClassesByAcademicYear(String academicYear) {
        List<ClassEntity> classes = classRepository.findByAcademicYear(academicYear);
        return classes.stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassResponseDTO> getClassesByClassName(String className) {
        List<ClassEntity> classes = classRepository.findByClassName(className);
        return classes.stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassResponseDTO> getClassesByTeacher(Long teacherId) {
        List<ClassEntity> classesAsClassTeacher = classRepository.findByClassTeacherId(teacherId);
        List<ClassEntity> classesAsAssistant = classRepository.findByAssistantTeacherId(teacherId);

        List<ClassEntity> allClasses = new java.util.ArrayList<>();
        allClasses.addAll(classesAsClassTeacher);
        allClasses.addAll(classesAsAssistant);

        return allClasses.stream()
                .distinct()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ClassResponseDTO updateClass(Long classId, ClassCreateRequestDTO request) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (Boolean.TRUE.equals(classEntity.getIsDeleted())) {
            throw new ResourceNotFoundException("Class has been deleted");
        }

        mapRequestToEntity(request, classEntity);
        classEntity.onUpdate();

        ClassEntity updatedEntity = classRepository.save(classEntity);
        return mapEntityToResponse(updatedEntity);
    }

    @Override
    public ClassResponseDTO updateClassTeacher(Long classId, Long teacherId, String subject) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (Boolean.TRUE.equals(classEntity.getIsDeleted())) {
            throw new ResourceNotFoundException("Class has been deleted");
        }

        classEntity.setClassTeacherId(teacherId);
        classEntity.setClassTeacherSubject(subject);
        classEntity.onUpdate();

        ClassEntity updatedEntity = classRepository.save(classEntity);
        return mapEntityToResponse(updatedEntity);
    }

    @Override
    public ClassResponseDTO updateAssistantTeacher(Long classId, Long teacherId, String subject) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (Boolean.TRUE.equals(classEntity.getIsDeleted())) {
            throw new ResourceNotFoundException("Class has been deleted");
        }

        classEntity.setAssistantTeacherId(teacherId);
        classEntity.setAssistantTeacherSubject(subject);
        classEntity.onUpdate();

        ClassEntity updatedEntity = classRepository.save(classEntity);
        return mapEntityToResponse(updatedEntity);
    }

    @Override
    public ClassResponseDTO addOtherTeacherSubject(Long classId, TeacherSubjectAssignmentDTO assignment) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (Boolean.TRUE.equals(classEntity.getIsDeleted())) {
            throw new ResourceNotFoundException("Class has been deleted");
        }

        var currentAssignments = classEntity.getOtherTeacherSubject();

        boolean teacherExists = false;
        for (ClassEntity.TeacherSubjectAssignment existing : currentAssignments) {
            if (existing.getTeacherId().equals(assignment.getTeacherId())) {
                // Add new subjects (avoid duplicates by subjectName)
                for (var subjectDto : assignment.getSubjects()) {
                    boolean alreadyExists = existing.getSubjects().stream()
                            .anyMatch(s -> s.getSubjectName().equalsIgnoreCase(subjectDto.getSubjectName()));
                    if (!alreadyExists) {
                        existing.getSubjects().add(
                                new ClassEntity.SubjectDetail(
                                        subjectDto.getSubId(),
                                        subjectDto.getSubjectName(),
                                        subjectDto.getTotalMarks()
                                )
                        );
                    }
                }
                teacherExists = true;
                break;
            }
        }

        if (!teacherExists) {
            List<ClassEntity.SubjectDetail> newSubjects = assignment.getSubjects().stream()
                    .map(dto -> new ClassEntity.SubjectDetail(
                            dto.getSubId(),
                            dto.getSubjectName(),
                            dto.getTotalMarks()
                    ))
                    .collect(Collectors.toList());

            var newAssignment = new ClassEntity.TeacherSubjectAssignment(
                    assignment.getTeacherId(),
                    assignment.getTeacherName(),
                    newSubjects
            );
            currentAssignments.add(newAssignment);
        }

        classEntity.setOtherTeacherSubject(currentAssignments);
        classEntity.onUpdate();

        ClassEntity updatedEntity = classRepository.save(classEntity);
        return mapEntityToResponse(updatedEntity);
    }

    @Override
    public ClassResponseDTO removeOtherTeacherSubject(Long classId, String teacherId, String subjectName) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (Boolean.TRUE.equals(classEntity.getIsDeleted())) {
            throw new ResourceNotFoundException("Class has been deleted");
        }

        var assignments = classEntity.getOtherTeacherSubject();

        assignments.removeIf(assignment -> {
            if (!assignment.getTeacherId().equals(teacherId)) {
                return false;
            }

            assignment.getSubjects().removeIf(sub -> sub.getSubjectName().equals(subjectName));

            // Remove teacher assignment if no subjects left
            return assignment.getSubjects().isEmpty();
        });

        classEntity.setOtherTeacherSubject(assignments);
        classEntity.onUpdate();

        ClassEntity updatedEntity = classRepository.save(classEntity);
        return mapEntityToResponse(updatedEntity);
    }

    @Override
    public void deleteClass(Long classId) {
        classRepository.deleteById(classId);
    }

    @Override
    public void softDeleteClass(Long classId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        classEntity.setIsDeleted(true);
        classEntity.onUpdate();
        classRepository.save(classEntity);
    }

    @Override
    public ClassResponseDTO updateClassStatus(Long classId, String status) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (Boolean.TRUE.equals(classEntity.getIsDeleted())) {
            throw new ResourceNotFoundException("Class has been deleted");
        }

        classEntity.setStatus(status);
        classEntity.onUpdate();

        ClassEntity updatedEntity = classRepository.save(classEntity);
        return mapEntityToResponse(updatedEntity);
    }

    @Override
    public boolean isClassCodeExists(String classCode) {
        return classRepository.findByClassCode(classCode).isPresent();
    }

    @Override
    public boolean isTeacherAssignedToClass(Long teacherId, Long excludeClassId) {
        List<ClassEntity> classesAsClassTeacher = classRepository.findByClassTeacherId(teacherId);
        List<ClassEntity> classesAsAssistant = classRepository.findByAssistantTeacherId(teacherId);

        for (ClassEntity c : classesAsClassTeacher) {
            if (!c.getClassId().equals(excludeClassId) && !Boolean.TRUE.equals(c.getIsDeleted())) {
                return true;
            }
        }

        for (ClassEntity c : classesAsAssistant) {
            if (!c.getClassId().equals(excludeClassId) && !Boolean.TRUE.equals(c.getIsDeleted())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ClassSubjectsResponseDTO getSubjectsByClassAndSection(String className, String section) {
        ClassEntity classEntity = classRepository.findByClassNameAndSection(className, section)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Class not found with name: " + className + " and section: " + section));

        if (Boolean.TRUE.equals(classEntity.getIsDeleted())) {
            throw new ResourceNotFoundException("Class has been deleted");
        }

        return getSubjectsFromEntity(classEntity);
    }



    @Override
    public ClassSubjectsResponseDTO getSubjectsByClassId(Long classId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (Boolean.TRUE.equals(classEntity.getIsDeleted())) {
            throw new ResourceNotFoundException("Class has been deleted");
        }

        return getSubjectsFromEntity(classEntity);
    }

    /**
     * Extract all unique subjects from class
     */
    private ClassSubjectsResponseDTO getSubjectsFromEntity(ClassEntity classEntity) {
        ClassSubjectsResponseDTO dto = new ClassSubjectsResponseDTO();
        dto.setClassId(classEntity.getClassId());
        dto.setClassName(classEntity.getClassName());
        dto.setClassCode(classEntity.getClassCode());
        dto.setSection(classEntity.getSection());
        dto.setAcademicYear(classEntity.getAcademicYear());

        Set<String> subjects = new LinkedHashSet<>();

        if (classEntity.getClassTeacherSubject() != null &&
                !classEntity.getClassTeacherSubject().isBlank()) {
            subjects.add(classEntity.getClassTeacherSubject());
        }

        if (classEntity.getAssistantTeacherSubject() != null &&
                !classEntity.getAssistantTeacherSubject().isBlank()) {
            subjects.add(classEntity.getAssistantTeacherSubject());
        }

        if (classEntity.getOtherTeacherSubject() != null) {
            for (ClassEntity.TeacherSubjectAssignment teacher : classEntity.getOtherTeacherSubject()) {
                if (teacher.getSubjects() != null) {
                    for (ClassEntity.SubjectDetail subject : teacher.getSubjects()) {
                        if (subject.getSubjectName() != null &&
                                !subject.getSubjectName().isBlank()) {
                            subjects.add(subject.getSubjectName());
                        }
                    }
                }
            }
        }

        dto.setSubjects(new ArrayList<>(subjects));
        return dto;
    }

    // ────────────────────────────────────────────────
    // Mapping helpers – updated for new subject structure
    // ────────────────────────────────────────────────

    private void mapRequestToEntity(ClassCreateRequestDTO request, ClassEntity entity) {
        entity.setClassName(request.getClassName());
        entity.setClassCode(request.getClassCode());
        entity.setAcademicYear(request.getAcademicYear());
        entity.setSection(request.getSection());
        entity.setMaxStudents(request.getMaxStudents());
        entity.setCurrentStudents(request.getCurrentStudents());
        entity.setRoomNumber(request.getRoomNumber());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setDescription(request.getDescription());
        entity.setClassTeacherId(request.getClassTeacherId());
        entity.setClassTeacherSubject(request.getClassTeacherSubject());
        entity.setAssistantTeacherId(request.getAssistantTeacherId());
        entity.setAssistantTeacherSubject(request.getAssistantTeacherSubject());
        entity.setWorkingDays(request.getWorkingDays());
        entity.setStatus(request.getStatus());

        // Map otherTeacherSubject
        if (request.getOtherTeacherSubject() != null) {
            List<ClassEntity.TeacherSubjectAssignment> assignments =
                    request.getOtherTeacherSubject().stream()
                            .map(dto -> {
                                List<ClassEntity.SubjectDetail> subjects = dto.getSubjects().stream()
                                        .map(subDto -> new ClassEntity.SubjectDetail(
                                                subDto.getSubId(),
                                                subDto.getSubjectName(),
                                                subDto.getTotalMarks()
                                        ))
                                        .collect(Collectors.toList());

                                return new ClassEntity.TeacherSubjectAssignment(
                                        dto.getTeacherId(),
                                        dto.getTeacherName(),
                                        subjects
                                );
                            })
                            .collect(Collectors.toList());

            entity.setOtherTeacherSubject(assignments);
        }
    }

    private ClassResponseDTO mapEntityToResponse(ClassEntity entity) {
        ClassResponseDTO response = new ClassResponseDTO();
        response.setClassId(entity.getClassId());
        response.setClassName(entity.getClassName());
        response.setClassCode(entity.getClassCode());
        response.setAcademicYear(entity.getAcademicYear());
        response.setSection(entity.getSection());
        response.setMaxStudents(entity.getMaxStudents());
        response.setCurrentStudents(entity.getCurrentStudents());
        response.setRoomNumber(entity.getRoomNumber());
        response.setStartTime(entity.getStartTime());
        response.setEndTime(entity.getEndTime());
        response.setDescription(entity.getDescription());
        response.setClassTeacherId(entity.getClassTeacherId());
        response.setClassTeacherSubject(entity.getClassTeacherSubject());
        response.setAssistantTeacherId(entity.getAssistantTeacherId());
        response.setAssistantTeacherSubject(entity.getAssistantTeacherSubject());
        response.setWorkingDays(entity.getWorkingDays());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        // Map otherTeacherSubject back to DTO
        List<TeacherSubjectAssignmentDTO> assignmentDTOs =
                entity.getOtherTeacherSubject().stream()
                        .map(assignment -> {
                            List<SubjectDetailDTO> subjectDTOs = assignment.getSubjects().stream()
                                    .map(sub -> new SubjectDetailDTO(
                                            sub.getSubId(),
                                            sub.getSubjectName(),
                                            sub.getTotalMarks()
                                    ))
                                    .collect(Collectors.toList());

                            return new TeacherSubjectAssignmentDTO(
                                    assignment.getTeacherId(),
                                    assignment.getTeacherName(),
                                    subjectDTOs
                            );
                        })
                        .collect(Collectors.toList());

        response.setOtherTeacherSubject(assignmentDTOs);

        return response;
    }
}