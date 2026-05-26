package com.sc.service;

import com.sc.dto.response.NotificationResponseDto;
import java.util.List;
import java.util.Map;

public interface NotificationService {

    // ============= 🎯 SEND REMINDERS ONLY TO PENDING STUDENTS =============

    /**
     * Send fee reminder to a specific student if they have pending fees
     * This will send both IN-APP notification and EMAIL
     */
    NotificationResponseDto sendFeeReminderToStudent(Long studentId);

    /**
     * Send fee reminders to ALL students with pending fees
     * This will send both IN-APP notification and EMAIL to all pending students
     */
    List<NotificationResponseDto> sendFeeRemindersToAllPendingStudents();

    /**
     * Send reminders to students with overdue installments
     */
    List<NotificationResponseDto> sendOverdueReminders();

    /**
     * Send payment confirmation after successful payment
     */
    NotificationResponseDto sendPaymentConfirmation(Long studentId, Integer amount, String transactionId);

    /**
     * Get all notifications for a student
     */
    List<NotificationResponseDto> getStudentNotifications(Long studentId);

    /**
     * Get unread notifications for a student
     */
    List<NotificationResponseDto> getUnreadNotifications(Long studentId);

    /**
     * Mark notification as read
     */
    NotificationResponseDto markAsRead(Long notificationId);

    /**
     * Mark all notifications as read for a student
     */
    void markAllAsRead(Long studentId);

    /**
     * Schedule daily fee reminders (automated)
     */
    void scheduleDailyFeeReminders();

    // ============= 🆕 NEW METHODS FOR DUE DATE INSTALLMENTS =============

    /**
     * Check for installments where due date has passed and send reminders
     * This runs automatically every hour via scheduler
     */
    void checkDueDateInstallments();

    /**
     * Get all due installments for a student (separate from total fees)
     * Returns list of installments with due date, amount, late fee, etc.
     *
     * @param studentId Student ID
     * @return List of due installments with details
     */
    List<Map<String, Object>> getDueInstallments(Long studentId);

    /**
     * Get total due amount (separate from total fees)
     * Returns total due amount, late fee, and grand total
     *
     * @param studentId Student ID
     * @return Map containing totalDueAmount, totalLateFee, grandTotal, etc.
     */
    Map<String, Object> getTotalDueAmount(Long studentId);
}