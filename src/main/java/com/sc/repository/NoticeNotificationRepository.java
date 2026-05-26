package com.sc.repository;
import com.sc.entity.NoticeNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface NoticeNotificationRepository extends JpaRepository<NoticeNotification, Long> {
    // Teacher bell
    List<NoticeNotification> findByTeacherIdOrderBySentAtDesc(Long teacherId);
    long countByTeacherIdAndIsReadFalse(Long teacherId);
    // Student bell
    List<NoticeNotification> findByStudentStdIdOrderBySentAtDesc(Long stdId);
    long countByStudentStdIdAndIsReadFalse(Long stdId);
    // Mark all read - teacher
    @Modifying
    @Query("UPDATE NoticeNotification n SET n.isRead=true, n.readAt=CURRENT_TIMESTAMP, n.deliveryStatus='DELIVERED' WHERE n.teacher.id=:teacherId AND n.isRead=false")
    int markAllReadForTeacher(@Param("teacherId") Long teacherId);
    // Mark all read - student
    @Modifying
    @Query("UPDATE NoticeNotification n SET n.isRead=true, n.readAt=CURRENT_TIMESTAMP, n.deliveryStatus='DELIVERED' WHERE n.student.stdId=:stdId AND n.isRead=false")
    int markAllReadForStudent(@Param("stdId") Long stdId);
    // Mark one read
    @Modifying
    @Query("UPDATE NoticeNotification n SET n.isRead=true, n.readAt=CURRENT_TIMESTAMP, n.deliveryStatus='DELIVERED' WHERE n.notificationId=:notifId")
    int markOneAsRead(@Param("notifId") Long notifId);
    // All notifications for a notice
    List<NoticeNotification> findByNoticeNoticeId(Long noticeId);
    long countByNoticeNoticeIdAndIsReadTrue(Long noticeId);
    long countByNoticeNoticeIdAndIsReadFalse(Long noticeId);
}
 