package cz.petrf.prani.service;

import cz.petrf.prani.db.entity.MagicLinkToken;
import cz.petrf.prani.db.entity.Role;
import cz.petrf.prani.db.entity.User;
import cz.petrf.prani.db.repo.MagicLinkTokenRepository;
import cz.petrf.prani.db.repo.RoleRepository;
import cz.petrf.prani.db.repo.UserRepository;
import cz.petrf.prani.exception.EmailException;
import cz.petrf.prani.exception.ExpiredTokenException;
import cz.petrf.prani.exception.InvalidTokenException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MagicLinkService {
  @Value("${app.magic-link.expiration-minutes:15}")
  private int expirationMinutes;
  @Value("${app.magic-link.token-url:http://localhost:8080/api/auth/verify?token=}")
  private String tokenUrl;

  private final MagicLinkTokenRepository magicLinkRepository;
  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;
  private final EmailService emailService;
  private final UserRepository userRepo;
  private final RoleRepository roleRepo;

  @Async("emailTaskExecutor")
  public void sendMagicLink(String email) {
    // Generování magic linku
    String token = generateMagicLink(email);
    String magicLink = tokenUrl + token;

    log.debug("magicLink: {}", magicLink);

    // Příprava šablony
    Context context = new Context();
    context.setVariable("magicLink", magicLink);
    context.setVariable("expirationMinutes", 15);
    context.setVariable("userEmail", email);

    String htmlContent = templateEngine.process("magic-link-email", context);

    // Odeslání
    MimeMessage message = mailSender.createMimeMessage();
    try {
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(email);
      helper.setSubject("Přihlašovací odkaz do aplikace");
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("Magic link email sent to: {}", email);
    } catch (MessagingException e) {
      log.error("Failed to send magic link email to: {}", email, e);
      throw new EmailException("Failed to send magic link email");
    }
  }

  private String generateMagicLink(String email) {
    String token = UUID.randomUUID().toString();

    MagicLinkToken linkToken = new MagicLinkToken();

    linkToken.setEmail(email);
    linkToken.setToken(token);
    linkToken.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));

    magicLinkRepository.save(linkToken);

    return token;
  }

  public MagicLinkToken verifyToken(String token) {
    MagicLinkToken linkToken = magicLinkRepository.findById(token).orElseThrow(InvalidTokenException::new);

    if (!linkToken.isValid()) {
      throw new ExpiredTokenException();
    }

    // Smaž token (jednorázové použití)
    magicLinkRepository.delete(linkToken);

    createUserIfNotExists(linkToken.getEmail());

    return linkToken;
  }

  private void createUserIfNotExists(String email) {
    Optional<User> userOpt = userRepo.findByEmail(email);

    if (userOpt.isEmpty()) {
      Role userRole = roleRepo.findByName("ROLE_USER").orElseThrow();
      User user = User.builder()
          .publicName(email)
          .password("")
          .email(email)
          .emailVerifiedAt(Instant.now())
          .roles(Set.of(userRole))
          .build();

      userRepo.save(user);
    }
  }
}