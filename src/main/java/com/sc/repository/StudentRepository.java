package com.sc.repository;

import com.sc.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

    // ============= 🔍 EXISTENCE CHECKS =============
    boolean existsByStudentId(String studentId);
    boolean existsByStudentRollNumber(String studentRollNumber);

    // ============= 🔍 FIND BY UNIQUE FIELDS =============
    StudentEntity findByStudentId(String studentId);
    StudentEntity findByStudentRollNumber(String studentRollNumber);

    // ============= 🔍 FIND BY FILTERS =============
    List<StudentEntity> findByCurrentClass(String currentClass);
    List<StudentEntity> findByCurrentClassAndSection(String currentClass, String section);
    List<StudentEntity> findByStatus(String status);
    List<StudentEntity> findByAdmissionDate(Date admissionDate);

    // ============= 📊 COUNT QUERIES =============
    long countByStatus(String status);
    long countByGender(String gender);

    @Query("SELECT s.currentClass, COUNT(s) FROM StudentEntity s GROUP BY s.currentClass")
    List<Object[]> getStudentCountByClass();

    @Query("SELECT s.currentClass, s.section, COUNT(s) FROM StudentEntity s GROUP BY s.currentClass, s.section")
    List<Object[]> getStudentCountByClassAndSection();

    // ============= 🔍 SEARCH =============
    @Query("SELECT s FROM StudentEntity s WHERE " +
            "(:name IS NULL OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(s.middleName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:fatherName IS NULL OR LOWER(s.fatherName) LIKE LOWER(CONCAT('%', :fatherName, '%'))) AND " +
            "(:studentId IS NULL OR s.studentId = :studentId) AND " +
            "(:rollNumber IS NULL OR s.studentRollNumber = :rollNumber)")
    List<StudentEntity> searchStudents(@Param("name") String name,
                                       @Param("fatherName") String fatherName,
                                       @Param("studentId") String studentId,
                                       @Param("rollNumber") String rollNumber);

    // ============= 🔍 FIXED METHOD - CLASS AND SECTION BASED STUDENT FETCH =============
    @Query("SELECT s FROM StudentEntity s WHERE s.currentClass = :className " +
            "AND (:section IS NULL OR s.section = :section OR :section = 'All Sections')")
    List<StudentEntity> findStudentsByClassAndSection(@Param("className") String className,
                                                      @Param("section") String section);

    // ============= 📊 COUNT BY CLASS AND SECTION =============
    @Query("SELECT COUNT(s) FROM StudentEntity s WHERE s.currentClass = :className " +
            "AND (:section IS NULL OR s.section = :section OR :section = 'All Sections')")
    long countStudentsByClassAndSection(@Param("className") String className,
                                        @Param("section") String section);

    // ============= 🔍 FIND ACTIVE STUDENTS =============
    @Query("SELECT s FROM StudentEntity s WHERE s.status = 'Active'")
    List<StudentEntity> findAllActiveStudents();

    // ============= 🔍 FIND STUDENTS BY ADMISSION YEAR =============
    @Query("SELECT s FROM StudentEntity s WHERE YEAR(s.admissionDate) = :year")
    List<StudentEntity> findByAdmissionYear(@Param("year") int year);

    // ============= 🔍 FIND STUDENTS BY MULTIPLE CLASSES =============
    @Query("SELECT s FROM StudentEntity s WHERE s.currentClass IN :classNames")
    List<StudentEntity> findByClassNames(@Param("classNames") List<String> classNames);

    // ============= 📊 GET STUDENT STATISTICS =============
    @Query("SELECT COUNT(s), s.gender FROM StudentEntity s GROUP BY s.gender")
    List<Object[]> getGenderStatistics();

    @Query("SELECT COUNT(s), s.casteCategory FROM StudentEntity s GROUP BY s.casteCategory")
    List<Object[]> getCasteStatistics();

    // ============= 🔍 FIND STUDENTS WITH PENDING FEES =============
    @Query("SELECT s FROM StudentEntity s WHERE s.status = 'Active' AND " +
            "(SELECT SUM(f.remainingFees) FROM FeesEntity f WHERE f.student.stdId = s.stdId) > 0")
    List<StudentEntity> findStudentsWithPendingFees();

    // ============= 🔍 FIND STUDENTS BY PARENT PHONE =============
    StudentEntity findByFatherPhone(String fatherPhone);
    StudentEntity findByMotherPhone(String motherPhone);
    StudentEntity findByGuardianPhone(String guardianPhone);

    // ============= 🔍 FIND STUDENTS BY EMERGENCY CONTACT =============
    StudentEntity findByEmergencyContact(String emergencyContact);
}