package cz.petrf.prani.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class EmailAuthenticationToken extends AbstractAuthenticationToken {

  private final String email;

  public EmailAuthenticationToken(String email) {
    super((Collection<? extends GrantedAuthority>) null);                 // zatím žádné authorities
    this.email = email;
    setAuthenticated(false);     // před průchodem providerem
  }

  /* po úspěšné autentizaci */
  public EmailAuthenticationToken(String email,
                                  Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.email = email;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return "";
  }  // heslo nepotřebujeme

  @Override
  public Object getPrincipal() {
    return email;
  }

  public String getEmail() {
    return email;
  }
}