package com.sc.service.serviceImpl;

import com.sc.entity.NoticeEntity;
import com.sc.entity.StudentEntity;
import com.sc.entity.FeesEntity;
import com.sc.entity.Installment;
import com.sc.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${school.name:Kunash School}")
    private String schoolName;

    @Value("${app.base.url:http://localhost:8084}")
    private String baseUrl;

    // ── FIX 1: Frontend URL for email buttons (login page redirect) ──
    // Add this in application.properties:
    //   app.frontend.url=http://127.0.0.1:5500
    @Value("${app.frontend.url:http://127.0.0.1:5500}")
    private String frontendUrl;

    // ── FIX 2: Date formatter for Notice dates (java.util.Date) ──
    private static final SimpleDateFormat NOTICE_DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ============= 📧 FEE REMINDER EMAIL =============

    @Override
    public void sendFeeReminderEmail(StudentEntity student, FeesEntity fees) {
        try {
            String to = getStudentEmail(student);
            String subject = "Fee Payment Reminder - " + schoolName;
            String htmlContent = buildFeeReminderHtml(student, fees);
            sendEmail(to, subject, htmlContent);
            logger.info("Fee reminder email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send fee reminder email: {}", e.getMessage());
        }
    }

    // ============= 📧 INSTALLMENT DUE EMAIL =============

    @Override
    public void sendInstallmentDueEmail(StudentEntity student, FeesEntity fees, Long installmentId) {
        try {
            String to = getStudentEmail(student);
            String subject = "Installment Due Reminder - " + schoolName;

            Installment installment = fees.getInstallmentsList().stream()
                    .filter(i -> installmentId.equals(i.getInstallmentId()))
                    .findFirst()
                    .orElse(null);

            if (installment == null) {
                logger.error("Installment not found with ID: {}", installmentId);
                return;
            }

            String htmlContent = buildInstallmentDueHtml(student, fees, installment);
            sendEmail(to, subject, htmlContent);
            logger.info("Installment due email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send installment due email: {}", e.getMessage());
        }
    }

    // ============= 📧 OVERDUE REMINDER EMAIL =============

    @Override
    public void sendOverdueReminderEmail(StudentEntity student, FeesEntity fees, Long installmentId) {
        try {
            String to = getStudentEmail(student);
            String subject = "Payment Overdue Reminder - " + schoolName;

            Installment installment = fees.getInstallmentsList().stream()
                    .filter(i -> installmentId.equals(i.getInstallmentId()))
                    .findFirst()
                    .orElse(null);

            if (installment == null) {
                logger.error("Installment not found with ID: {}", installmentId);
                return;
            }

            String htmlContent = buildOverdueReminderHtml(student, fees, installment);
            sendEmail(to, subject, htmlContent);
            logger.info("Overdue reminder email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send overdue reminder email: {}", e.getMessage());
        }
    }

    // ============= 📧 PAYMENT CONFIRMATION EMAIL =============

    @Override
    public void sendPaymentConfirmationEmail(StudentEntity student, FeesEntity fees, Integer amount, String transactionId) {
        try {
            String to = getStudentEmail(student);
            String subject = "Payment Confirmation - " + schoolName;
            String htmlContent = buildPaymentConfirmationHtml(student, fees, amount, transactionId);
            sendEmail(to, subject, htmlContent);
            logger.info("Payment confirmation email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send payment confirmation email: {}", e.getMessage());
        }
    }

    // ============= 📢 NOTICE EMAIL — TO PARENT =============

    @Override
    public void sendNoticeEmail(StudentEntity student, NoticeEntity notice) {
        try {
            String to = getStudentEmail(student);
            if (to == null || to.isEmpty()) {
                logger.warn("No parent email found for student: {} {}", student.getFirstName(), student.getLastName());
                return;
            }

            String subject     = "📢 New Notice: " + notice.getTitle() + " - " + schoolName;
            String htmlContent = buildNoticeEmailHtml(getParentName(student), student, notice, "PARENT");

            sendEmail(to, subject, htmlContent);
            logger.info("Notice email sent to parent: {} for student: {} {}",
                    to, student.getFirstName(), student.getLastName());
        } catch (Exception e) {
            logger.error("Failed to send notice email to parent of {}: {}", student.getFirstName(), e.getMessage());
        }
    }

    // ============= 📢 NOTICE EMAIL — TO TEACHER =============

    @Override
    public void sendNoticeEmailToTeacher(String teacherEmail, String teacherName, NoticeEntity notice) {
        try {
            if (teacherEmail == null || teacherEmail.isEmpty()) {
                logger.warn("No email found for teacher: {}", teacherName);
                return;
            }

            String subject     = "📢 New Notice: " + notice.getTitle() + " - " + schoolName;
            String htmlContent = buildNoticeEmailHtml(teacherName, null, notice, "TEACHER");

            sendEmail(teacherEmail, subject, htmlContent);
            logger.info("Notice email sent to teacher: {}", teacherEmail);
        } catch (Exception e) {
            logger.error("Failed to send notice email to teacher {}: {}", teacherEmail, e.getMessage());
        }
    }

    // ============= 🎨 HTML TEMPLATE BUILDERS =============

    private String buildFeeReminderHtml(StudentEntity student, FeesEntity fees) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='en'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Fee Payment Reminder</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; background-color: #f4f7fc; padding: 20px; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append(".header { background: #3498db; color: white; padding: 20px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 24px; }");
        html.append(".content { padding: 30px; }");
        html.append(".greeting { font-size: 18px; margin-bottom: 20px; }");
        html.append(".fee-box { background: #f8f9fa; border-left: 4px solid #3498db; padding: 20px; margin: 20px 0; }");
        html.append(".amount { font-size: 24px; color: #e74c3c; font-weight: bold; }");
        html.append(".installment-item { border-bottom: 1px solid #dee2e6; padding: 10px 0; }");
        html.append(".due-badge { background: #fff3cd; color: #856404; padding: 3px 10px; border-radius: 15px; font-size: 12px; }");
        html.append(".overdue-badge { background: #f8d7da; color: #721c24; padding: 3px 10px; border-radius: 15px; font-size: 12px; }");
        html.append(".button { display: inline-block; padding: 12px 30px; background: #3498db; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; margin-top: 20px; }");
        html.append(".button:hover { background: #2980b9; }");
        html.append(".footer { background: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 12px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h1>🏫 ").append(schoolName).append("</h1>");
        html.append("<p>Fee Payment Reminder</p>");
        html.append("</div>");

        html.append("<div class='content'>");
        html.append("<div class='greeting'>Dear ").append(getParentName(student)).append(",</div>");

        html.append("<p>This is a reminder for your child <strong>")
                .append(student.getFirstName()).append(" ").append(student.getLastName())
                .append("</strong> (Class: ").append(student.getCurrentClass()).append("-").append(student.getSection())
                .append(").</p>");

        int paidAmount = fees.getTotalFees() - fees.getRemainingFees();
        html.append("<div class='fee-box'>");
        html.append("<p><strong>Total Fees:</strong> ₹").append(fees.getTotalFees()).append("</p>");
        html.append("<p><strong>Paid Amount:</strong> ₹").append(paidAmount).append("</p>");
        html.append("<p><strong>Pending Amount:</strong> <span class='amount'>₹").append(fees.getRemainingFees()).append("</span></p>");
        html.append("</div>");

        List<Installment> pendingInstallments = fees.getInstallmentsList().stream()
                .filter(i -> !"PAID".equalsIgnoreCase(i.getStatus()))
                .collect(Collectors.toList());

        if (!pendingInstallments.isEmpty()) {
            html.append("<h3>Installment Details:</h3>");
            for (Installment inst : pendingInstallments) {
                boolean isOverdue = isOverdue(inst);
                String badgeClass = isOverdue ? "overdue-badge" : "due-badge";
                String badgeText  = isOverdue ? "OVERDUE" : "PENDING";

                html.append("<div class='installment-item'>");
                html.append("<p><strong>Installment #").append(inst.getInstallmentId()).append("</strong> ");
                html.append("<span class='").append(badgeClass).append("'>").append(badgeText).append("</span></p>");
                html.append("<p>Due Date: ").append(formatDate(inst.getDueDate())).append("</p>");
                html.append("<p>Amount: ₹").append(inst.getAmount()).append("</p>");

                if (isOverdue) {
                    int daysOverdue = calculateDaysOverdue(inst.getDueDate());
                    int lateFee     = daysOverdue * 100;
                    html.append("<p style='color: #e74c3c;'><strong>Late Fee (₹100/day):</strong> +₹").append(lateFee).append("</p>");
                }
                html.append("</div>");
            }
        }

        html.append("<div style='text-align: center;'>");
        html.append("<a href='").append(frontendUrl).append("/login.html' class='button'>💰 Click to Pay</a>");
        html.append("</div>");
        html.append("<p style='margin-top: 20px; font-size: 12px; color: #6c757d;'>Login to your account to view all details and make payment.</p>");
        html.append("</div>");
        html.append("<div class='footer'>");
        html.append("<p>").append(schoolName).append("</p>");
        html.append("<p>This is an automated message. Please do not reply.</p>");
        html.append("</div>");
        html.append("</div></body></html>");

        return html.toString();
    }

    private String buildInstallmentDueHtml(StudentEntity student, FeesEntity fees, Installment installment) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='en'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Installment Due Reminder</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; background-color: #f4f7fc; padding: 20px; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append(".header { background: #f39c12; color: white; padding: 20px; text-align: center; }");
        html.append(".content { padding: 30px; }");
        html.append(".greeting { font-size: 18px; margin-bottom: 20px; }");
        html.append(".installment-box { background: #fef9e7; border-left: 4px solid #f39c12; padding: 20px; margin: 20px 0; }");
        html.append(".button { display: inline-block; padding: 12px 30px; background: #f39c12; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; }");
        html.append(".footer { background: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 12px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        html.append("<div class='container'>");
        html.append("<div class='header'><h1>⏰ Installment Due Reminder</h1></div>");
        html.append("<div class='content'>");
        html.append("<div class='greeting'>Dear ").append(getParentName(student)).append(",</div>");
        html.append("<p>This is a reminder for your child's installment:</p>");

        html.append("<div class='installment-box'>");
        html.append("<p><strong>Student Name:</strong> ").append(student.getFirstName()).append(" ").append(student.getLastName()).append("</p>");
        html.append("<p><strong>Installment #:</strong> ").append(installment.getInstallmentId()).append("</p>");
        html.append("<p><strong>Due Date:</strong> ").append(formatDate(installment.getDueDate())).append("</p>");
        html.append("<p><strong>Amount:</strong> ₹").append(installment.getAmount()).append("</p>");

        if (isOverdue(installment)) {
            int daysOverdue = calculateDaysOverdue(installment.getDueDate());
            int lateFee     = daysOverdue * 100;
            html.append("<p style='color: #e74c3c;'><strong>Late Fee:</strong> +₹").append(lateFee).append(" (₹100/day)</p>");
        }
        html.append("</div>");

        html.append("<div style='text-align: center;'>");
        html.append("<a href='").append(frontendUrl).append("/login.html' class='button'>💰 View Details</a>");
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='footer'><p>").append(schoolName).append("</p></div>");
        html.append("</div></body></html>");

        return html.toString();
    }

    private String buildOverdueReminderHtml(StudentEntity student, FeesEntity fees, Installment installment) {
        StringBuilder html = new StringBuilder();

        int daysOverdue = calculateDaysOverdue(installment.getDueDate());
        int lateFee     = daysOverdue * 100;

        html.append("<!DOCTYPE html>");
        html.append("<html lang='en'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Payment Overdue Reminder</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; background-color: #f4f7fc; padding: 20px; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append(".header { background: #e74c3c; color: white; padding: 20px; text-align: center; }");
        html.append(".urgent-badge { background: #fff3cd; color: #856404; padding: 5px 15px; border-radius: 20px; display: inline-block; font-weight: bold; margin-bottom: 10px; }");
        html.append(".content { padding: 30px; }");
        html.append(".overdue-box { background: #fef2f2; border-left: 4px solid #e74c3c; padding: 20px; margin: 20px 0; }");
        html.append(".late-fee { color: #e74c3c; font-weight: bold; }");
        html.append(".button { display: inline-block; padding: 12px 30px; background: #e74c3c; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; }");
        html.append(".footer { background: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 12px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<span class='urgent-badge'>⚠️ URGENT</span>");
        html.append("<h1>Payment Overdue</h1>");
        html.append("</div>");
        html.append("<div class='content'>");
        html.append("<div class='greeting'>Dear ").append(getParentName(student)).append(",</div>");
        html.append("<p>Your payment is overdue. Please pay as soon as possible.</p>");

        html.append("<div class='overdue-box'>");
        html.append("<p><strong>Student Name:</strong> ").append(student.getFirstName()).append(" ").append(student.getLastName()).append("</p>");
        html.append("<p><strong>Installment #:</strong> ").append(installment.getInstallmentId()).append("</p>");
        html.append("<p><strong>Due Date:</strong> ").append(formatDate(installment.getDueDate())).append("</p>");
        html.append("<p><strong>Installment Amount:</strong> ₹").append(installment.getAmount()).append("</p>");
        html.append("<p class='late-fee'><strong>Late Fee:</strong> +₹").append(lateFee).append(" (").append(daysOverdue).append(" days @ ₹100/day)</p>");
        html.append("<p><strong>Total to Pay:</strong> ₹").append(installment.getAmount() + lateFee).append("</p>");
        html.append("</div>");

        html.append("<div style='text-align: center;'>");
        html.append("<a href='").append(frontendUrl).append("/login.html' class='button'>💰 Click to Pay</a>");
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='footer'><p>").append(schoolName).append("</p></div>");
        html.append("</div></body></html>");

        return html.toString();
    }

    private String buildPaymentConfirmationHtml(StudentEntity student, FeesEntity fees, Integer amount, String transactionId) {
        StringBuilder html = new StringBuilder();

        int remainingFees    = fees.getRemainingFees() - amount;
        Installment nextInst = fees.getInstallmentsList().stream()
                .filter(i -> !"PAID".equalsIgnoreCase(i.getStatus()))
                .findFirst()
                .orElse(null);

        html.append("<!DOCTYPE html>");
        html.append("<html lang='en'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Payment Confirmation</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; background-color: #f4f7fc; padding: 20px; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append(".header { background: #27ae60; color: white; padding: 20px; text-align: center; }");
        html.append(".success-icon { font-size: 48px; }");
        html.append(".content { padding: 30px; }");
        html.append(".payment-box { background: #f8f9fa; border-radius: 5px; padding: 20px; margin: 20px 0; }");
        html.append(".button { display: inline-block; padding: 12px 30px; background: #27ae60; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; }");
        html.append(".footer { background: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 12px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<div class='success-icon'>✅</div>");
        html.append("<h1>Payment Successful!</h1>");
        html.append("</div>");

        html.append("<div class='content'>");
        html.append("<p>Thank you for your payment!</p>");

        html.append("<div class='payment-box'>");
        html.append("<p><strong>Student Name:</strong> ").append(student.getFirstName()).append(" ").append(student.getLastName()).append("</p>");
        html.append("<p><strong>Amount Paid:</strong> ₹").append(amount).append("</p>");
        html.append("<p><strong>Transaction ID:</strong> ").append(transactionId).append("</p>");
        html.append("<p><strong>Date:</strong> ").append(LocalDate.now().format(DATE_FORMATTER)).append("</p>");
        html.append("</div>");

        html.append("<p><strong>Remaining Balance:</strong> ₹").append(Math.max(0, remainingFees)).append("</p>");

        if (nextInst != null) {
            html.append("<p><strong>Next Due Date:</strong> ").append(formatDate(nextInst.getDueDate())).append("</p>");
        }

        html.append("<div style='text-align: center; margin-top: 30px;'>");
        html.append("<a href='").append(frontendUrl).append("/login.html' class='button'>📊 View Dashboard</a>");
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='footer'><p>").append(schoolName).append("</p></div>");
        html.append("</div></body></html>");

        return html.toString();
    }

    // ============= 📢 NOTICE EMAIL HTML BUILDER =============
    // FIX 1: Date format — SimpleDateFormat("dd MMM yyyy") instead of Date.toString()
    // FIX 2: Portal URL — frontendUrl instead of baseUrl
    // recipientType = "PARENT" → shows student info
    // recipientType = "TEACHER" → notice details only

    private String buildNoticeEmailHtml(String recipientName, StudentEntity student,
                                        NoticeEntity notice, String recipientType) {

        String headerColor = "high".equalsIgnoreCase(notice.getPriority())   ? "#e74c3c"
                : "medium".equalsIgnoreCase(notice.getPriority()) ? "#f39c12"
                : "#27ae60";

        String priorityBadge = "high".equalsIgnoreCase(notice.getPriority())
                ? "<span style='background:#fde8e8;color:#c0392b;padding:3px 10px;border-radius:12px;font-size:12px;font-weight:bold;'>🔴 HIGH PRIORITY</span>"
                : "medium".equalsIgnoreCase(notice.getPriority())
                ? "<span style='background:#fef3e2;color:#e67e22;padding:3px 10px;border-radius:12px;font-size:12px;font-weight:bold;'>🟡 MEDIUM PRIORITY</span>"
                : "<span style='background:#eafaf1;color:#27ae60;padding:3px 10px;border-radius:12px;font-size:12px;font-weight:bold;'>🟢 LOW PRIORITY</span>";

        // FIX: Properly format java.util.Date → "16 Mar 2026"
        String publishedStr = notice.getPublishDate() != null
                ? NOTICE_DATE_FORMAT.format(notice.getPublishDate()) : "";
        String expiryStr    = notice.getExpiryDate()  != null
                ? NOTICE_DATE_FORMAT.format(notice.getExpiryDate())  : "";

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='en'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Notice - ").append(notice.getTitle()).append("</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; background-color: #f4f7fc; padding: 20px; margin: 0; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append(".header { background: " + headerColor + "; color: white; padding: 24px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 22px; }");
        html.append(".header p { margin: 6px 0 0; font-size: 13px; opacity: 0.9; }");
        html.append(".content { padding: 28px; }");
        html.append(".greeting { font-size: 17px; margin-bottom: 16px; color: #2c3e50; }");
        html.append(".student-info { background: #eaf4fb; border-radius: 6px; padding: 10px 14px; margin: 14px 0; font-size: 13px; color: #2980b9; }");
        html.append(".notice-box { background: #f8f9fa; border-left: 4px solid " + headerColor + "; padding: 18px; margin: 18px 0; border-radius: 4px; }");
        html.append(".notice-title { font-size: 18px; font-weight: bold; color: #2c3e50; margin-bottom: 10px; }");
        html.append(".notice-meta { font-size: 12px; color: #7f8c8d; margin: 10px 0; }");
        html.append(".notice-body { font-size: 14px; color: #34495e; line-height: 1.7; white-space: pre-wrap; margin-top: 12px; }");
        html.append(".button { display: inline-block; padding: 11px 28px; background: " + headerColor + "; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 14px; }");
        html.append(".footer { background: #f8f9fa; padding: 16px; text-align: center; color: #95a5a6; font-size: 12px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        html.append("<div class='container'>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>🏫 ").append(schoolName).append("</h1>");
        html.append("<p>Official Notice</p>");
        html.append("</div>");

        // Content
        html.append("<div class='content'>");
        html.append("<div class='greeting'>Dear <strong>").append(recipientName).append("</strong>,</div>");
        html.append("<p style='color:#555;font-size:14px;'>A new notice has been issued. Please read it carefully.</p>");

        // Student info — only for parents
        if ("PARENT".equals(recipientType) && student != null) {
            html.append("<div class='student-info'>");
            html.append("👤 <strong>Student:</strong> ")
                    .append(student.getFirstName()).append(" ")
                    .append(student.getLastName() != null ? student.getLastName() : "")
                    .append(" &nbsp;|&nbsp; <strong>Class:</strong> ")
                    .append(student.getCurrentClass() != null ? student.getCurrentClass() : "")
                    .append("-")
                    .append(student.getSection() != null ? student.getSection() : "");
            html.append("</div>");
        }

        // Notice box
        html.append("<div class='notice-box'>");
        html.append("<div class='notice-title'>").append(notice.getTitle()).append("</div>");
        html.append(priorityBadge);

        // FIX: Clean meta — formatted dates, no raw Date.toString()
        html.append("<div class='notice-meta'>");
        html.append("📂 ").append(notice.getCategory() != null ? notice.getCategory() : "General");
        if (!publishedStr.isEmpty())
            html.append(" &nbsp;|&nbsp; 📅 Published: ").append(publishedStr);
        if (!expiryStr.isEmpty())
            html.append(" &nbsp;|&nbsp; ⏰ Valid till: ").append(expiryStr);
        html.append("</div>");

        html.append("<div class='notice-body'>").append(notice.getDescription()).append("</div>");
        html.append("</div>");

        // FIX: CTA button uses frontendUrl not baseUrl
        html.append("<div style='text-align: center; margin-top: 22px;'>");
        html.append("<a href='").append(frontendUrl).append("/login.html' class='button'>🔗 View on Portal</a>");
        html.append("</div>");
        html.append("<p style='font-size: 12px; color: #95a5a6; margin-top: 18px; text-align: center;'>Login to the school portal to view full details.</p>");

        html.append("</div>"); // end content

        // Footer
        html.append("<div class='footer'>");
        html.append("<p>").append(schoolName).append("</p>");
        html.append("<p>This is an automated message from the school notice system. Do not reply.</p>");
        html.append("</div>");

        html.append("</div></body></html>");

        return html.toString();
    }

    // ============= 📧 EMAIL SENDER =============

    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    // ============= 🛠️ HELPER METHODS =============

    private String getStudentEmail(StudentEntity student) {
        if (student.getFatherEmail() != null && !student.getFatherEmail().isEmpty()) {
            return student.getFatherEmail();
        } else if (student.getMotherEmail() != null && !student.getMotherEmail().isEmpty()) {
            return student.getMotherEmail();
        } else if (student.getGuardianEmail() != null && !student.getGuardianEmail().isEmpty()) {
            return student.getGuardianEmail();
        }
        return student.getFatherEmail();
    }

    private String getParentName(StudentEntity student) {
        if (student.getFatherName() != null && !student.getFatherName().isEmpty()) {
            return student.getFatherName();
        } else if (student.getMotherName() != null && !student.getMotherName().isEmpty()) {
            return student.getMotherName();
        } else if (student.getGuardianName() != null && !student.getGuardianName().isEmpty()) {
            return student.getGuardianName();
        }
        return "Parent";
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DATE_FORMATTER);
    }

    private boolean isOverdue(Installment installment) {
        if ("PAID".equalsIgnoreCase(installment.getStatus())) {
            return false;
        }
        return installment.getDueDate() != null && LocalDate.now().isAfter(installment.getDueDate());
    }

    private int calculateDaysOverdue(LocalDate dueDate) {
        if (dueDate == null) return 0;
        return (int) (LocalDate.now().toEpochDay() - dueDate.toEpochDay());
    }
}