package com.sc.repository;

import com.sc.entity.NoticeAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeAttachmentRepository extends JpaRepository<NoticeAttachment, Long> {
    List<NoticeAttachment> findByNoticeNoticeId(Long noticeId);
    Optional<NoticeAttachment> findByStoredName(String storedName);
    void deleteByNoticeNoticeId(Long noticeId);
}