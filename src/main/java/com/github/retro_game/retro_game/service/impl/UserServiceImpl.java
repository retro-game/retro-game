package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.User;
import com.github.retro_game.retro_game.model.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.dto.UserSettingsDto;
import com.github.retro_game.retro_game.service.exception.UserDoesntExistException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

@Service("userService")
class UserServiceImpl implements UserServiceInternal {
  private final String defaultLanguage;
  private final String defaultSkin;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  public UserServiceImpl(@Value("${retro-game.default-language}") String defaultLanguage,
                         @Value("${retro-game.default-skin}") String defaultSkin,
                         PasswordEncoder passwordEncoder,
                         UserRepository userRepository) {
    this.defaultLanguage = defaultLanguage;
    this.defaultSkin = defaultSkin;
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
  }

  @Override
  public void create(String email, String name, String password) {
    Date now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    User user = new User();
    user.setName(name);
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(password));
    user.setMessagesSeenAt(now);
    user.setCombatReportsSeenAt(now);
    user.setEspionageReportsSeenAt(now);
    user.setHarvestReportsSeenAt(now);
    user.setTransportReportsSeenAt(now);
    user.setOtherReportsSeenAt(now);
    user.setLanguage(defaultLanguage);
    user.setSkin(defaultSkin);
    user.setNumProbes(1);
    user.setNumberInputScrolling(true);
    userRepository.save(user);
  }

  @Override
  public boolean checkCurrentUserPassword(String password) {
    long userId = CustomUser.getCurrentUserId();
    String encodedPassword = userRepository.findById(userId).orElseThrow(UserDoesntExistException::new).getPassword();
    return passwordEncoder.matches(password, encodedPassword);
  }

  @Override
  public boolean existsByEmailIgnoreCase(String email) {
    return userRepository.existsByEmailIgnoreCase(email);
  }

  @Override
  public boolean existsByNameIgnoreCase(String name) {
    return userRepository.existsByNameIgnoreCase(name);
  }

  @Override
  public String getName(long userId) {
    return userRepository.findById(userId).orElseThrow(UserDoesntExistException::new).getName();
  }

  @Override
  public String getCurrentUserName() {
    return getName(CustomUser.getCurrentUserId());
  }

  @Override
  public UserSettingsDto getCurrentUserSettings() {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.findById(userId).orElseThrow(UserDoesntExistException::new);
    return new UserSettingsDto(user.getLanguage(), user.getSkin(), user.getNumProbes(),
        user.isNumberInputScrollingEnabled(), user.isShowNewMessagesInOverviewEnabled(),
        user.isShowNewReportsInOverviewEnabled());
  }

  @Override
  @Transactional
  public void saveCurrentUserSettings(UserSettingsDto settings) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.findById(userId).orElseThrow(UserDoesntExistException::new);
    user.setLanguage(settings.getLanguage());
    user.setSkin(settings.getSkin());
    user.setNumProbes(settings.getNumProbes());
    user.setNumberInputScrolling(settings.isNumberInputScrollingEnabled());
    user.setShowNewMessagesInOverview(settings.isShowNewMessagesInOverviewEnabled());
    user.setShowNewReportsInOverview(settings.isShowNewReportsInOverviewEnabled());
  }

  @Override
  public Optional<Locale> getCurrentUserLocaleIfAuthenticated() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof CustomUser)) {
      return Optional.empty();
    }
    UserSettingsDto settings = getCurrentUserSettings();
    Locale locale = new Locale(settings.getLanguage());
    return Optional.of(locale);
  }
}
