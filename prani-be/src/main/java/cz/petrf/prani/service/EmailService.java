package cz.petrf.prani.service;

import cz.petrf.prani.exception.EmailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
  private static final int MAX_RETRIES = 3;
  private static final long RETRY_DELAY_MS = 1000;

  @Value("${spring.mail.from:prani@prani.cz}")
  private String fromEmail;

  private final JavaMailSender mailSender;
  private final SpringTemplateEngine templateEngine;

  public void sendSimpleEmail(String to, String subject, String text) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(text);

      mailSender.send(message);
      log.info("Email sent successfully to: {}", to);
    } catch (MailException e) {
      log.error("Failed to send email to: {}", to, e);
      throw new EmailException("Failed to send email", e);
    }
  }

  public void sendHtmlEmail(String to, String subject, String htmlContent) {
    MimeMessage message = mailSender.createMimeMessage();

    try {
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlContent, true);  // true = HTML obsah

      mailSender.send(message);
      log.info("HTML email sent to: {}", to);
    } catch (MessagingException e) {
      log.error("Failed to send HTML email", e);
      throw new EmailException("Failed to send HTML email", e);
    }
  }


  @Retryable(value = {MailException.class}, maxRetries = MAX_RETRIES, delay = RETRY_DELAY_MS)
  public void sendEmailWithRetry(String to, String subject, String content) {
    try {
      // Odeslání emailu
    } catch (MailException e) {
      log.warn("Email sending failed, retrying... Attempt: {}", RetrySynchronizationManager.getContext().getRetryCount());
      throw e;
    }
  }

  @Recover
  public void recover(MailException e, String to, String subject, String content) {
    log.error("Failed to send email after {} retries to: {}", MAX_RETRIES, to, e);
    // Uložit do DB pro pozdější opakování
    // saveFailedEmail(to, subject, content);
  }
}