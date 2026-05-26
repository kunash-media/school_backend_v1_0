package com.sc.repository;

import com.sc.entity.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {

    List<SubmissionEntity> findByAssignment_AssignmentId(Long assignmentId);

    List<SubmissionEntity> findByStudent_StdId(Long studentId);

    Optional<SubmissionEntity> findByAssignment_AssignmentIdAndStudent_StdId(Long assignmentId, Long studentId);

    long countByAssignment_AssignmentIdAndStatus(Long assignmentId, String status);

    long countByAssignment_AssignmentIdAndIsLateTrue(Long assignmentId);

    @Query("SELECT COUNT(s), " +
            "SUM(CASE WHEN s.status IN ('submitted', 'graded') THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN s.isLate = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN s.status = 'graded' THEN 1 ELSE 0 END), " +
            "AVG(s.obtainedMarks) " +
            "FROM SubmissionEntity s WHERE s.assignment.assignmentId = :assignmentId")
    List<Object[]> getSubmissionStats(@Param("assignmentId") Long assignmentId);

    @Modifying
    @Transactional
    void deleteByAssignment_AssignmentId(Long assignmentId);
}