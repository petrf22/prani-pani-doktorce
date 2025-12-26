package cz.petrf.prani.controller;

import cz.petrf.prani.db.entity.MagicLinkToken;
import cz.petrf.prani.db.entity.Role;
import cz.petrf.prani.db.entity.User;
import cz.petrf.prani.exception.EmailException;
import cz.petrf.prani.exception.ExpiredTokenException;
import cz.petrf.prani.exception.InvalidTokenException;
import cz.petrf.prani.security.*;
import cz.petrf.prani.service.MagicLinkService;
import cz.petrf.prani.service.UserDetailsServiceImpl;
import cz.petrf.prani.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final UserDetailsServiceImpl userDetailsService;
  private final JwtService jwtService;
  private final MagicLinkService magicLinkService;
  private final UserService userService;

  @Value("${app.refresh.token.max.age:90}")
  private int refreshTokenMaxAge;
  @Value("${app.security.cookie.secure:true}")
  private boolean secureCookie;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest req, HttpServletResponse resp) {

    try {
      log.info("Login attempt for user: {} from IP: {}", loginRequest.getUsername(), req.getRemoteAddr());

      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginRequest.getUsername(),
              loginRequest.getPassword()
          )
      );

      SecurityContextHolder.getContext().setAuthentication(authentication);

      final AppUser appUser = userDetailsService.loadUserByUsername(loginRequest.getUsername());
      log.info("Successful login for user: {}", loginRequest.getUsername());

      return createLoginResponseEntity(appUser.getDbUser(), req, resp);
    } catch (BadCredentialsException e) {
      log.warn("Failed login attempt for user: {} from IP: {}", loginRequest.getUsername(), req.getRemoteAddr());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(@CookieValue(name = "refresh", required = false) String refreshToken,
                                   HttpServletRequest req, HttpServletResponse resp) {

    /* 1. chybí cookie */
    if (refreshToken==null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      /* 2. parse */
      Claims claims = jwtService.extractAllClaims(refreshToken);
      Optional<UUID> jtiOpt = Optional.ofNullable(claims.getId()).map(UUID::fromString);

      /* 1. chybí JIT */
      if (jtiOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      /* 3. blacklist */
      if (!jwtService.isValid(jtiOpt.get())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      /* 4. najdi uživatele */
      Optional<User> userOpt = jwtService.findByJti(jtiOpt.get());

      if (userOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      return createLoginResponseEntity(userOpt.get(), req, resp);
    } catch (JwtException | UsernameNotFoundException ex) {
      log.error("Chyba refresh tokenu", ex);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(@CookieValue(name = "refresh", required = false) String refreshToken,
                                  @RequestParam("deleteAccount") boolean deleteAccount,
                                  @AuthenticationPrincipal AppUser appUser,
                                  HttpServletResponse resp) {
    Optional<User> userOpt = Optional.empty();

    if (refreshToken!=null) {
      try {
        String jti = jwtService.extractAllClaims(refreshToken).getId();
        UUID jtiUuid = UUID.fromString(jti);
        jwtService.revokeAllForUserByJti(jtiUuid);

        userOpt = jwtService.findByJti(jtiUuid);
      } catch (JwtException ignored) {
      }
    }

    // Delete cookie
    deleteRefreshCookie(resp);

    if (userOpt.isPresent() && deleteAccount) {
      userService.deleteUserAccount(userOpt.get());
    }

    return ResponseEntity.ok().build();
  }

  private void deleteRefreshCookie(HttpServletResponse resp) {
    addRefreshCookie(resp, "", Duration.ZERO);
  }

  private void addRefreshCookie(HttpServletResponse resp, String refreshToken, Duration maxAge) {
    ResponseCookie cookie = ResponseCookie.from("refresh", refreshToken)
        .httpOnly(true)
        .secure(secureCookie)
        .sameSite(secureCookie ? "Strict":"Lax")
        .path(secureCookie ? "/api/auth/refresh":"/")
        .maxAge(maxAge)
        .build();

    resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  @GetMapping("/validate")
  public ResponseEntity<?> validateToken() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth!=null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  private ResponseEntity<?> createLoginResponseEntity(User dbUser, HttpServletRequest req, HttpServletResponse resp) {
    final String accessToken = jwtService.generateToken(dbUser);
    final String device = jwtService.guessDevice(req);
    final Duration maxAge = Duration.ofDays(refreshTokenMaxAge);
    final String refreshToken = jwtService.createRefresh(dbUser, UUID.randomUUID(), device, maxAge);

    addRefreshCookie(resp, refreshToken, maxAge);

    return ResponseEntity.ok(new TokenDto(accessToken, dbUser.getEmail(), dbUser.getRoles().stream().map(Role::getName).toList()));
  }

  @GetMapping("/verify/{emailToken}")
  public ResponseEntity<?> verifyToken(@PathVariable String emailToken, HttpServletRequest req, HttpServletResponse resp) {

    try {
      log.info("Login attempt for emailToken: {} from IP: {}", emailToken, req.getRemoteAddr());

      MagicLinkToken linkToken = magicLinkService.verifyToken(emailToken);
      Authentication authentication = authenticationManager.authenticate(
          new EmailAuthenticationToken(
              linkToken.getEmail()
          )
      );

      SecurityContextHolder.getContext().setAuthentication(authentication);

      final AppUser appUser = userDetailsService.loadUserByUsername(linkToken.getEmail());
      log.info("Successful login for user email: {}", linkToken.getEmail());

      return createLoginResponseEntity(appUser.getDbUser(), req, resp);
    } catch (InvalidTokenException | ExpiredTokenException e) {
      log.warn("Failed login attempt for token: {} from IP: {}", emailToken, req.getRemoteAddr());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
    }
  }

  @PostMapping("/mail-token")
  public ResponseEntity<?> mailToken(@RequestParam String email, HttpServletRequest req, HttpServletResponse resp) {

    try {
      log.info("Login attempt for email: {} from IP: {}", email, req.getRemoteAddr());

      magicLinkService.sendMagicLink(email);

      return ResponseEntity.status(HttpStatus.OK).body("Send email successfully");
    } catch (EmailException e) {
      log.warn("Failed send attempt for email: {} from IP: {}", email, req.getRemoteAddr());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Send email error");
    }
  }
}
