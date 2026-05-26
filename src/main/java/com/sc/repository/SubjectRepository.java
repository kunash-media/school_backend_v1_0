package com.sc.repository;

import com.sc.entity.SubjectEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository

public interface SubjectRepository extends JpaRepository<SubjectEntity, Long> {

    Optional<SubjectEntity> findBySubjectCode(String subjectCode);

    List<SubjectEntity> findBySubjectName(String subjectName);

    List<SubjectEntity> findBySubjectType(String subjectType);

    List<SubjectEntity> findByGradeLevel(String gradeLevel);

    List<SubjectEntity> findByStatus(String status);

    @Query("SELECT s FROM SubjectEntity s WHERE s.isDeleted = false")

    List<SubjectEntity> findAllActiveSubjects();

    @Query("SELECT s FROM SubjectEntity s WHERE s.subjectCode = :subjectCode AND s.isDeleted = false")

    Optional<SubjectEntity> findBySubjectCodeAndNotDeleted(@Param("subjectCode") String subjectCode);

    @Query("SELECT s FROM SubjectEntity s WHERE s.primaryTeacherId = :teacherId AND s.isDeleted = false")

    List<SubjectEntity> findByPrimaryTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT s FROM SubjectEntity s WHERE s.displayOrder IS NOT NULL ORDER BY s.displayOrder ASC")

    List<SubjectEntity> findAllByDisplayOrder();

    @Query("SELECT s FROM SubjectEntity s WHERE s.gradeLevel = :gradeLevel AND s.isDeleted = false ORDER BY s.displayOrder ASC")

    List<SubjectEntity> findByGradeLevelOrdered(@Param("gradeLevel") String gradeLevel);

    @Query("SELECT COUNT(s) FROM SubjectEntity s WHERE s.subjectCode = :subjectCode AND s.isDeleted = false")

    Long countBySubjectCode(@Param("subjectCode") String subjectCode);

}
