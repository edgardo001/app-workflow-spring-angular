package com.workflowspring.flow.infrastructure.email;

import com.workflowspring.audit.AuditService;
import com.workflowspring.shared.event.EmailSendEvent;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    private final JavaMailSender mailSender;
    private final AuditService auditService;

    public EmailSenderService(JavaMailSender mailSender, AuditService auditService) {
        this.mailSender = mailSender;
        this.auditService = auditService;
    }

    public void sendEmail(EmailSendEvent event) {
        log.info("Sending email to={}, subject={}", event.getTo(), event.getSubject());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            boolean isMultipart = event.getAttachmentPaths() != null && !event.getAttachmentPaths().isEmpty();
            MimeMessageHelper helper = new MimeMessageHelper(message, isMultipart, "UTF-8");

            helper.setTo(event.getTo().split(","));
            helper.setSubject(event.getSubject());
            helper.setText(event.getBody(), true); // true indicates HTML content

            if (isMultipart) {
                for (String path : event.getAttachmentPaths()) {
                    File file = new File(path);
                    if (file.exists()) {
                        helper.addAttachment(file.getName(), file);
                    } else {
                        log.warn("Attachment file not found at path: {}", path);
                    }
                }
            }

            mailSender.send(message);
            auditService.logEvent(event.getFlowId(), "EMAIL_SENT to " + event.getTo());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email to " + event.getTo(), e);
        }
    }
}
