package com.sc.controller;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.sc.dto.request.NoticeRequestDTO;

import com.sc.dto.request.NoticeStatsDTO;

import com.sc.dto.response.NoticeResponseDTO;

import com.sc.service.FileStorageService;

import com.sc.service.NoticeService;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.Resource;

import org.springframework.data.domain.Page;

import org.springframework.http.*;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.net.MalformedURLException;

import java.util.List;

@RestController

@RequestMapping("/api/notices")

public class NoticeController {

    private static final Logger log = LoggerFactory.getLogger(NoticeController.class);

    private final NoticeService      noticeService;

    private final FileStorageService fileStorage;

    @Autowired

    public NoticeController(NoticeService noticeService, FileStorageService fileStorage) {

        this.noticeService = noticeService;

        this.fileStorage   = fileStorage;

    }

    // ── GET /api/notices/stats ─────────────────────────────

    @GetMapping("/stats")

    public ResponseEntity<NoticeStatsDTO> getStats() {

        return ResponseEntity.ok(noticeService.getStats());

    }

    // ── GET /api/notices ───────────────────────────────────

    @GetMapping

    public ResponseEntity<Page<NoticeResponseDTO>> getAllNotices(

            @RequestParam(defaultValue = "all")       String status,

            @RequestParam(defaultValue = "all")       String priority,

            @RequestParam(defaultValue = "all")       String category,

            @RequestParam(defaultValue = "all")       String audience,

            @RequestParam(defaultValue = "")          String query,

            @RequestParam(defaultValue = "0")         int    page,

            @RequestParam(defaultValue = "10")        int    size,

            @RequestParam(defaultValue = "createdAt") String sortBy,

            @RequestParam(defaultValue = "desc")      String sortDir

    ) {

        return ResponseEntity.ok(noticeService.getAllNotices(

                status, priority, category, audience, query, page, size, sortBy, sortDir));

    }

    // ── GET /api/notices/{id} ──────────────────────────────

    @GetMapping("/{id}")

    public ResponseEntity<NoticeResponseDTO> getById(@PathVariable Long id) {

        return ResponseEntity.ok(noticeService.getNoticeById(id));

    }

    // ── POST /api/notices ──────────────────────────────────

    // Content-Type: multipart/form-data

    // Part "notice" = JSON string

    // Part "files"  = optional files

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<NoticeResponseDTO> create(

            @RequestPart("notice")                           String              noticeJson,

            @RequestPart(value = "files", required = false) List<MultipartFile> files

    ) throws IOException {

        NoticeResponseDTO created = noticeService.createNotice(parse(noticeJson), files, "admin");

        log.info("[API] Notice created id={}", created.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);

    }

    // ── PUT /api/notices/{id} ──────────────────────────────

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<NoticeResponseDTO> update(

            @PathVariable                                    Long                id,

            @RequestPart("notice")                           String              noticeJson,

            @RequestPart(value = "files", required = false) List<MultipartFile> newFiles

    ) throws IOException {

        NoticeResponseDTO updated = noticeService.updateNotice(id, parse(noticeJson), newFiles);

        log.info("[API] Notice updated id={}", id);

        return ResponseEntity.ok(updated);

    }

    // ── PATCH /api/notices/{id}/publish ───────────────────

    @PatchMapping("/{id}/publish")

    public ResponseEntity<NoticeResponseDTO> publish(@PathVariable Long id) {

        NoticeResponseDTO published = noticeService.publishNotice(id);

        log.info("[API] Notice published id={}", id);

        return ResponseEntity.ok(published);

    }

    // ── DELETE /api/notices/{id} ───────────────────────────

    @DeleteMapping("/{id}")

    public ResponseEntity<Void> delete(@PathVariable Long id) {

        noticeService.deleteNotice(id);

        return ResponseEntity.noContent().build();

    }

    // ── DELETE /api/notices/{id}/attachments/{aId} ────────

    @DeleteMapping("/{id}/attachments/{attachId}")

    public ResponseEntity<Void> deleteAttachment(

            @PathVariable Long id,

            @PathVariable Long attachId) {

        noticeService.deleteAttachment(id, attachId);

        return ResponseEntity.noContent().build();

    }

    // ── GET /api/notices/{id}/attachments/{storedName} ────

