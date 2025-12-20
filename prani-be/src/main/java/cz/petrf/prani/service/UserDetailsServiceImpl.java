package cz.petrf.prani.service;

import cz.petrf.prani.db.entity.User;
import cz.petrf.prani.db.repo.UserRepository;
import cz.petrf.prani.security.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Override
  @Transactional
  public AppUser loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = findByEmailOrEx(email);

    return new AppUser(user);
  }

  @Transactional
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Transactional
  public User findByEmailOrEx(String email) {
    return findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User by email not found: " + email));
  }
}