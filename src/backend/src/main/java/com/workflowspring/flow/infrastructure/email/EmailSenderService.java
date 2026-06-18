package com.workflowspring.flow.infrastructure.email;

import com.workflowspring.audit.AuditService;
import com.workflowspring.auth.JwtTokenService;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Locale;

@Service
public class EmailSenderService {

    private final JavaMailSender mailSender;
    private final AuditService auditService;
    private final JwtTokenService jwtTokenService;
    private final TemplateEngine templateEngine;
    private final String frontendUrl;

    public EmailSenderService(JavaMailSender mailSender,
                              AuditService auditService,
                              JwtTokenService jwtTokenService,
                              TemplateEngine templateEngine,
                              @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl) {
        this.mailSender = mailSender;
        this.auditService = auditService;
        this.jwtTokenService = jwtTokenService;
        this.templateEngine = templateEngine;
        this.frontendUrl = frontendUrl;
    }

    public void sendApprovalEmail(String to, String flowTitle, String token,
                                  String flowId, int stepNumber, int totalSteps,
                                  String description, List<String> documentNames) {
        try {
            String approvalLink = frontendUrl + "/approve?token=" + token;

            Context context = new Context(Locale.getDefault());
            context.setVariable("flowTitle", flowTitle);
            context.setVariable("stepInfo", (stepNumber + 1) + " de " + totalSteps);
            context.setVariable("description", description != null ? description : "");
            context.setVariable("documentNames", documentNames);
            context.setVariable("approvalLink", approvalLink);

            String htmlBody = templateEngine.process("email/approval", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Solicitud de Aprobacion: " + flowTitle);
            helper.setText(htmlBody, true);
            mailSender.send(message);

            auditService.logEvent(flowId, "APPROVAL_EMAIL_SENT to " + to);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send approval email to " + to, e);
        }
    }

    public void sendRejectionNotification(List<String> participants, String flowTitle, String reason) {
        String subject = "Flujo Rechazado: " + flowTitle;

        Context context = new Context(Locale.getDefault());
        context.setVariable("flowTitle", flowTitle);
        context.setVariable("reason", reason);
        String htmlBody = templateEngine.process("email/rejection", context);

        for (String email : participants) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(email);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);
                mailSender.send(message);
                auditService.logEvent(null, "REJECTION_NOTIFICATION_SENT to " + email);
            } catch (Exception e) {
                throw new RuntimeException("Failed to send rejection notification to " + email, e);
            }
        }
    }

    public void sendCompletionEmail(List<String> participants, String flowTitle,
                                    List<byte[]> attachments, List<String> filenames) {
        String subject = "Flujo Completado: " + flowTitle;

        Context context = new Context(Locale.getDefault());
        context.setVariable("flowTitle", flowTitle);
        String htmlBody = templateEngine.process("email/completion", context);

        for (String email : participants) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(email);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);

                for (int i = 0; i < attachments.size(); i++) {
                    String filename = (i < filenames.size()) ? filenames.get(i) : "document-" + (i + 1);
                    ByteArrayDataSource dataSource = new ByteArrayDataSource(attachments.get(i), "application/octet-stream");
                    helper.addAttachment(filename, dataSource);
                }

                mailSender.send(message);
                auditService.logEvent(null, "COMPLETION_EMAIL_SENT to " + email);
            } catch (Exception e) {
                throw new RuntimeException("Failed to send completion email to " + email, e);
            }
        }
    }

    public void sendExpirationNotification(List<String> participants, String flowTitle) {
        String subject = "Flujo Expirado: " + flowTitle;

        Context context = new Context(Locale.getDefault());
        context.setVariable("flowTitle", flowTitle);
        String htmlBody = templateEngine.process("email/expiration", context);

        for (String email : participants) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(email);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);
                mailSender.send(message);
                auditService.logEvent(null, "EXPIRATION_NOTIFICATION_SENT to " + email);
            } catch (Exception e) {
                throw new RuntimeException("Failed to send expiration notification to " + email, e);
            }
        }
    }
}
