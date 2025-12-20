package cz.petrf.prani.security;

import lombok.Data;

@Data
public class LoginRequest {
  private String username;
  private String password;
  private boolean remember;
}