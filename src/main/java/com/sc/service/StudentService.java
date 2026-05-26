package com.sc.service;

import com.sc.dto.request.StudentRequestDto;
import com.sc.dto.response.StudentResponseDto;
import com.sc.entity.StudentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface StudentService {

    // ============= 📝 CREATE =============
    StudentResponseDto createStudent(StudentRequestDto requestDto);

    // ============= 🔍 READ =============
    // List<StudentResponseDto> getAllStudents();

    Page<StudentResponseDto> getAllStudents(Pageable pageable);

    // ============= ✅ ADD THESE AUTHENTICATION METHODS =============

    /**
     * Authenticate student by Student ID
     */
    boolean authenticateStudent(String studentId, String rawPassword);

    /**
     * Authenticate student by Roll Number
     */
    boolean authenticateStudentByRollNumber(String rollNumber, String rawPassword);

    /**
     * Get student by identifier (studentId or rollNumber)
     */
    StudentEntity getStudentByIdentifier(String identifier);

    StudentEntity getStudentByStudentIdEntity(String studentId);





    StudentResponseDto getStudentById(Long stdId);
    StudentResponseDto getStudentByStudentId(String studentId);
    StudentResponseDto getStudentByRollNumber(String rollNumber);
    List<StudentResponseDto> getStudentsByClass(String className);
    List<StudentResponseDto> getStudentsByClassAndSection(String className, String section);
    List<StudentResponseDto> getStudentsByStatus(String status);
    List<StudentResponseDto> getStudentsByAdmissionDate(Date admissionDate);
    List<StudentResponseDto> searchStudents(String name, String fatherName, String studentId, String rollNumber);

    // ============= 📊 STATISTICS =============
    Map<String, Long> getStudentCountByClass();
    Map<String, Map<String, Long>> getStudentCountByClassAndSection();
    Map<String, Object> getStudentStatistics();

    // ============= ✏️ UPDATE =============
    StudentResponseDto updateStudent(Long stdId, StudentRequestDto requestDto);
    StudentResponseDto updateStudentPartial(Long id, Map<String, Object> updates);
    StudentResponseDto updateStudentStatus(Long id, String status);
    StudentResponseDto updateStudentClassSection(Long id, String currentClass, String section);
    List<StudentResponseDto> bulkUpdateStudentStatus(List<Long> studentIds, String status);

    // ============= 🗑️ DELETE =============
    void deleteStudent(Long stdId);

    // ============= 🖼️ IMAGE UPLOAD =============
    StudentResponseDto uploadStudentImage(Long stdId, MultipartFile profileImage);
    StudentResponseDto uploadStudentDocuments(Long stdId,
                                              MultipartFile studentAadharImage,
                                              MultipartFile fatherAadharImage,
                                              MultipartFile motherAadharImage,
                                              MultipartFile birthCertificateImage,
                                              MultipartFile transferCertificateImage,
                                              MultipartFile markSheetImage,
                                              MultipartFile incomeCertificateImage,
                                              MultipartFile casteCertificateImage);

    // Add with other image retrieval methods
    byte[] getIncomeCertificateImage(Long stdId);
    byte[] getCasteCertificateImage(Long stdId);

    // ============= 🖼️ IMAGE RETRIEVAL =============
    byte[] getProfileImage(Long stdId);
    byte[] getStudentAadharImage(Long stdId);
    byte[] getFatherAadharImage(Long stdId);
    byte[] getMotherAadharImage(Long stdId);
    byte[] getBirthCertificateImage(Long stdId);
    byte[] getTransferCertificateImage(Long stdId);
    byte[] getMarkSheetImage(Long stdId);

    // ============= 💰 FEES =============
    StudentResponseDto updateStudentFees(Long stdId, StudentRequestDto requestDto);

    // ============= 👨‍🏫 TEACHER STUDENT METHODS ==========

    List<StudentResponseDto> getStudentsByTeacherId(Long teacherId);

    List<StudentResponseDto> getStudentsByTeacherCode(String teacherCode);

    List<StudentResponseDto> getStudentsByClassId(Long classId);
}