    @GetMapping("/{id}/attachments/{storedName}")

    public ResponseEntity<Resource> download(

            @PathVariable Long   id,

            @PathVariable String storedName) throws MalformedURLException {

        Resource resource    = fileStorage.loadFileAsResource(storedName);

        String   contentType = fileStorage.getContentType(storedName);

        return ResponseEntity.ok()

                .contentType(MediaType.parseMediaType(contentType))

                .header(HttpHeaders.CONTENT_DISPOSITION,

                        "attachment; filename=\"" + resource.getFilename() + "\"")

                .body(resource);

    }

    // ══════════════════════════════════════════

    //  TEACHER DASHBOARD

    // ══════════════════════════════════════════

    @GetMapping("/teacher/all")

    public ResponseEntity<List<NoticeResponseDTO>> allForTeachers() {

        return ResponseEntity.ok(noticeService.getActiveNoticesForTeachers());

    }

    @GetMapping("/teacher/{teacherId}")

    public ResponseEntity<List<NoticeResponseDTO>> forTeacher(@PathVariable Long teacherId) {

        return ResponseEntity.ok(noticeService.getNoticesForTeacher(teacherId));

    }

    @GetMapping("/teacher/{teacherId}/unread")

    public ResponseEntity<Long> unreadTeacher(@PathVariable Long teacherId) {

        return ResponseEntity.ok(noticeService.getUnreadCountForTeacher(teacherId));

    }

    @PatchMapping("/teacher/{teacherId}/read-all")

    public ResponseEntity<Void> readAllTeacher(@PathVariable Long teacherId) {

        noticeService.markAllReadForTeacher(teacherId);

        return ResponseEntity.ok().build();

    }

    // ══════════════════════════════════════════

    //  STUDENT DASHBOARD

    // ══════════════════════════════════════════

    @GetMapping("/student/all")

    public ResponseEntity<List<NoticeResponseDTO>> allForStudents() {

        return ResponseEntity.ok(noticeService.getActiveNoticesForStudents());

    }

    @GetMapping("/student/{stdId}")

    public ResponseEntity<List<NoticeResponseDTO>> forStudent(@PathVariable Long stdId) {

        return ResponseEntity.ok(noticeService.getNoticesForStudent(stdId));

    }

    @GetMapping("/student/{stdId}/unread")

    public ResponseEntity<Long> unreadStudent(@PathVariable Long stdId) {

        return ResponseEntity.ok(noticeService.getUnreadCountForStudent(stdId));

    }

    @PatchMapping("/student/{stdId}/read-all")

    public ResponseEntity<Void> readAllStudent(@PathVariable Long stdId) {

        noticeService.markAllReadForStudent(stdId);

        return ResponseEntity.ok().build();

    }

    // ══════════════════════════════════════════

    //  PARENT DASHBOARD

    // ══════════════════════════════════════════

    @GetMapping("/parent/all")

    public ResponseEntity<List<NoticeResponseDTO>> allForParents() {

        return ResponseEntity.ok(noticeService.getActiveNoticesForParents());

    }

    // ══════════════════════════════════════════

    //  CLASS-SPECIFIC

    //  GET /api/notices/class?class=9&section=A

    // ══════════════════════════════════════════

    @GetMapping("/class")

    public ResponseEntity<List<NoticeResponseDTO>> forClass(

            @RequestParam("class")   String targetClass,

            @RequestParam("section") String section) {

        return ResponseEntity.ok(noticeService.getActiveNoticesForClass(targetClass, section));

    }

    // ══════════════════════════════════════════

    //  MARK ONE NOTIFICATION READ

    // ══════════════════════════════════════════

    @PatchMapping("/notification/{notifId}/read")

    public ResponseEntity<Void> markOneRead(@PathVariable Long notifId) {

        noticeService.markOneNotificationRead(notifId);

        return ResponseEntity.ok().build();

    }

    // ── JSON parse helper ──────────────────────────────────

    private NoticeRequestDTO parse(String json) {

        try {

            ObjectMapper mapper = new ObjectMapper();

            mapper.registerModule(new JavaTimeModule());

            return mapper.readValue(json, NoticeRequestDTO.class);

        } catch (Exception e) {

            throw new IllegalArgumentException("Invalid notice JSON: " + e.getMessage());

        }

    }

}
 