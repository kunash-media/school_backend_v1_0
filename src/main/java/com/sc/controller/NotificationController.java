package com.sc.controller;

import com.sc.dto.response.NotificationResponseDto;
import com.sc.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    // ============= 📨 SEND REMINDERS (ONLY TO PENDING STUDENTS) =============

    /**
     * Send fee reminder to a specific student (ONLY if fees are pending)
     * URL: POST http://localhost:8084/api/notifications/fee-reminder/student/{studentId}
     */
    @PostMapping("/fee-reminder/student/{studentId}")
    public ResponseEntity<?> sendFeeReminderToStudent(@PathVariable Long studentId) {
        logger.info("📨 Sending fee reminder to student ID: {} (Only if fees are pending)", studentId);
        try {
            NotificationResponseDto response = notificationService.sendFeeReminderToStudent(studentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to send reminder: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to send reminder: " + e.getMessage());
        }
    }

    /**
     * Send fee reminders to ALL students with pending fees
     * URL: POST http://localhost:8084/api/notifications/fee-reminder/all-pending
     */
    @PostMapping("/fee-reminder/all-pending")
    public ResponseEntity<?> sendFeeRemindersToAllPendingStudents() {
        logger.info("📨📨 Sending fee reminders to ALL students with pending fees...");
        try {
            List<NotificationResponseDto> responses = notificationService.sendFeeRemindersToAllPendingStudents();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("❌ Failed to send bulk reminders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send bulk reminders: " + e.getMessage());
        }
    }

    /**
     * Send overdue reminders
     * URL: POST http://localhost:8084/api/notifications/overdue-reminders
     */
    @PostMapping("/overdue-reminders")
    public ResponseEntity<?> sendOverdueReminders() {
        logger.info("⚠️ Sending overdue reminders...");
        try {
            List<NotificationResponseDto> responses = notificationService.sendOverdueReminders();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("❌ Failed to send overdue reminders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send overdue reminders: " + e.getMessage());
        }
    }

    /**
     * Send payment confirmation
     * URL: POST http://localhost:8084/api/notifications/payment-confirmation/{studentId}
     */
    @PostMapping("/payment-confirmation/{studentId}")
    public ResponseEntity<?> sendPaymentConfirmation(
            @PathVariable Long studentId,
            @RequestParam Integer amount,
            @RequestParam String transactionId) {
        logger.info("💰 Sending payment confirmation for student ID: {}, Amount: ₹{}", studentId, amount);
        try {
            NotificationResponseDto response = notificationService.sendPaymentConfirmation(studentId, amount, transactionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to send payment confirmation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to send payment confirmation: " + e.getMessage());
        }
    }

    // ============= 🔍 GET NOTIFICATIONS =============

    /**
     * Get all notifications for a student
     * URL: GET http://localhost:8084/api/notifications/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentNotifications(@PathVariable Long studentId) {
        logger.info("🔍 Getting notifications for student ID: {}", studentId);
        try {
            List<NotificationResponseDto> notifications = notificationService.getStudentNotifications(studentId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("❌ Error getting notifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get notifications: " + e.getMessage());
        }
    }

    /**
     * Get unread notifications for a student
     * URL: GET http://localhost:8084/api/notifications/student/{studentId}/unread
     */
    @GetMapping("/student/{studentId}/unread")
    public ResponseEntity<?> getUnreadNotifications(@PathVariable Long studentId) {
        logger.info("🔍 Getting unread notifications for student ID: {}", studentId);
        try {
            List<NotificationResponseDto> notifications = notificationService.getUnreadNotifications(studentId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("❌ Error getting unread notifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get unread notifications: " + e.getMessage());
        }
    }

    // ============= ✅ MARK NOTIFICATIONS =============

    /**
     * Mark notification as read
     * URL: PATCH http://localhost:8084/api/notifications/{notificationId}/read
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        logger.info("✅ Marking notification as read: {}", notificationId);
        try {
            NotificationResponseDto response = notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error marking notification as read: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to mark notification as read: " + e.getMessage());
        }
    }

    /**
     * Mark all notifications as read for a student
     * URL: PATCH http://localhost:8084/api/notifications/student/{studentId}/read-all
     */
    @PatchMapping("/student/{studentId}/read-all")
    public ResponseEntity<?> markAllAsRead(@PathVariable Long studentId) {
        logger.info("✅ Marking all notifications as read for student: {}", studentId);
        try {
            notificationService.markAllAsRead(studentId);
            return ResponseEntity.ok("All notifications marked as read successfully");
        } catch (Exception e) {
            logger.error("❌ Error marking all notifications as read: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to mark all as read: " + e.getMessage());
        }
    }

    // ============= ⏰ TRIGGER SCHEDULED =============

    /**
     * Trigger scheduled reminders manually (for testing)
     * URL: POST http://localhost:8084/api/notifications/trigger-scheduled
     */
    @PostMapping("/trigger-scheduled")
    public ResponseEntity<?> triggerScheduledReminders() {
        logger.info("⏰ Manually triggering scheduled reminders...");
        try {
            notificationService.scheduleDailyFeeReminders();
            return ResponseEntity.ok("Scheduled reminders triggered successfully");
        } catch (Exception e) {
            logger.error("❌ Error triggering scheduled reminders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to trigger scheduled reminders: " + e.getMessage());
        }
    }

    // ============= 🆕 NEW ENDPOINTS FOR DUE DATE INSTALLMENTS =============

    /**
     * Get all due installments for a student (separate from total fees)
     * URL: GET http://localhost:8084/api/notifications/due-installments/{studentId}
     */
    @GetMapping("/due-installments/{studentId}")
    public ResponseEntity<?> getDueInstallments(@PathVariable Long studentId) {
        logger.info("🔍 Getting due installments for student ID: {}", studentId);
        try {
            List<Map<String, Object>> dueInstallments = notificationService.getDueInstallments(studentId);
            return ResponseEntity.ok(dueInstallments);
        } catch (Exception e) {
            logger.error("❌ Error getting due installments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get due installments: " + e.getMessage());
        }
    }

    /**
     * Get total due amount (separate from total fees)
     * URL: GET http://localhost:8084/api/notifications/total-due-amount/{studentId}
     */
    @GetMapping("/total-due-amount/{studentId}")
    public ResponseEntity<?> getTotalDueAmount(@PathVariable Long studentId) {
        logger.info("🔍 Getting total due amount for student ID: {}", studentId);
        try {
            Map<String, Object> dueAmount = notificationService.getTotalDueAmount(studentId);
            return ResponseEntity.ok(dueAmount);
        } catch (Exception e) {
            logger.error("❌ Error getting total due amount: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get total due amount: " + e.getMessage());
        }
    }

    /**
     * Manually trigger due date check (for testing)
     * URL: POST http://localhost:8084/api/notifications/check-due-dates
     */
    @PostMapping("/check-due-dates")
    public ResponseEntity<?> checkDueDates() {
        logger.info("⏰ Manually triggering due date check...");
        try {
            notificationService.checkDueDateInstallments();
            return ResponseEntity.ok("Due date check completed successfully");
        } catch (Exception e) {
            logger.error("❌ Error in due date check: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to check due dates: " + e.getMessage());
        }
    }
}