package com.workflownet.flow.infrastructure.email;

import com.workflownet.audit.AuditService;
import com.workflownet.shared.event.EmailSendEvent;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailSenderService {

    private final JavaMailSender mailSender;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AuditService auditService;

    public EmailSenderService(JavaMailSender mailSender,
                              KafkaTemplate<String, Object> kafkaTemplate,
                              AuditService auditService) {
        this.mailSender = mailSender;
        this.kafkaTemplate = kafkaTemplate;
        this.auditService = auditService;
    }

    public void sendApprovalEmail(String to, String flowTitle, String token,
                                  String flowId, int stepNumber, List<byte[]> attachments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Approval Required: " + flowTitle);

            String approvalLink = "https://workflownet.app/approve?token=" + token;
            String htmlBody = """
                    <html>
                    <body>
                    <h2>Approval Request</h2>
                    <p>You have been requested to approve: <strong>%s</strong></p>
                    <p>Step %d of the approval workflow requires your review.</p>
                    <p><a href="%s">Click here to review and approve</a></p>
                    </body>
                    </html>
                    """.formatted(flowTitle, stepNumber + 1, approvalLink);

            helper.setText(htmlBody, true);

            for (int i = 0; i < attachments.size(); i++) {
                ByteArrayDataSource dataSource = new ByteArrayDataSource(attachments.get(i), "application/octet-stream");
                helper.addAttachment("document-" + (i + 1), dataSource);
            }

            mailSender.send(message);
            auditService.logEvent(flowId, "APPROVAL_EMAIL_SENT to " + to);
            kafkaTemplate.send("email.send", flowId,
                    new EmailSendEvent(flowId, to, "Approval Required: " + flowTitle, htmlBody, List.of(), stepNumber));
        } catch (Exception e) {
            throw new RuntimeException("Failed to send approval email to " + to, e);
        }
    }

    public void sendNotificationEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            mailSender.send(message);
            auditService.logEvent(null, "NOTIFICATION_EMAIL_SENT to " + to);
            kafkaTemplate.send("email.send", null,
                    new EmailSendEvent(null, to, subject, body, List.of(), -1));
        } catch (Exception e) {
            throw new RuntimeException("Failed to send notification email to " + to, e);
        }
    }

    public void sendRejectionNotification(List<String> participants, String flowTitle, String reason) {
        String subject = "Flow Rejected: " + flowTitle;
        String body = "The flow \"" + flowTitle + "\" has been rejected.\nReason: " + reason;

        for (String email : participants) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

                helper.setTo(email);
                helper.setSubject(subject);
                helper.setText(body);

                mailSender.send(message);
                auditService.logEvent(null, "REJECTION_NOTIFICATION_SENT to " + email);
                kafkaTemplate.send("email.send", null,
                        new EmailSendEvent(null, email, subject, body, List.of(), -1));
            } catch (Exception e) {
                throw new RuntimeException("Failed to send rejection notification to " + email, e);
            }
        }
    }

    public void sendCompletionEmail(List<String> participants, String flowTitle,
                                    List<byte[]> attachments, List<String> filenames) {
        String subject = "Flow Completed: " + flowTitle;
        String text = "The flow \"" + flowTitle + "\" has been completed. The final documents are attached.";

        for (String email : participants) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(email);
                helper.setSubject(subject);
                helper.setText(text);

                for (int i = 0; i < attachments.size(); i++) {
                    String filename = (i < filenames.size()) ? filenames.get(i) : "document-" + (i + 1);
                    ByteArrayDataSource dataSource = new ByteArrayDataSource(attachments.get(i), "application/octet-stream");
                    helper.addAttachment(filename, dataSource);
                }

                mailSender.send(message);
                auditService.logEvent(null, "COMPLETION_EMAIL_SENT to " + email);
                kafkaTemplate.send("email.send", null,
                        new EmailSendEvent(null, email, subject, text, List.of(), -1));
            } catch (Exception e) {
                throw new RuntimeException("Failed to send completion email to " + email, e);
            }
        }
    }
}
