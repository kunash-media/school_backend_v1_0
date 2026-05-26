package com.sc.service;
import com.sc.dto.request.NoticeRequestDTO;
import com.sc.dto.request.NoticeStatsDTO;
import com.sc.dto.response.NoticeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
public interface NoticeService {
    NoticeResponseDTO createNotice(NoticeRequestDTO dto, List<MultipartFile> files, String createdBy) throws IOException;
    Page<NoticeResponseDTO> getAllNotices(String status, String priority, String category,
                                          String audience, String query,
                                          int page, int size, String sortBy, String sortDir);
    NoticeResponseDTO getNoticeById(Long noticeId);
    NoticeResponseDTO updateNotice(Long noticeId, NoticeRequestDTO dto, List<MultipartFile> newFiles) throws IOException;
    // Publish: draft→active, resolve recipients, create notifications, send emails
    NoticeResponseDTO publishNotice(Long noticeId);
    void deleteNotice(Long noticeId);
    void deleteAttachment(Long noticeId, Long attachmentId);
    NoticeStatsDTO getStats();
    // Dashboard lists
    List<NoticeResponseDTO> getActiveNoticesForTeachers();
    List<NoticeResponseDTO> getActiveNoticesForStudents();
    List<NoticeResponseDTO> getActiveNoticesForParents();
    List<NoticeResponseDTO> getActiveNoticesForClass(String targetClass, String section);
    List<NoticeResponseDTO> getNoticesForTeacher(Long teacherId);
    List<NoticeResponseDTO> getNoticesForStudent(Long stdId);
    // Notification bell
    long getUnreadCountForTeacher(Long teacherId);
    long getUnreadCountForStudent(Long stdId);
    void markAllReadForTeacher(Long teacherId);
    void markAllReadForStudent(Long stdId);
    void markOneNotificationRead(Long notificationId);
}
 