package cz.petrf.prani.service;

import cz.petrf.prani.db.entity.MagicLinkToken;
import cz.petrf.prani.db.entity.Role;
import cz.petrf.prani.db.entity.User;
import cz.petrf.prani.db.repo.MagicLinkTokenRepository;
import cz.petrf.prani.db.repo.RoleRepository;
import cz.petrf.prani.db.repo.UserRepository;
import cz.petrf.prani.exception.ExpiredTokenException;
import cz.petrf.prani.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.resilience.annotation.Retryable;
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
  private static final int MAX_RETRIES = 3;
  private static final long RETRY_DELAY_MS = 1000;

  @Value("${app.magic-link.expiration-minutes:15}")
  private int expirationMinutes;
  @Value("${app.magic-link.token-url:http://localhost:4200/verify-token/}")
  private String tokenUrl;
  @Value("${app.magic-link.mail.from:petr.franta@gmail.com}")
  private String fromEmail;
  @Value("${app.magic-link.mail.subject:Přání paní doktorce - přihlášení do aplikace}")
  private String mailSubject;

  private final MagicLinkTokenRepository magicLinkRepository;
  private final TemplateEngine templateEngine;
  private final EmailService emailService;
  private final UserRepository userRepo;
  private final RoleRepository roleRepo;

  @Retryable(value = {MailException.class}, maxRetries = MAX_RETRIES, delay = RETRY_DELAY_MS)
  public void sendMagicLink(String email) {
    // Generování magic linku
    String token = generateMagicLink(email);
    String magicLink = tokenUrl + token;
    log.debug("magicLink: {}", magicLink);
    String htmlContent = createMagicLinkEmail(magicLink, email);
    log.trace("htmlContent: {}", htmlContent);

    emailService.sendHtmlEmail(fromEmail, email, mailSubject, htmlContent);
  }

  private String createMagicLinkEmail(String magicLink, String email) {
    Context context = new Context();
    context.setVariable("magicLink", magicLink);
    context.setVariable("expirationMinutes", expirationMinutes);
    context.setVariable("userEmail", email);

    return templateEngine.process("email/magic-link-email", context);
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

