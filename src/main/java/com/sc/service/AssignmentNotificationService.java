package com.sc.service;

import com.sc.entity.AssignmentEntity;
import com.sc.entity.AssignmentNotificationEntity;
import java.util.List;

public interface AssignmentNotificationService {

    // ============= 📨 NOTIFICATION ON ASSIGNMENT CREATION =============

    /**
     * Sends notifications to students about a newly created assignment
     * @param assignment The newly created assignment
     * @param studentIds List of student IDs to notify
     */
    void notifyStudentsAboutNewAssignment(AssignmentEntity assignment, List<Long> studentIds);


    // ============= ⏰ SCHEDULED REMINDERS =============

    /**
     * Sends reminders for assignments due in the next 24 hours
     * Scheduled to run every hour
     */
    void sendDueDateReminders();

    /**
     * Sends reminders for overdue assignments
     * Scheduled to run daily at 9 AM
     */
    void sendOverdueReminders();

    /**
     * Sends weekly summary of pending assignments to all students
     * Scheduled to run every Monday at 8 AM
     */
    void sendWeeklyPendingSummary();


    // ============= 📊 PUBLIC METHODS =============

    /**
     * Marks assignment notifications as read for a specific student and assignment
     * @param studentId ID of the student
     * @param assignmentId ID of the assignment
     */
    void markAssignmentNotificationAsRead(Long studentId, Long assignmentId);

    /**
     * Marks assignment action as completed for a specific student and assignment
     * @param studentId ID of the student
     * @param assignmentId ID of the assignment
     */
    void markAssignmentActionCompleted(Long studentId, Long assignmentId);

    /**
     * Gets the count of unread notifications for a student
     * @param studentId ID of the student
     * @return Number of unread notifications
     */
    long getUnreadNotificationCount(Long studentId);


    // ============= 🎯 ADDITIONAL RECOMMENDED METHODS =============

    /**
     * Gets all notifications for a specific student
     * @param studentId ID of the student
     * @return List of notifications for the student
     */
    // List<AssignmentNotificationEntity> getStudentNotifications(Long studentId);

    /**
     * Gets unread notifications for a specific student
     * @param studentId ID of the student
     * @return List of unread notifications for the student
     */
    // List<AssignmentNotificationEntity> getUnreadStudentNotifications(Long studentId);

    /**
     * Deletes old notifications (for cleanup purposes)
     * @param daysOld Delete notifications older than specified days
     */
    // void deleteOldNotifications(int daysOld);

    /**
     * Sends manual reminder for a specific assignment to a specific student
     * @param assignmentId ID of the assignment
     * @param studentId ID of the student
     * @param reminderType Type of reminder (DUE_SOON, OVERDUE, etc.)
     */
    // void sendManualReminder(Long assignmentId, Long studentId, String reminderType);
}