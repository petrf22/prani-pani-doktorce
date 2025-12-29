package cz.petrf.prani.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import cz.petrf.prani.exception.EmailException;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
  private static final int MAX_RETRIES = 3;
  private static final long RETRY_DELAY_MS = 1000;

  @Value("${spring.mail.from:prani@prani.cz}")
  private String fromEmailDefault;
  @Value("${app.mail.resend.api.key}")
  private String resendApiKey;

  private final JavaMailSender mailSender;
  private Optional<Resend> resendOpt = Optional.empty();

  public void sendHtmlEmail(String fromEmail, String toEmail, String mailSubject, String htmlContent) {
    if (resendOpt.isPresent()) {
      sendOverResend(fromEmail, toEmail, mailSubject, htmlContent);
    } else {
      sendOverJavaMail(fromEmail, toEmail, mailSubject, htmlContent);
    }
  }

  private void sendOverResend(String fromEmail, String toEmail, String mailSubject, String htmlContent) {
    CreateEmailOptions params = CreateEmailOptions.builder()
        .from(fromEmail)  // nebo vlastní doména po verifikaci
        .to(toEmail)
        .subject(mailSubject)
        .html(htmlContent)
        .build();

    try {
      CreateEmailResponse data = resendOpt.orElseThrow().emails().send(params);
      log.info("Magic link byl odeslán na email: {} (email ID: {})", toEmail, data.getId());
    } catch (ResendException e) {
      log.error("Chyba při odeslání emailu s přihlášením k aplikaci na adresu: {}", toEmail, e);
      throw new EmailException("Chyba při odeslání emailu s přihlášením k aplikaci");
    }
  }

  private void sendOverJavaMail(String fromEmail, String toEmail, String mailSubject, String htmlContent) {
    MimeMessage message = mailSender.createMimeMessage();

    try {
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject(mailSubject);
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("Magic link byl odeslán na email: {}", toEmail);
    } catch (MessagingException e) {
      log.error("Chyba při odeslání emailu s přihlášením k aplikaci na adresu: {}", toEmail, e);
      throw new EmailException("Chyba při odeslání emailu s přihlášením k aplikaci");
    }
  }

  public void sendSimpleEmail(String to, String subject, String text) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmailDefault);
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
      helper.setFrom(fromEmailDefault);
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

  @PostConstruct
  public void postConstruct() {
    if (StringUtils.isNotBlank(resendApiKey)) {
      resendOpt = Optional.of(new Resend(StringUtils.trimToNull(resendApiKey)));
    }
  }
}