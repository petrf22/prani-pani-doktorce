package cz.petrf.prani.security;

import cz.petrf.prani.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  public static final String BEARER = "Bearer ";

  @Autowired
  private UserDetailsServiceImpl userDetailsService;

  @Autowired
  private JwtService jwtService;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {

    String path = request.getServletPath();

    // Přeskoč JWT validaci pro statické soubory a veřejné endpointy
    if (shouldNotFilter(request)) {
      chain.doFilter(request, response);
      return;
    }

    final String authorizationHeader = request.getHeader("Authorization");

    String username = null;
    String jwt = null;

    if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
      try {
        jwt = authorizationHeader.substring(BEARER.length());
        username = jwtService.extractUsername(jwt);
      } catch (Exception e) {
        logger.error("JWT Token extraction failed", e);
      }
    }

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

      if (jwtService.validateToken(jwt, userDetails)) {
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }
    chain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();

    // Přeskoč JWT validaci pro:
    return path.equals("/") ||
        path.equals("/index.html") ||
        path.endsWith(".js") ||
        path.endsWith(".css") ||
        path.endsWith(".ico") ||
        path.endsWith(".png") ||
        path.endsWith(".jpg") ||
        path.endsWith(".jpeg") ||
        path.endsWith(".gif") ||
        path.endsWith(".svg") ||
        path.endsWith(".woff") ||
        path.endsWith(".woff2") ||
        path.endsWith(".ttf") ||
        path.endsWith(".eot") ||
        path.startsWith("/assets/") ||
        path.startsWith("/api/auth/") ||
        path.startsWith("/actuator/health") ||
        path.startsWith("/actuator/info");
  }
}