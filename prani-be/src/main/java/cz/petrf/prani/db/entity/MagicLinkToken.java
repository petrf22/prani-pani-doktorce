package cz.petrf.prani.db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "magic_link_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MagicLinkToken {
  @Id
  private String token;
  private String email;
  private LocalDateTime expiresAt;
  private LocalDateTime usedAt;
  private boolean revoked;

  // Single-use pouze
  public boolean isValid() {
    return !revoked && usedAt==null && expiresAt.isAfter(LocalDateTime.now());
  }
}