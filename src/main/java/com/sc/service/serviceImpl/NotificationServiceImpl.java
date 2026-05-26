package com.sc.service.serviceImpl;

import com.sc.dto.response.NotificationResponseDto;
import com.sc.dto.response.FeesResponseDto;
import com.sc.entity.*;
import com.sc.repository.*;
import com.sc.service.NotificationService;
import com.sc.service.FeesService;
import com.sc.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FeesRepository feesRepository;

    @Autowired
    private FeesService feesService;

    @Autowired
    private EmailService emailService;

    // ============= 🎯 SEND REMINDER TO SPECIFIC STUDENT (ONLY IF PENDING) =============

    @Override
    @Transactional
    public NotificationResponseDto sendFeeReminderToStudent(Long studentId) {
        logger.info("🔍 Checking pending fees for student ID: {}", studentId);

        try {
            // 1. Get student
            StudentEntity student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

            // 2. Get fees for student
            FeesResponseDto feesDto = feesService.getFeesByStudentId(studentId);

            // 3. Check if fees exist AND have pending amount
            if (feesDto == null) {
                logger.info("❌ No fees record found for student ID: {}. No reminder sent.", studentId);
                throw new RuntimeException("No fees record found for this student");
            }

            if (feesDto.isFullyPaid()) {
                logger.info("✅ Student ID: {} has NO pending fees. Fully paid. No reminder sent.", studentId);
                throw new RuntimeException("Student has no pending fees. Fees are fully paid.");
            }

            // 4. Get full FeesEntity for email
            FeesEntity feesEntity = feesRepository.findByStudentId(studentId)
                    .orElseThrow(() -> new RuntimeException("Fees entity not found"));

            // 5. Only reach here if fees are PENDING
            logger.info("💰 Student ID: {} has pending fees of ₹{}. Sending reminder...",
                    studentId, feesDto.getRemainingFees());

            // 6. Create IN-APP notification
            NotificationEntity notification = createFeeReminderNotification(student, feesDto);
            NotificationEntity savedNotification = notificationRepository.save(notification);
            savedNotification.setStatus("SENT");
            savedNotification.setSentDate(new Date());
            notificationRepository.save(savedNotification);

            // 7. Send EMAIL notification
            emailService.sendFeeReminderEmail(student, feesEntity);

            logger.info("✅✅ Fee reminder sent successfully to student: {} (Pending: ₹{}) - [IN-APP + EMAIL]",
                    student.getStudentId(), feesDto.getRemainingFees());

            return convertToDto(savedNotification);

        } catch (RuntimeException e) {
            logger.info("⏭️ Reminder not sent: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("❌ Error sending fee reminder: {}", e.getMessage());
            throw new RuntimeException("Failed to send fee reminder: " + e.getMessage());
        }
    }





    // ============= 🎯 SEND REMINDERS TO ALL PENDING STUDENTS =============

    @Override
    @Transactional
    public List<NotificationResponseDto> sendFeeRemindersToAllPendingStudents() {
        logger.info("🔍🔍 SEARCHING FOR ALL STUDENTS WITH PENDING FEES...");

        try {
            // 1. Get ALL students with pending fees
            List<FeesResponseDto> pendingFeesList = feesService.getAllPendingFees();

            if (pendingFeesList.isEmpty()) {
                logger.info("✅✅ NO students with pending fees found. No reminders sent.");
                return new ArrayList<>();
            }

            logger.info("📊📊 FOUND {} STUDENTS WITH PENDING FEES", pendingFeesList.size());

            // 2. Send reminder to each pending student
            List<NotificationResponseDto> sentNotifications = new ArrayList<>();
            int successCount = 0;
            int failedCount = 0;

            for (FeesResponseDto fees : pendingFeesList) {
                try {
                    NotificationResponseDto notification = sendFeeReminderToStudent(fees.getStudentId());
                    sentNotifications.add(notification);
                    successCount++;
                } catch (Exception e) {
                    failedCount++;
                    logger.error("❌ Failed to send reminder to student ID {}: {}",
                            fees.getStudentId(), e.getMessage());
                }
            }

            logger.info("✅✅ BULK REMINDERS COMPLETED - Success: {}, Failed: {}",
                    successCount, failedCount);

            return sentNotifications;

        } catch (Exception e) {
            logger.error("❌ Error sending bulk reminders: {}", e.getMessage());
            throw new RuntimeException("Failed to send bulk reminders: " + e.getMessage());
        }
    }

    // ============= 🎯 SEND OVERDUE REMINDERS =============

    @Override
    @Transactional
    public List<NotificationResponseDto> sendOverdueReminders() {
        logger.info("🔍🔍 SEARCHING FOR STUDENTS WITH OVERDUE INSTALLMENTS...");

        try {
            List<FeesResponseDto> pendingFeesList = feesService.getAllPendingFees();
            List<NotificationResponseDto> sentNotifications = new ArrayList<>();

            for (FeesResponseDto fees : pendingFeesList) {
                StudentEntity student = studentRepository.findById(fees.getStudentId()).orElse(null);
                if (student == null) continue;

                FeesEntity feesEntity = feesRepository.findByStudentId(fees.getStudentId()).orElse(null);
                if (feesEntity == null) continue;

                List<Installment> overdueInstallments = feesEntity.getInstallmentsList().stream()
                        .filter(i -> !"PAID".equalsIgnoreCase(i.getStatus()))
                        .filter(i -> i.getDueDate() != null && LocalDate.now().isAfter(i.getDueDate()))
                        .collect(Collectors.toList());

                for (Installment installment : overdueInstallments) {
                    try {
                        // Create IN-APP notification for overdue
                        NotificationEntity notification = createOverdueNotification(student, fees, installment);
                        NotificationEntity saved = notificationRepository.save(notification);
                        saved.setStatus("SENT");
                        saved.setSentDate(new Date());
                        notificationRepository.save(saved);

                        // Send EMAIL for overdue
                        emailService.sendOverdueReminderEmail(student, feesEntity, installment.getInstallmentId());

                        sentNotifications.add(convertToDto(saved));
                        logger.info("✅ Overdue reminder sent to student: {} for installment #{}",
                                student.getStudentId(), installment.getInstallmentId());

                    } catch (Exception e) {
                        logger.error("❌ Failed to send overdue reminder: {}", e.getMessage());
                    }
                }
            }

            logger.info("✅✅ Sent {} overdue reminders", sentNotifications.size());
            return sentNotifications;

        } catch (Exception e) {
            logger.error("❌ Error sending overdue reminders: {}", e.getMessage());
            throw new RuntimeException("Failed to send overdue reminders: " + e.getMessage());
        }
    }

    // ============= 🎯 SEND PAYMENT CONFIRMATION =============

    @Override
    @Transactional
    public NotificationResponseDto sendPaymentConfirmation(Long studentId, Integer amount, String transactionId) {
        logger.info("💰 Sending payment confirmation for student ID: {}, Amount: ₹{}", studentId, amount);

        try {
            StudentEntity student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            FeesEntity fees = feesRepository.findByStudentId(studentId)
                    .orElseThrow(() -> new RuntimeException("Fees record not found"));

            FeesResponseDto feesDto = feesService.getFeesByStudentId(studentId);

            // Create IN-APP notification
            NotificationEntity notification = createPaymentConfirmationNotification(student, feesDto, amount, transactionId);
            NotificationEntity saved = notificationRepository.save(notification);
            saved.setStatus("SENT");
            saved.setSentDate(new Date());
            notificationRepository.save(saved);

            // Send EMAIL confirmation
            emailService.sendPaymentConfirmationEmail(student, fees, amount, transactionId);

            logger.info("✅ Payment confirmation sent to student: {}", student.getStudentId());

            return convertToDto(saved);

        } catch (Exception e) {
            logger.error("❌ Error sending payment confirmation: {}", e.getMessage());
            throw new RuntimeException("Failed to send payment confirmation: " + e.getMessage());
        }
    }

    // ============= 🔍 GET NOTIFICATIONS =============

    @Override
    public List<NotificationResponseDto> getStudentNotifications(Long studentId) {
        try {
            List<NotificationEntity> notifications = notificationRepository.findByStudentId(studentId);
            return notifications.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting notifications for student {}: {}", studentId, e.getMessage());
            throw new RuntimeException("Failed to get notifications: " + e.getMessage());
        }
    }

    @Override
    public List<NotificationResponseDto> getUnreadNotifications(Long studentId) {
        try {
            StudentEntity student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            List<NotificationEntity> notifications =
                    notificationRepository.findByStudentAndStatus(student, "SENT");

            return notifications.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting unread notifications: {}", e.getMessage());
            throw new RuntimeException("Failed to get unread notifications: " + e.getMessage());
        }
    }

    // ============= ✅ MARK NOTIFICATIONS =============

    @Override
    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId) {
        try {
            NotificationEntity notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found"));

            notification.setStatus("READ");
            notification.setReadAt(new Date());

            NotificationEntity updated = notificationRepository.save(notification);
            return convertToDto(updated);

        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", e.getMessage());
            throw new RuntimeException("Failed to mark notification as read: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long studentId) {
        try {
            List<NotificationEntity> notifications = notificationRepository.findByStudentId(studentId);
            notifications.forEach(n -> {
                n.setStatus("READ");
                n.setReadAt(new Date());
            });
            notificationRepository.saveAll(notifications);
            logger.info("Marked all notifications as read for student: {}", studentId);
        } catch (Exception e) {
            logger.error("Error marking all notifications as read: {}", e.getMessage());
            throw new RuntimeException("Failed to mark all as read: " + e.getMessage());
        }
    }

    // ============= ⏰ SCHEDULED TASKS =============

    @Override
    @Scheduled(cron = "0 0 9 * * ?")    // Every day at 9 AM
    @Transactional
    public void scheduleDailyFeeReminders() {
        logger.info("⏰⏰ RUNNING SCHEDULED DAILY FEE REMINDERS...");
        try {
            sendFeeRemindersToAllPendingStudents();
            sendOverdueReminders();
            logger.info("✅✅ Daily fee reminders completed successfully");
        } catch (Exception e) {
            logger.error("❌ Error in scheduled fee reminders: {}", e.getMessage());
        }
    }

    // ============= ⏰ NEW: DUE DATE INSTALLMENT SCHEDULER =============

    /**
     * Check for installments where due date has passed and send reminders
     * Runs every hour to check for newly expired due dates
     */
    @Scheduled(cron = "0 0 * * * ?") // Runs every hour at minute 0
    @Transactional
    public void checkDueDateInstallments() {
        logger.info("⏰ CHECKING FOR INSTALLMENTS WITH PASSED DUE DATES...");

        try {
            // Get current date
            LocalDate today = LocalDate.now();
            logger.info("Current date: {}", today);

            // Get all fees records
            List<FeesEntity> allFees = feesRepository.findAll();
            int totalDueInstallments = 0;
            int emailsSent = 0;

            for (FeesEntity fees : allFees) {
                StudentEntity student = fees.getStudent();
                if (student == null) continue;

                // Find installments with due date passed and NOT paid
                List<Installment> dueInstallments = fees.getInstallmentsList().stream()
                        .filter(i -> !"PAID".equalsIgnoreCase(i.getStatus()))
                        .filter(i -> i.getDueDate() != null)
                        .filter(i -> i.getDueDate().isBefore(today) || i.getDueDate().isEqual(today))
                        .filter(i -> !isReminderSentToday(student.getStdId(), i.getInstallmentId()))
                        .collect(Collectors.toList());

                if (!dueInstallments.isEmpty()) {
                    totalDueInstallments += dueInstallments.size();
                    logger.info("Found {} due installments for student: {}",
                            dueInstallments.size(), student.getStudentId());

                    for (Installment installment : dueInstallments) {
                        try {
                            // Calculate days overdue
                            int daysOverdue = calculateDaysOverdue(installment.getDueDate());

                            // Check if already overdue (more than 1 day)
                            if (daysOverdue > 0) {
                                // Send OVERDUE reminder
                                sendOverdueReminderForInstallment(student, fees, installment);
                                logger.info("✅ Overdue reminder sent for installment #{} ({} days overdue)",
                                        installment.getInstallmentId(), daysOverdue);
                            } else {
                                // Send DUE TODAY reminder
                                sendDueTodayReminder(student, fees, installment);
                                logger.info("✅ Due today reminder sent for installment #{}",
                                        installment.getInstallmentId());
                            }
                            emailsSent++;

                        } catch (Exception e) {
                            logger.error("❌ Failed to send reminder for installment #{}: {}",
                                    installment.getInstallmentId(), e.getMessage());
                        }
                    }
                }
            }

            logger.info("✅✅ DUE DATE CHECK COMPLETED - Found: {}, Emails Sent: {}",
                    totalDueInstallments, emailsSent);

        } catch (Exception e) {
            logger.error("❌ Error in due date installment checker: {}", e.getMessage());
        }
    }

    /**
     * Check if reminder already sent today for this installment
     */
    private boolean isReminderSentToday(Long studentId, Long installmentId) {
        LocalDate today = LocalDate.now();
        Date startOfDay = convertToDate(today);
        Date endOfDay = convertToDate(today.plusDays(1));

        List<NotificationEntity> todaysReminders = notificationRepository
                .findByStudentIdAndInstallmentIdAndDateRange(
                        studentId, installmentId, startOfDay, endOfDay);

        return !todaysReminders.isEmpty();
    }

    /**
     * Send reminder for installment due today
     */
    private void sendDueTodayReminder(StudentEntity student, FeesEntity fees, Installment installment) {
        // Create IN-APP notification for due today
        NotificationEntity notification = new NotificationEntity();
        notification.setStudent(student);
        notification.setTitle("⏰ Installment Due Today");

        String message = String.format(
                "Dear %s %s,\n\nYour installment #%d of ₹%d is due today (%s).\n" +
                        "Please pay today to avoid late fees.\n\n" +
                        "Amount Due: ₹%d\n" +
                        "Thank you,\nSchool Administration",
                student.getFirstName(),
                student.getLastName(),
                installment.getInstallmentId(),
                installment.getAmount(),
                formatDate(installment.getDueDate()),
                installment.getDueAmount()
        );

        notification.setMessage(message);
        notification.setNotificationType("DUE_TODAY_REMINDER");
        notification.setChannel("IN_APP");
        notification.setAmountDue(installment.getDueAmount());
        notification.setDueDate(convertToDate(installment.getDueDate()));
        notification.setInstallmentId(installment.getInstallmentId());
        notification.setAcademicYear(fees.getAcademicYear());

        NotificationEntity saved = notificationRepository.save(notification);
        saved.setStatus("SENT");
        saved.setSentDate(new Date());
        notificationRepository.save(saved);

        // Send EMAIL for due today
        emailService.sendInstallmentDueEmail(student, fees, installment.getInstallmentId());
    }

    /**
     * Send overdue reminder for installment (reuse existing method but ensure email is sent)
     */
    private void sendOverdueReminderForInstallment(StudentEntity student, FeesEntity fees, Installment installment) {
        // Get fees DTO for the method
        FeesResponseDto feesDto = feesService.getFeesByStudentId(student.getStdId());

        // Create overdue notification using existing method
        NotificationEntity notification = createOverdueNotification(student, feesDto, installment);
        NotificationEntity saved = notificationRepository.save(notification);
        saved.setStatus("SENT");
        saved.setSentDate(new Date());
        notificationRepository.save(saved);

        // Send EMAIL for overdue
        emailService.sendOverdueReminderEmail(student, fees, installment.getInstallmentId());
    }

    // ============= 🆕 NEW METHODS TO GET DUE INSTALLMENTS SEPARATELY =============

    /**
     * Get all due installments (separate from total amount)
     */
    @Override
    public List<Map<String, Object>> getDueInstallments(Long studentId) {
        logger.info("🔍 Getting due installments for student ID: {}", studentId);

        try {
            StudentEntity student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            FeesEntity fees = feesRepository.findByStudentId(studentId)
                    .orElse(null);

            if (fees == null) {
                return new ArrayList<>();
            }

            LocalDate today = LocalDate.now();
            List<Map<String, Object>> dueInstallments = new ArrayList<>();

            for (Installment inst : fees.getInstallmentsList()) {
                if (!"PAID".equalsIgnoreCase(inst.getStatus())) {
                    Map<String, Object> dueInfo = new HashMap<>();
                    dueInfo.put("installmentId", inst.getInstallmentId());
                    dueInfo.put("amount", inst.getAmount());
                    dueInfo.put("dueDate", formatDate(inst.getDueDate()));
                    dueInfo.put("dueAmount", inst.getDueAmount());
                    dueInfo.put("status", inst.getStatus());

                    if (inst.getDueDate() != null) {
                        if (inst.getDueDate().isBefore(today)) {
                            int daysOverdue = calculateDaysOverdue(inst.getDueDate());
                            dueInfo.put("daysOverdue", daysOverdue);
                            dueInfo.put("lateFee", daysOverdue * 100);
                            dueInfo.put("totalDue", inst.getDueAmount() + (daysOverdue * 100));
                            dueInfo.put("isOverdue", true);
                        } else if (inst.getDueDate().isEqual(today)) {
                            dueInfo.put("daysRemaining", 0);
                            dueInfo.put("dueToday", true);
                            dueInfo.put("isOverdue", false);
                        } else {
                            long daysRemaining = today.until(inst.getDueDate()).getDays();
                            dueInfo.put("daysRemaining", daysRemaining);
                            dueInfo.put("isOverdue", false);
                        }
                    }

                    dueInstallments.add(dueInfo);
                }
            }

            return dueInstallments;

        } catch (Exception e) {
            logger.error("Error getting due installments: {}", e.getMessage());
            throw new RuntimeException("Failed to get due installments: " + e.getMessage());
        }
    }

    /**
     * Get total due amount (separate from total fees)
     */
    @Override
    public Map<String, Object> getTotalDueAmount(Long studentId) {
        logger.info("🔍 Getting total due amount for student ID: {}", studentId);

        try {
            FeesEntity fees = feesRepository.findByStudentId(studentId)
                    .orElse(null);

            Map<String, Object> dueInfo = new HashMap<>();
            dueInfo.put("studentId", studentId);

            if (fees == null) {
                dueInfo.put("totalDueAmount", 0);
                dueInfo.put("totalLateFee", 0);
                dueInfo.put("grandTotal", 0);
                dueInfo.put("dueInstallments", 0);
                dueInfo.put("overdueInstallments", 0);
                return dueInfo;
            }

            LocalDate today = LocalDate.now();
            int totalDueAmount = 0;
            int totalLateFee = 0;
            int dueCount = 0;
            int overdueCount = 0;

            for (Installment inst : fees.getInstallmentsList()) {
                if (!"PAID".equalsIgnoreCase(inst.getStatus())) {
                    dueCount++;
                    totalDueAmount += inst.getDueAmount();

                    if (inst.getDueDate() != null && inst.getDueDate().isBefore(today)) {
                        int daysOverdue = calculateDaysOverdue(inst.getDueDate());
                        int lateFee = daysOverdue * 100;
                        totalLateFee += lateFee;
                        overdueCount++;
                    }
                }
            }

            dueInfo.put("totalDueAmount", totalDueAmount);
            dueInfo.put("totalLateFee", totalLateFee);
            dueInfo.put("grandTotal", totalDueAmount + totalLateFee);
            dueInfo.put("dueInstallments", dueCount);
            dueInfo.put("overdueInstallments", overdueCount);

            return dueInfo;

        } catch (Exception e) {
            logger.error("Error getting total due amount: {}", e.getMessage());
            throw new RuntimeException("Failed to get total due amount: " + e.getMessage());
        }
    }

    // ============= 🛠️ PRIVATE HELPER METHODS =============

    private NotificationEntity createFeeReminderNotification(StudentEntity student, FeesResponseDto fees) {
        NotificationEntity notification = new NotificationEntity();
        notification.setStudent(student);
        notification.setTitle("Fee Payment Reminder");

        String message = String.format(
                "Dear %s %s,\n\nYou have pending fees of ₹%d for the academic year %s.\n" +
                        "Please pay the remaining amount to avoid late fees.\n\n" +
                        "Total Fees: ₹%d\nPaid Amount: ₹%d\nRemaining: ₹%d\n\n" +
                        "Thank you,\nSchool Administration",
                student.getFirstName(),
                student.getLastName(),
                fees.getRemainingFees(),
                fees.getAcademicYear(),
                fees.getTotalFees(),
                fees.getTotalPaidAmount(),
                fees.getRemainingFees()
        );

        notification.setMessage(message);
        notification.setNotificationType("FEE_REMINDER");
        notification.setChannel("IN_APP");
        notification.setAmountDue(fees.getRemainingFees());
        notification.setAcademicYear(fees.getAcademicYear());

        return notification;
    }

    private NotificationEntity createOverdueNotification(StudentEntity student, FeesResponseDto fees, Installment installment) {
        NotificationEntity notification = new NotificationEntity();
        notification.setStudent(student);
        notification.setTitle("⚠️ URGENT: Payment Overdue");

        int daysOverdue = calculateDaysOverdue(installment.getDueDate());
        int lateFee = daysOverdue * 100;

        String message = String.format(
                "Dear %s %s,\n\nYour installment #%d of ₹%d was due on %s and is now %d days overdue.\n" +
                        "Late fee of ₹100 per day has been applied. Total due now: ₹%d.\n\n" +
                        "Please pay immediately to avoid further charges.\n\n" +
                        "Thank you,\nSchool Administration",
                student.getFirstName(),
                student.getLastName(),
                installment.getInstallmentId(),
                installment.getAmount(),
                formatDate(installment.getDueDate()),
                daysOverdue,
                installment.getDueAmount() + lateFee
        );

        notification.setMessage(message);
        notification.setNotificationType("OVERDUE_REMINDER");
        notification.setChannel("IN_APP");
        notification.setAmountDue(installment.getDueAmount() + lateFee);
        notification.setDueDate(convertToDate(installment.getDueDate()));
        notification.setInstallmentId(installment.getInstallmentId());
        notification.setAcademicYear(fees.getAcademicYear());

        return notification;
    }

    private NotificationEntity createPaymentConfirmationNotification(StudentEntity student, FeesResponseDto fees,
                                                                     Integer amount, String transactionId) {
        NotificationEntity notification = new NotificationEntity();
        notification.setStudent(student);
        notification.setTitle("✅ Payment Confirmed");

        String message = String.format(
                "Dear %s %s,\n\nYour payment of ₹%d has been received successfully.\n" +
                        "Transaction ID: %s\nDate: %s\n\n" +
                        "Remaining Fees: ₹%d\n\n" +
                        "Thank you for your payment!\nSchool Administration",
                student.getFirstName(),
                student.getLastName(),
                amount,
                transactionId,
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                fees.getRemainingFees() - amount
        );

        notification.setMessage(message);
        notification.setNotificationType("PAYMENT_CONFIRMATION");
        notification.setChannel("IN_APP");
        notification.setAmountDue(amount);
        notification.setTransactionId(transactionId);
        notification.setAcademicYear(fees.getAcademicYear());

        return notification;
    }

    private NotificationResponseDto convertToDto(NotificationEntity entity) {
        NotificationResponseDto dto = new NotificationResponseDto();

        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudent().getStdId());
        dto.setStudentName(entity.getStudent().getFirstName() + " " + entity.getStudent().getLastName());
        dto.setStudentRollNumber(entity.getStudent().getStudentRollNumber());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setNotificationType(entity.getNotificationType());
        dto.setChannel(entity.getChannel());
        dto.setStatus(entity.getStatus());
        dto.setSentDate(entity.getSentDate());
        dto.setDueDate(entity.getDueDate());
        dto.setAmountDue(entity.getAmountDue());
        dto.setInstallmentId(entity.getInstallmentId());
        dto.setAcademicYear(entity.getAcademicYear());
        dto.setTransactionId(entity.getTransactionId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setReadAt(entity.getReadAt());

        return dto;
    }

    private Date convertToDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    private int calculateDaysOverdue(LocalDate dueDate) {
        if (dueDate == null) return 0;
        return (int) (LocalDate.now().toEpochDay() - dueDate.toEpochDay());
    }
}