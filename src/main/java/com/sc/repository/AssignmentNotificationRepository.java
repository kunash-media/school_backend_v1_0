package com.sc.repository;

import com.sc.entity.AssignmentNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentNotificationRepository extends JpaRepository<AssignmentNotificationEntity, Long> {

    List<AssignmentNotificationEntity> findByStudent_StdIdOrderByCreatedAtDesc(Long studentId);

    List<AssignmentNotificationEntity> findByStudent_StdIdAndStatusOrderByCreatedAtDesc(Long studentId, String status);

    @Query("SELECT an FROM AssignmentNotificationEntity an WHERE an.student.stdId = :studentId AND an.readAt IS NULL")
    List<AssignmentNotificationEntity> findUnreadByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT an FROM AssignmentNotificationEntity an WHERE an.student.stdId = :studentId AND an.assignmentId = :assignmentId")
    List<AssignmentNotificationEntity> findByStudentAndAssignment(
            @Param("studentId") Long studentId,
            @Param("assignmentId") Long assignmentId);

    @Query("SELECT COUNT(an) FROM AssignmentNotificationEntity an WHERE an.student.stdId = :studentId AND an.readAt IS NULL")
    long countUnreadByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT an FROM AssignmentNotificationEntity an WHERE an.assignmentId = :assignmentId")
    List<AssignmentNotificationEntity> findByAssignmentId(@Param("assignmentId") Long assignmentId);
}