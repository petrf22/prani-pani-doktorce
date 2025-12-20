package cz.petrf.prani.config;

import cz.petrf.prani.security.EmailAuthenticationProvider;
import cz.petrf.prani.security.JwtRequestFilter;
import cz.petrf.prani.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtRequestFilter jwtRequestFilter;
  private final UserDetailsServiceImpl userDetailsService;
  private final EmailAuthenticationProvider emailProvider;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);   // strength 10
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config, HttpSecurity http) {
    return http.getSharedObject(AuthenticationManagerBuilder.class)
        .authenticationProvider(emailProvider)
        .build();

    //return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(c -> c.configurationSource(request -> {
          var cors = new CorsConfiguration();
          cors.setAllowedOrigins(List.of("http://localhost:4200"));
          cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
          cors.setAllowedHeaders(List.of("*"));
          cors.setAllowCredentials(true);
          return cors;
        }))
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/api/auth/**").permitAll()
            //.requestMatchers("/api/admin/**").hasRole("ADMIN")
            //.requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
            //.anyRequest().permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(sess -> sess
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}