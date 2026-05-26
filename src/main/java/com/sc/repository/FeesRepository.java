package com.sc.repository;

import com.sc.entity.FeesEntity;
import com.sc.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeesRepository extends JpaRepository<FeesEntity, Long> {

    // Find by Student entity
    Optional<FeesEntity> findByStudent(StudentEntity student);

    // Find by Student ID using JPQL
    @Query("SELECT f FROM FeesEntity f WHERE f.student.stdId = :studentId")
    Optional<FeesEntity> findByStudentId(@Param("studentId") Long studentId);

    // Find by Student and Academic Year
    @Query("SELECT f FROM FeesEntity f WHERE f.student = :student AND f.academicYear = :academicYear")
    Optional<FeesEntity> findByStudentAndAcademicYear(
            @Param("student") StudentEntity student,
            @Param("academicYear") String academicYear);

    // Find all by Student
    List<FeesEntity> findAllByStudent(StudentEntity student);

    // Delete by Student
    void deleteByStudent(StudentEntity student);

    // Check if exists by Student ID
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FeesEntity f WHERE f.student.stdId = :studentId")
    boolean existsByStudentId(@Param("studentId") Long studentId);

    // Find with Student details (EAGER fetch)
    @Query("SELECT f FROM FeesEntity f JOIN FETCH f.student WHERE f.student.stdId = :studentId")
    Optional<FeesEntity> findByStudentIdWithStudent(@Param("studentId") Long studentId);

    // Find all pending fees (remaining > 0)
    @Query("SELECT f FROM FeesEntity f WHERE f.remainingFees > 0")
    List<FeesEntity> findAllPendingFees();

    // Find all paid fees (remaining = 0)
    @Query("SELECT f FROM FeesEntity f WHERE f.remainingFees = 0")
    List<FeesEntity> findAllPaidFees();

    // Find by Academic Year
    List<FeesEntity> findByAcademicYear(String academicYear);

    // Find by Payment Status (using remaining fees)
    @Query("SELECT f FROM FeesEntity f WHERE f.remainingFees > 0 AND f.academicYear = :academicYear")
    List<FeesEntity> findPendingFeesByAcademicYear(@Param("academicYear") String academicYear);

    // Get total collection by academic year
    @Query("SELECT SUM(f.totalFees) FROM FeesEntity f WHERE f.academicYear = :academicYear")
    Long getTotalCollectionByAcademicYear(@Param("academicYear") String academicYear);

    // Get pending amount by academic year
    @Query("SELECT SUM(f.remainingFees) FROM FeesEntity f WHERE f.academicYear = :academicYear")
    Long getPendingAmountByAcademicYear(@Param("academicYear") String academicYear);
}