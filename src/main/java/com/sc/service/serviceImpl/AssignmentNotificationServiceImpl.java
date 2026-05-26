package com.sc.service.serviceImpl;

import com.sc.entity.AssignmentEntity;
import com.sc.entity.AssignmentNotificationEntity;
import com.sc.entity.StudentEntity;
import com.sc.repository.AssignmentNotificationRepository;
import com.sc.repository.AssignmentRepository;
import com.sc.repository.StudentRepository;
import com.sc.repository.SubmissionRepository;
import com.sc.service.AssignmentNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssignmentNotificationServiceImpl implements AssignmentNotificationService {

    @Autowired
    private AssignmentNotificationRepository assignmentNotificationRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    // ============= LOGGING METHODS =============

    private void logInfo(String message) {
//        System.out.println("[INFO] " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " - " + message);
    }

    private void logError(String message, Exception e) {
//        System.err.println("[ERROR] " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " - " + message);
//        if (e != null) {
//            e.printStackTrace();
//        }
    }

    private void logError(String message) {
//        System.err.println("[ERROR] " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " - " + message);
    }

    private void logDebug(String message) {
//        System.out.println("[DEBUG] " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " - " + message);
    }

    private void logWarn(String message) {
//        System.out.println("[WARN] " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " - " + message);
    }

    // ============= 📨 NOTIFICATION ON ASSIGNMENT CREATION =============

    @Override
    @Transactional
    public void notifyStudentsAboutNewAssignment(AssignmentEntity assignment, List<Long> studentIds) {
        logInfo("📨 Sending new assignment notifications to " + studentIds.size() + " students");

        if (studentIds == null || studentIds.isEmpty()) {
            logWarn("No students to notify for assignment: " + assignment.getAssignmentId());
            return;
        }

        int successCount = 0;
        int failureCount = 0;

        for (Long studentId : studentIds) {
            try {
                // 🔴 FIXED: Use Optional properly
                Optional<StudentEntity> studentOpt = studentRepository.findById(studentId);

                if (!studentOpt.isPresent()) {
                    logWarn("Student not found with ID: " + studentId);
                    failureCount++;
                    continue;
                }

                // ✅ Get the StudentEntity from Optional
                StudentEntity student = studentOpt.get();

                // Check if already notified
                List<AssignmentNotificationEntity> existing = assignmentNotificationRepository
                        .findByStudentAndAssignment(studentId, assignment.getAssignmentId());

                if (!existing.isEmpty()) {
                    logDebug("Student " + studentId + " already notified about assignment " + assignment.getAssignmentId());
                    continue;
                }

                // Create notification
                AssignmentNotificationEntity notification = createNotification(assignment, student);
                assignmentNotificationRepository.save(notification);
                successCount++;

            } catch (Exception e) {
                logError("Failed to send notification to student " + studentId + ": " + e.getMessage(), e);
                failureCount++;
            }
        }

        logInfo("✅ Notification summary - Sent: " + successCount + ", Failed: " + failureCount +
                ", Total: " + studentIds.size());
    }

    // Helper method to create notification
    private AssignmentNotificationEntity createNotification(AssignmentEntity assignment, StudentEntity student) {
        AssignmentNotificationEntity notification = new AssignmentNotificationEntity();
        notification.setStudent(student);
        notification.setTitle("📝 New Assignment: " + assignment.getTitle());
        notification.setMessage(String.format(
                "A new assignment '%s' has been created for %s %s. Due date: %s",
                assignment.getTitle(),
                assignment.getClassName(),
                assignment.getSection(),
                formatDate(assignment.getDueDate())
        ));
        notification.setNotificationType("ASSIGNMENT");
        notification.setChannel("IN_APP");
        notification.setPriority(getPriorityString(assignment.getPriority()));
        notification.setStatus("SENT");
        notification.setSentDate(new Date());

        notification.setAssignmentId(assignment.getAssignmentId());
        notification.setAssignmentTitle(assignment.getTitle());
        notification.setDueDate(Date.from(assignment.getDueDate().atZone(ZoneId.systemDefault()).toInstant()));

        notification.setActionRequired(true);

        Date expiryDate = Date.from(assignment.getDueDate().plusDays(7)
                .atZone(ZoneId.systemDefault()).toInstant());
        notification.setExpiryDate(expiryDate);

        notification.setActionUrl("/student/assignments/" + assignment.getAssignmentId());
        notification.setActionButtonText("View Assignment");

        return notification;
    }

    // ============= ⏰ SCHEDULED REMINDERS =============

    @Override
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendDueDateReminders() {
        logInfo("⏰ Checking for assignments due in next 24 hours...");
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrow = now.plusHours(24);

            List<AssignmentEntity> assignmentsDueSoon = assignmentRepository
                    .findByDueDateBetween(now, tomorrow);

            logInfo("Found " + assignmentsDueSoon.size() + " assignments due in next 24 hours");

            for (AssignmentEntity assignment : assignmentsDueSoon) {
                try {
                    sendRemindersForAssignment(assignment, "DUE_SOON");
                } catch (Exception e) {
                    logError("Error sending due reminders for assignment " +
                            assignment.getAssignmentId() + ": " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logError("Error in sendDueDateReminders", e);
        }
    }

    @Override
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendOverdueReminders() {
        logInfo("⚠️ Checking for overdue assignments...");
        try {
            LocalDateTime now = LocalDateTime.now();
            List<AssignmentEntity> overdueAssignments = assignmentRepository
                    .findOverdueAssignments(now);

            logInfo("Found " + overdueAssignments.size() + " overdue assignments");

            for (AssignmentEntity assignment : overdueAssignments) {
                try {
                    sendRemindersForAssignment(assignment, "OVERDUE");
                } catch (Exception e) {
                    logError("Error sending overdue reminders for assignment " +
                            assignment.getAssignmentId() + ": " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logError("Error in sendOverdueReminders", e);
        }
    }

    @Override
    @Scheduled(cron = "0 0 8 * * MON")
    @Transactional
    public void sendWeeklyPendingSummary() {
        logInfo("📊 Sending weekly pending assignments summary...");
        try {
            List<StudentEntity> allStudents = studentRepository.findAll();

            if (allStudents.isEmpty()) {
                logWarn("No students found to send weekly summary");
                return;
            }

            for (StudentEntity student : allStudents) {
                try {
                    sendPendingSummaryToStudent(student);
                } catch (Exception e) {
                    logError("Error sending summary to student " +
                            student.getStdId() + ": " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logError("Error in sendWeeklyPendingSummary", e);
        }
    }

    // ============= 🎯 HELPER METHODS FOR REMINDERS =============

    private void sendRemindersForAssignment(AssignmentEntity assignment, String reminderType) {
        try {
            List<StudentEntity> students = studentRepository.findStudentsByClassAndSection(
                    assignment.getClassName(),
                    assignment.getSection().equals("All Sections") ? null : assignment.getSection()
            );

            if (students.isEmpty()) {
                logWarn("No students found for assignment " + assignment.getAssignmentId());
                return;
            }

            List<Long> submittedStudentIds = submissionRepository
                    .findByAssignment_AssignmentId(assignment.getAssignmentId())
                    .stream()
                    .map(s -> s.getStudent().getStdId())
                    .collect(Collectors.toList());

            int reminderCount = 0;
            for (StudentEntity student : students) {
                if (!submittedStudentIds.contains(student.getStdId())) {
                    sendReminderToStudent(assignment, student, reminderType);
                    reminderCount++;
                }
            }
            logInfo("Sent " + reminderCount + " " + reminderType + " reminders for assignment " + assignment.getAssignmentId());
        } catch (Exception e) {
            logError("Error in sendRemindersForAssignment", e);
        }
    }

    private void sendReminderToStudent(AssignmentEntity assignment, StudentEntity student, String reminderType) {
        try {
            // Check if already sent in last 24 hours
            Date last24Hours = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
            List<AssignmentNotificationEntity> recent = assignmentNotificationRepository
                    .findByStudentAndAssignment(student.getStdId(), assignment.getAssignmentId())
                    .stream()
                    .filter(n -> n.getCreatedAt() != null && n.getCreatedAt().after(last24Hours))
                    .collect(Collectors.toList());

            if (!recent.isEmpty()) {
                return;
            }

            AssignmentNotificationEntity notification = new AssignmentNotificationEntity();
            notification.setStudent(student);

            if ("DUE_SOON".equals(reminderType)) {
                notification.setTitle("⏰ Assignment Due Soon: " + assignment.getTitle());
                notification.setMessage(String.format(
                        "Your assignment '%s' is due in less than 24 hours (Due: %s). Please submit soon!",
                        assignment.getTitle(),
                        formatDate(assignment.getDueDate())
                ));
                notification.setNotificationType("ASSIGNMENT_DUE_SOON");
            } else if ("OVERDUE".equals(reminderType)) {
                notification.setTitle("⚠️ Overdue Assignment: " + assignment.getTitle());
                notification.setMessage(String.format(
                        "Your assignment '%s' was due on %s. Please submit as soon as possible.",
                        assignment.getTitle(),
                        formatDate(assignment.getDueDate())
                ));
                notification.setNotificationType("ASSIGNMENT_OVERDUE");
                notification.setPriority("HIGH");
            } else {
                notification.setTitle("Reminder: " + assignment.getTitle());
                notification.setMessage(String.format(
                        "Please complete your assignment '%s' which is due on %s.",
                        assignment.getTitle(),
                        formatDate(assignment.getDueDate())
                ));
                notification.setNotificationType("ASSIGNMENT_REMINDER");
            }

            notification.setChannel("IN_APP");
            notification.setStatus("SENT");
            notification.setSentDate(new Date());

            notification.setAssignmentId(assignment.getAssignmentId());
            notification.setAssignmentTitle(assignment.getTitle());
            notification.setDueDate(Date.from(assignment.getDueDate().atZone(ZoneId.systemDefault()).toInstant()));

            notification.setActionRequired(true);
            notification.setActionUrl("/student/assignments/" + assignment.getAssignmentId());
            notification.setActionButtonText("Submit Now");

            assignmentNotificationRepository.save(notification);
            logDebug("Sent " + reminderType + " reminder to student " + student.getStdId());

        } catch (Exception e) {
            logError("Error sending reminder to student " + student.getStdId(), e);
        }
    }

    private void sendPendingSummaryToStudent(StudentEntity student) {
        try {
            // Get all published assignments for student's class and section
            List<AssignmentEntity> allAssignments = assignmentRepository
                    .findAllAssignmentsForStudent(
                            student.getCurrentClass(),
                            student.getSection()
                    );

            if (allAssignments.isEmpty()) {
                return;
            }

            // Get IDs of submitted assignments
            List<Long> submittedIds = submissionRepository
                    .findByStudent_StdId(student.getStdId())
                    .stream()
                    .map(s -> s.getAssignment().getAssignmentId())
                    .collect(Collectors.toList());

            // Filter for pending assignments (not submitted)
            List<AssignmentEntity> pendingAssignments = allAssignments.stream()
                    .filter(a -> !submittedIds.contains(a.getAssignmentId()))
                    .filter(a -> a.getDueDate() != null && a.getDueDate().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());

            if (pendingAssignments.isEmpty()) {
                return;
            }

            AssignmentNotificationEntity notification = new AssignmentNotificationEntity();
            notification.setStudent(student);
            notification.setTitle("📋 Weekly Summary: Pending Assignments");
            notification.setMessage(String.format(
                    "You have %d pending assignment(s) this week. Please check your dashboard for details.",
                    pendingAssignments.size()
            ));
            notification.setNotificationType("WEEKLY_SUMMARY");
            notification.setChannel("IN_APP");
            notification.setStatus("SENT");
            notification.setSentDate(new Date());
            notification.setActionRequired(false);
            notification.setActionUrl("/student/assignments/pending");
            notification.setActionButtonText("View Pending");

            assignmentNotificationRepository.save(notification);
            logDebug("Sent weekly summary to student " + student.getStdId() +
                    " (" + pendingAssignments.size() + " pending)");
        } catch (Exception e) {
            logError("Error sending weekly summary to student " + student.getStdId(), e);
        }
    }

    // ============= 📊 PUBLIC METHODS =============

    @Override
    @Transactional
    public void markAssignmentNotificationAsRead(Long studentId, Long assignmentId) {
        try {
            List<AssignmentNotificationEntity> notifications = assignmentNotificationRepository
                    .findByStudentAndAssignment(studentId, assignmentId);

            for (AssignmentNotificationEntity notification : notifications) {
                if (notification.getReadAt() == null) {
                    notification.setReadAt(new Date());
                    notification.setStatus("READ");
                    assignmentNotificationRepository.save(notification);
                }
            }
            logDebug("Marked notifications as read for student " + studentId + ", assignment " + assignmentId);
        } catch (Exception e) {
            logError("Error marking notifications as read", e);
        }
    }

    @Override
    @Transactional
    public void markAssignmentActionCompleted(Long studentId, Long assignmentId) {
        try {
            List<AssignmentNotificationEntity> notifications = assignmentNotificationRepository
                    .findByStudentAndAssignment(studentId, assignmentId);

            for (AssignmentNotificationEntity notification : notifications) {
                notification.setActionCompleted(true);
                assignmentNotificationRepository.save(notification);
            }
            logDebug("Marked action completed for student " + studentId + ", assignment " + assignmentId);
        } catch (Exception e) {
            logError("Error marking action completed", e);
        }
    }

    @Override
    public long getUnreadNotificationCount(Long studentId) {
        try {
            return assignmentNotificationRepository.countUnreadByStudentId(studentId);
        } catch (Exception e) {
            logError("Error getting unread notification count", e);
            return 0;
        }
    }

    // ============= 🔧 UTILITY METHODS =============

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        return dateTime.format(formatter);
    }

    private String getPriorityString(Object priority) {
        if (priority == null) return "MEDIUM";
        return priority.toString().toUpperCase();
    }
}