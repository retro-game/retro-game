package com.github.retro_game.retro_game.security;

import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new UsernameNotFoundException("User not found"));
    return new CustomUser(user);
  }
}
