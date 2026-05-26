package com.sc.repository;
import com.sc.entity.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
    // Admin filtered + paginated table
    @Query("""
            SELECT n FROM NoticeEntity n
            WHERE (:status   IS NULL OR n.status   = :status)
              AND (:priority  IS NULL OR n.priority = :priority)
              AND (:category  IS NULL OR n.category = :category)
              AND (:audience  IS NULL OR n.audience LIKE CONCAT('%',:audience,'%'))
              AND (:query     IS NULL
                   OR LOWER(n.title)       LIKE LOWER(CONCAT('%',:query,'%'))
                   OR LOWER(n.description) LIKE LOWER(CONCAT('%',:query,'%')))
            """)
    Page<NoticeEntity> findWithFilters(
            @Param("status")   String   status,
            @Param("priority") String   priority,
            @Param("category") String   category,
            @Param("audience") String   audience,
            @Param("query")    String   query,
            Pageable                    pageable
    );
    // Publish: draft → active
    @Modifying
    @Query("UPDATE NoticeEntity n SET n.status='active', n.publishDate=:publishDate WHERE n.noticeId=:noticeId")
    int publishNotice(@Param("noticeId") Long noticeId, @Param("publishDate") Date publishDate);
    // Auto-expire overdue notices
    @Modifying
    @Query("UPDATE NoticeEntity n SET n.status='expired' WHERE n.status='active' AND n.expiryDate IS NOT NULL AND n.expiryDate < :now")
    int expireOverdueNotices(@Param("now") Date now);
    // Stats
    long countByStatus(String status);
    // Teacher dashboard
    @Query("SELECT n FROM NoticeEntity n WHERE n.status='active' AND (n.audience LIKE '%all-users%' OR n.audience LIKE '%teachers%' OR n.audience LIKE '%staff%' OR n.audience LIKE '%management%') ORDER BY n.publishDate DESC")
    List<NoticeEntity> findActiveNoticesForTeachers();
    // Student dashboard
    @Query("SELECT n FROM NoticeEntity n WHERE n.status='active' AND (n.audience LIKE '%all-users%' OR n.audience LIKE '%students%') ORDER BY n.publishDate DESC")
    List<NoticeEntity> findActiveNoticesForStudents();
    // Parent dashboard
    @Query("SELECT n FROM NoticeEntity n WHERE n.status='active' AND (n.audience LIKE '%all-users%' OR n.audience LIKE '%parents%') ORDER BY n.publishDate DESC")
    List<NoticeEntity> findActiveNoticesForParents();
    // Class-specific notices
    @Query("SELECT n FROM NoticeEntity n WHERE n.status='active' AND n.audience LIKE '%class%' AND n.targetClass=:targetClass AND (n.targetSections='ALL' OR n.targetSections LIKE CONCAT('%',:section,'%')) ORDER BY n.publishDate DESC")
    List<NoticeEntity> findActiveNoticesForClass(@Param("targetClass") String targetClass, @Param("section") String section);
    // Notices received by a specific teacher (by teacher DB id)
    @Query("SELECT n FROM NoticeEntity n JOIN n.teacherRecipients t WHERE t.id=:teacherId AND n.status='active' ORDER BY n.publishDate DESC")
    List<NoticeEntity> findNoticesForTeacherId(@Param("teacherId") Long teacherId);
    // Notices received by a specific student (by stdId)
    @Query("SELECT n FROM NoticeEntity n JOIN n.studentRecipients s WHERE s.stdId=:stdId AND n.status='active' ORDER BY n.publishDate DESC")
    List<NoticeEntity> findNoticesForStudentId(@Param("stdId") Long stdId);
    // Recent notices for notification bell
    @Query("SELECT n FROM NoticeEntity n WHERE n.status='active' AND n.publishDate>=:since ORDER BY n.publishDate DESC")
    List<NoticeEntity> findRecentNotices(@Param("since") Date since);
    Page<NoticeEntity> findByStatus(String status, Pageable pageable);
    Page<NoticeEntity> findByCategory(String category, Pageable pageable);
    Page<NoticeEntity> findByPriority(String priority, Pageable pageable);
}
