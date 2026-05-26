package com.sc.service.serviceImpl;
import com.sc.CustomExceptions.ResourceNotFoundException;
import com.sc.dto.request.NoticeRequestDTO;
import com.sc.dto.request.NoticeStatsDTO;
import com.sc.dto.response.NoticeResponseDTO;
import com.sc.entity.*;
import com.sc.repository.*;
import com.sc.service.EmailService;
import com.sc.service.FileStorageService;
import com.sc.service.NoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
@Service
@Transactional
public class NoticeServiceImpl implements NoticeService {
    private static final Logger log = LoggerFactory.getLogger(NoticeServiceImpl.class);
    private final NoticeRepository             noticeRepo;
    private final NoticeNotificationRepository notifRepo;
    private final NoticeAttachmentRepository   attachmentRepo;
    private final TeacherRepository            teacherRepo;
    private final StudentRepository            studentRepo;
    private final FileStorageService fileStorage;
    private final EmailService                 emailService;
    @Autowired
    public NoticeServiceImpl(NoticeRepository             noticeRepo,
                             NoticeNotificationRepository notifRepo,
                             NoticeAttachmentRepository   attachmentRepo,
                             TeacherRepository            teacherRepo,
                             StudentRepository            studentRepo,
                             FileStorageService           fileStorage,
                             EmailService                 emailService) {
        this.noticeRepo   = noticeRepo;
        this.notifRepo    = notifRepo;
        this.attachmentRepo = attachmentRepo;
        this.teacherRepo  = teacherRepo;
        this.studentRepo  = studentRepo;
        this.fileStorage  = fileStorage;
        this.emailService = emailService;
    }
    // ══════════════════════════════════════════
    //  CREATE
    // ══════════════════════════════════════════
    @Override
    public NoticeResponseDTO createNotice(NoticeRequestDTO dto, List<MultipartFile> files, String createdBy) throws IOException {
        validate(dto);
        NoticeEntity notice = toEntity(dto);
        notice.setCreatedBy(createdBy);
        noticeRepo.save(notice);
        if (files != null) {
            for (MultipartFile f : files) {
                if (f != null && !f.isEmpty()) saveAttachment(notice, f);
            }
        }
        log.info("[Notice] Created id={} title='{}' by={}", notice.getNoticeId(), notice.getTitle(), createdBy);
        return toDTO(noticeRepo.findById(notice.getNoticeId()).orElseThrow());
    }
    // ══════════════════════════════════════════
    //  GET ALL — filtered + paginated
    // ══════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public Page<NoticeResponseDTO> getAllNotices(String status, String priority, String category,
                                                 String audience, String query,
                                                 int page, int size, String sortBy, String sortDir) {
        Sort     sort     = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return noticeRepo.findWithFilters(
                skip(status, "all"), skip(priority, "all"),
                skip(category, "all"), skip(audience, "all"),
                (query != null && !query.isBlank()) ? query.trim() : null,
                pageable
        ).map(this::toDTO);
    }
    // ══════════════════════════════════════════
    //  GET BY ID
    // ══════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public NoticeResponseDTO getNoticeById(Long id) {
        return toDTO(findOrThrow(id));
    }
    // ══════════════════════════════════════════
    //  UPDATE
    // ══════════════════════════════════════════
    @Override
    public NoticeResponseDTO updateNotice(Long id, NoticeRequestDTO dto, List<MultipartFile> newFiles) throws IOException {
        NoticeEntity n = findOrThrow(id);
        validate(dto);
        n.setTitle(dto.getTitle());
        n.setDescription(dto.getDescription());
        n.setCategory(val(dto.getCategory(), "general"));
        n.setPriority(val(dto.getPriority(), "medium"));
        n.setStatus(val(dto.getStatus(), "draft"));
        n.setAudience(dto.getAudience());
        n.setTargetClass(dto.getTargetClass());
        n.setTargetSections(dto.getTargetSections());
        n.setPublishDate(dto.getPublishDate());
        n.setExpiryDate(dto.getExpiryDate());
        noticeRepo.save(n);
        if (newFiles != null) {
            for (MultipartFile f : newFiles) {
                if (f != null && !f.isEmpty()) saveAttachment(n, f);
            }
        }
        log.info("[Notice] Updated id={}", id);
        return toDTO(noticeRepo.findById(id).orElseThrow());
    }
    // ══════════════════════════════════════════
    //  PUBLISH
    //  1. status = active
    //  2. Resolve teacher recipients from TeacherEntity
    //  3. Resolve student recipients from StudentEntity
    //     (class + section filter applied)
    //  4. Create NoticeNotification per recipient
    //     → powers bell badge on dashboard
    //  5. Send email to teachers (teacher.getEmail())
    //  6. Send email to parents (fatherEmail→motherEmail→guardianEmail)
    //  7. Send in-app notification to students (student dashboard bell)
    // ══════════════════════════════════════════
    @Override
    public NoticeResponseDTO publishNotice(Long id) {
        NoticeEntity n = findOrThrow(id);
        if ("active".equalsIgnoreCase(n.getStatus()))
            throw new IllegalStateException("Notice is already published and active.");
        if ("expired".equalsIgnoreCase(n.getStatus()))
            throw new IllegalStateException("Cannot publish expired notice. Update expiry date first.");
        // Step 1 — activate
        n.setStatus("active");
        n.setPublishDate(new Date());
        noticeRepo.save(n);
        String audience = n.getAudience() != null ? n.getAudience() : "";
        // Step 2 — resolve teachers
        List<TeacherEntity> teachers = resolveTeachers(audience);
        teachers.forEach(n::addTeacherRecipient);
        // Step 3 — resolve students
        List<StudentEntity> students = resolveStudents(audience, n.getTargetClass(), n.getTargetSections());
        students.forEach(n::addStudentRecipient);
        noticeRepo.save(n);
        // Step 4 — create in-app notifications
        List<NoticeNotification> notifs = new ArrayList<>();
        // TEACHER in-app notification (shows on teacher dashboard bell)
        for (TeacherEntity t : teachers) {
            NoticeNotification notif = new NoticeNotification();
            notif.setNotice(n);
            notif.setTeacher(t);
            notif.setRecipientType("TEACHER");
            notif.setDeliveryStatus("DELIVERED");
            notifs.add(notif);
        }
        // STUDENT in-app notification (shows on student dashboard bell)
        boolean sendToStudents = audience.contains("students") || audience.contains("all-users") || audience.contains("class");
        if (sendToStudents) {
            for (StudentEntity s : students) {
                NoticeNotification notif = new NoticeNotification();
                notif.setNotice(n);
                notif.setStudent(s);
                notif.setRecipientType("STUDENT");
                notif.setDeliveryStatus("DELIVERED");
                notifs.add(notif);
            }
        }
        // PARENT in-app notification (linked via student, shows on parent portal)
        boolean sendToParents = audience.contains("parents") || audience.contains("all-users");
        if (sendToParents) {
            for (StudentEntity s : students) {
                NoticeNotification notif = new NoticeNotification();
                notif.setNotice(n);
                notif.setStudent(s);
                notif.setRecipientType("PARENT");
                notif.setDeliveryStatus("DELIVERED");
                notifs.add(notif);
            }
        }
        notifRepo.saveAll(notifs);
        // Step 5 — Email to teachers
        for (TeacherEntity t : teachers) {
            if (t.getEmail() != null && !t.getEmail().isBlank()) {
                emailService.sendNoticeEmailToTeacher(t.getEmail(), t.getFullName(), n);
            }
        }
        // Step 6 — Email to parents of students
        // Uses fatherEmail → motherEmail → guardianEmail (priority order)
        if (sendToParents) {
            for (StudentEntity s : students) {
                emailService.sendNoticeEmail(s, n);
            }
        }
        // Step 7 — Email to students directly (if audience = students)
        // Note: student email is not in StudentEntity (parent email used)
        // So student is notified via in-app bell only (Step 4 above)
        log.info("[Notice] Published id={} | teachers={} | students={} | notifs={} | emails sent",
                id, teachers.size(), students.size(), notifs.size());
        return toDTO(noticeRepo.findById(id).orElseThrow());
    }
    // ══════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════
    @Override
    public void deleteNotice(Long id) {
        NoticeEntity n = findOrThrow(id);
        n.getAttachments().forEach(a -> fileStorage.deleteFile(a.getStoredName()));
        noticeRepo.delete(n);
        log.info("[Notice] Deleted id={}", id);
    }
    @Override
    public void deleteAttachment(Long noticeId, Long attachmentId) {
        NoticeAttachment a = attachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));
        if (!a.getNotice().getNoticeId().equals(noticeId))
            throw new IllegalArgumentException("Attachment does not belong to notice " + noticeId);
        fileStorage.deleteFile(a.getStoredName());
        attachmentRepo.delete(a);
    }
    // ══════════════════════════════════════════
    //  STATS
    // ══════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public NoticeStatsDTO getStats() {
        return new NoticeStatsDTO(
                noticeRepo.count(),
                noticeRepo.countByStatus("active"),
                noticeRepo.countByStatus("draft"),
                noticeRepo.countByStatus("expired")
        );
    }
    // ══════════════════════════════════════════
    //  DASHBOARD LISTS
    // ══════════════════════════════════════════
    @Override @Transactional(readOnly = true)
    public List<NoticeResponseDTO> getActiveNoticesForTeachers() {
        return noticeRepo.findActiveNoticesForTeachers().stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override @Transactional(readOnly = true)
    public List<NoticeResponseDTO> getActiveNoticesForStudents() {
        return noticeRepo.findActiveNoticesForStudents().stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override @Transactional(readOnly = true)
    public List<NoticeResponseDTO> getActiveNoticesForParents() {
        return noticeRepo.findActiveNoticesForParents().stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override @Transactional(readOnly = true)
    public List<NoticeResponseDTO> getActiveNoticesForClass(String targetClass, String section) {
        return noticeRepo.findActiveNoticesForClass(targetClass, section).stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override @Transactional(readOnly = true)
    public List<NoticeResponseDTO> getNoticesForTeacher(Long teacherId) {
        return noticeRepo.findNoticesForTeacherId(teacherId).stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override @Transactional(readOnly = true)
    public List<NoticeResponseDTO> getNoticesForStudent(Long stdId) {
        return noticeRepo.findNoticesForStudentId(stdId).stream().map(this::toDTO).collect(Collectors.toList());
    }
    // ══════════════════════════════════════════
    //  NOTIFICATION BELL
    // ══════════════════════════════════════════
    @Override @Transactional(readOnly = true)
    public long getUnreadCountForTeacher(Long teacherId) {
        return notifRepo.countByTeacherIdAndIsReadFalse(teacherId);
    }
    @Override @Transactional(readOnly = true)
    public long getUnreadCountForStudent(Long stdId) {
        return notifRepo.countByStudentStdIdAndIsReadFalse(stdId);
    }
    @Override public void markAllReadForTeacher(Long teacherId) { notifRepo.markAllReadForTeacher(teacherId); }
    @Override public void markAllReadForStudent(Long stdId)     { notifRepo.markAllReadForStudent(stdId); }
    @Override public void markOneNotificationRead(Long notifId) { notifRepo.markOneAsRead(notifId); }
    // ══════════════════════════════════════════
    //  SCHEDULED — auto-expire every hour
    // ══════════════════════════════════════════
    @Scheduled(cron = "0 0 * * * *")
    public void autoExpireNotices() {
        int count = noticeRepo.expireOverdueNotices(new Date());
        if (count > 0) log.info("[Scheduler] Auto-expired {} notices.", count);
    }
    // ══════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════
    /**
     * audience "all-users" or "teachers" → all active teachers
     * audience "staff"      → employmentType = "Staff"
     * audience "management" → designation contains "Management"
     */
    private List<TeacherEntity> resolveTeachers(String audience) {
        if (audience.contains("all-users") || audience.contains("teachers")) {
            return teacherRepo.findAll().stream()
                    .filter(t -> Boolean.TRUE.equals(t.getActive()))
                    .collect(Collectors.toList());
        }
        List<TeacherEntity> result = new ArrayList<>();
        if (audience.contains("staff")) {
            teacherRepo.findAll().stream()
                    .filter(t -> Boolean.TRUE.equals(t.getActive()))
                    .filter(t -> "Staff".equalsIgnoreCase(t.getEmploymentType()))
                    .forEach(result::add);
        }
        if (audience.contains("management")) {
            teacherRepo.findAll().stream()
                    .filter(t -> Boolean.TRUE.equals(t.getActive()))
                    .filter(t -> t.getDesignation() != null && t.getDesignation().toLowerCase().contains("management"))
                    .forEach(result::add);
        }
        return result.stream().distinct().collect(Collectors.toList());
    }
    /**
     * audience "all-users" / "students" / "parents" → all active students
     * audience "class" → filter by currentClass + section
     * targetSections "ALL" → all sections of that class
     * targetSections "A,B" → only those sections
     */
    private List<StudentEntity> resolveStudents(String audience, String targetClass, String targetSections) {
        if (audience.contains("all-users") || audience.contains("students") || audience.contains("parents")) {
            return studentRepo.findAll().stream()
                    .filter(s -> "Active".equalsIgnoreCase(s.getStatus()))
                    .collect(Collectors.toList());
        }
        if (audience.contains("class") && targetClass != null && !targetClass.isBlank()) {
            return studentRepo.findAll().stream()
                    .filter(s -> "Active".equalsIgnoreCase(s.getStatus()))
                    .filter(s -> targetClass.equalsIgnoreCase(s.getCurrentClass()))
                    .filter(s -> {
                        if (targetSections == null || targetSections.isBlank() || "ALL".equalsIgnoreCase(targetSections))
                            return true;
                        List<String> secs = Arrays.stream(targetSections.split(","))
                                .map(String::trim).collect(Collectors.toList());
                        return s.getSection() != null && secs.contains(s.getSection().trim());
                    })
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    private void saveAttachment(NoticeEntity notice, MultipartFile file) throws IOException {
        String stored      = fileStorage.storeFile(file);
        String downloadUrl = "/api/notices/" + notice.getNoticeId() + "/attachments/" + stored;
        long   bytes       = file.getSize();
        NoticeAttachment a = new NoticeAttachment();
        a.setOriginalName(file.getOriginalFilename());
        a.setStoredName(stored);
        a.setFileType(file.getContentType());
        a.setFileSize(bytes);
        a.setDownloadUrl(downloadUrl);
        a.setNotice(notice);
        attachmentRepo.save(a);
        notice.getAttachments().add(a);
    }
    private void validate(NoticeRequestDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank())
            throw new IllegalArgumentException("Title is required");
        if (dto.getDescription() == null || dto.getDescription().isBlank())
            throw new IllegalArgumentException("Description is required");
        if (dto.getAudience() == null || dto.getAudience().isBlank())
            throw new IllegalArgumentException("Audience is required");
        if (dto.getAudience().contains("class")) {
            if (dto.getTargetClass() == null || dto.getTargetClass().isBlank())
                throw new IllegalArgumentException("targetClass required when audience = class");
            if (dto.getTargetSections() == null || dto.getTargetSections().isBlank())
                throw new IllegalArgumentException("targetSections required. Use 'ALL' or 'A,B'");
        }
    }
    private NoticeEntity toEntity(NoticeRequestDTO dto) {
        NoticeEntity n = new NoticeEntity();
        n.setTitle(dto.getTitle());
        n.setDescription(dto.getDescription());
        n.setCategory(val(dto.getCategory(), "general"));
        n.setPriority(val(dto.getPriority(), "medium"));
        n.setStatus(val(dto.getStatus(), "draft"));
        n.setAudience(dto.getAudience());
        n.setTargetClass(dto.getTargetClass());
        n.setTargetSections(dto.getTargetSections());
        n.setPublishDate(dto.getPublishDate());
        n.setExpiryDate(dto.getExpiryDate());
        return n;
    }
    private NoticeResponseDTO toDTO(NoticeEntity n) {
        NoticeResponseDTO dto = new NoticeResponseDTO();
        dto.setId(n.getNoticeId());
        dto.setTitle(n.getTitle());
        dto.setDescription(n.getDescription());
        dto.setCategory(n.getCategory());
        dto.setPriority(n.getPriority());
        dto.setStatus(n.getStatus());
        dto.setAudience(n.getAudience());
        dto.setTargetClass(n.getTargetClass());
        dto.setTargetSections(n.getTargetSections());
        dto.setPublishDate(n.getPublishDate());
        dto.setExpiryDate(n.getExpiryDate());
        dto.setCreatedBy(n.getCreatedBy());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setUpdatedAt(n.getUpdatedAt());
        dto.setTotalTeacherRecipients(n.getTeacherRecipients().size());
        dto.setTotalStudentRecipients(n.getStudentRecipients().size());
        dto.setTotalNotificationsSent(n.getNotifications().size());
        long read = n.getNotifications().stream().filter(NoticeNotification::isRead).count();
        dto.setTotalNotificationsRead((int) read);
        dto.setTotalNotificationsPending((int)(n.getNotifications().size() - read));
        if (n.getAttachments() != null) {
            List<NoticeResponseDTO.AttachmentDTO> atts = n.getAttachments().stream().map(a -> {
                NoticeResponseDTO.AttachmentDTO ad = new NoticeResponseDTO.AttachmentDTO();
                ad.setId(a.getId());
                ad.setName(a.getOriginalName());
                long bytes = a.getFileSize() != null ? a.getFileSize() : 0;
                ad.setSize(bytes > 1024 * 1024
                        ? String.format("%.1f MB", bytes / 1024.0 / 1024.0)
                        : String.format("%d KB", bytes / 1024));
                ad.setFileType(a.getFileType());
                ad.setDownloadUrl(a.getDownloadUrl());
                return ad;
            }).collect(Collectors.toList());
            dto.setAttachments(atts);
        }
        return dto;
    }
    private NoticeEntity findOrThrow(Long id) {
        return noticeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found: " + id));
    }
    private String skip(String val, String skipVal) {
        return (val != null && !val.equalsIgnoreCase(skipVal)) ? val : null;
    }
    private String val(String value, String defaultVal) {
        return (value != null && !value.isBlank()) ? value : defaultVal;
    }
}
