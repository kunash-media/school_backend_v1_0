package com.sc.repository;

import com.sc.entity.NotificationEntity;
import com.sc.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByStudent(StudentEntity student);

    List<NotificationEntity> findByStudentAndStatus(StudentEntity student, String status);

    @Query("SELECT n FROM NotificationEntity n WHERE n.student.stdId = :studentId ORDER BY n.createdAt DESC")
    List<NotificationEntity> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT n FROM NotificationEntity n WHERE n.notificationType = 'FEE_REMINDER' AND n.status = 'SENT'")
    List<NotificationEntity> findPendingFeeReminders();

    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.student.stdId = :studentId AND n.status = 'SENT'")
    long countUnreadNotifications(@Param("studentId") Long studentId);

    // ============= 🆕 NEW METHOD FOR DUE DATE CHECK =============

    /**
     * Find notifications sent to a specific student for a specific installment within a date range
     * Used to prevent duplicate reminders on the same day
     *
     * @param studentId Student ID
     * @param installmentId Installment ID
     * @param startDate Start of date range (usually start of day)
     * @param endDate End of date range (usually end of day)
     * @return List of notifications sent in that period
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.student.stdId = :studentId AND n.installmentId = :installmentId AND n.createdAt BETWEEN :startDate AND :endDate")
    List<NotificationEntity> findByStudentIdAndInstallmentIdAndDateRange(
            @Param("studentId") Long studentId,
            @Param("installmentId") Long installmentId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);
}