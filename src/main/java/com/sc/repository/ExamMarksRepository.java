package com.sc.repository;

import com.sc.entity.ExamMarksEntity;
import com.sc.entity.ExamEntity.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamMarksRepository extends JpaRepository<ExamMarksEntity, Long> {

    @Query("SELECT e FROM ExamMarksEntity e WHERE e.student.stdId = :studentId AND e.examType = :examType AND e.academicYear = :academicYear")
    Optional<ExamMarksEntity> findByStudent_StdIdAndExamTypeAndAcademicYear(
            @Param("studentId") Long studentId,
            @Param("examType") ExamType examType,
            @Param("academicYear") String academicYear);

    @Query("SELECT e FROM ExamMarksEntity e WHERE e.student.stdId = :studentId")
    List<ExamMarksEntity> findByStudent_StdId(@Param("studentId") Long studentId);

    @Query("SELECT e FROM ExamMarksEntity e WHERE e.student.stdId = :studentId AND e.examType = :examType")
    List<ExamMarksEntity> findByStudent_StdIdAndExamType(@Param("studentId") Long studentId, @Param("examType") ExamType examType);

    @Query("SELECT e FROM ExamMarksEntity e WHERE e.student.currentClass = :className AND e.student.section = :section AND e.examType = :examType AND e.academicYear = :academicYear")
    List<ExamMarksEntity> findByClassAndSectionAndExamType(
            @Param("className") String className,
            @Param("section") String section,
            @Param("examType") ExamType examType,
            @Param("academicYear") String academicYear);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM ExamMarksEntity e WHERE e.student.stdId = :studentId AND e.examType = :examType AND e.academicYear = :academicYear")
    boolean existsByStudent_StdIdAndExamTypeAndAcademicYear(
            @Param("studentId") Long studentId,
            @Param("examType") ExamType examType,
            @Param("academicYear") String academicYear);
}