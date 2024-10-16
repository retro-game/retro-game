package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.entity.UserPasswordResetToken;
import com.github.retro_game.retro_game.entity.UserPasswordResetTokenKey;
import com.github.retro_game.retro_game.repository.UserPasswordResetTokenRepository;
import com.github.retro_game.retro_game.repository.UserRepository;
import com.github.retro_game.retro_game.service.EmailService;
import com.github.retro_game.retro_game.service.ResetPasswordService;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

import static java.util.UUID.randomUUID;

@Service
public class ResetPasswordServiceImpl implements ResetPasswordService {
  private static final Logger logger = LoggerFactory.getLogger(ResetPasswordServiceImpl.class);
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final EmailService emailService;
  private final UserPasswordResetTokenRepository userPasswordResetTokenRepository;
  private final boolean enablePasswordRecovery;
  private final int passwordResetLinkExpiresAfterDays;

  public ResetPasswordServiceImpl(
      @Value("${retro-game.enable-password-recovery}") boolean enablePasswordRecovery,
      @Value("${retro-game.password-reset-link-expires-after-days}") int passwordResetLinkExpiresAfterDays,
      PasswordEncoder passwordEncoder,
      UserRepository userRepository,
      EmailService emailService,
      UserPasswordResetTokenRepository userPasswordResetTokenRepository) {
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.emailService = emailService;
    this.userPasswordResetTokenRepository = userPasswordResetTokenRepository;
    this.enablePasswordRecovery = enablePasswordRecovery;
    this.passwordResetLinkExpiresAfterDays = passwordResetLinkExpiresAfterDays;
  }

  @Override
  @Transactional
  public void generateTokenAndSendEmail(String email) {
    if (!enablePasswordRecovery) {
      logger.warn("Attempt to recover password while password recovery is disabled");
      return;
    }

    Optional<User> user = userRepository.findByEmailIgnoreCase(email);

    user.ifPresent(u -> {
      String plainToken = generateToken();
      var existingToken = userPasswordResetTokenRepository.findByKey_User(u);

      if (existingToken.isPresent()) {
        userPasswordResetTokenRepository.save(
            updatedToken(plainToken, existingToken.get())
        );
      } else {
        userPasswordResetTokenRepository.save(
            prepareNewToken(u, plainToken)
        );
      }

      emailService.sendResetPasswordEmail(u, plainToken);
    });
  }

  @Override
  @Transactional
  public void resetUserPassword(String plainToken, String newPassword) {
    if (!enablePasswordRecovery) {
      logger.warn("Attempt to recover password while password recovery is disabled");
      return;
    }

    Optional<UserPasswordResetToken> token = userPasswordResetTokenRepository.findAll().stream()
        .filter(t -> passwordEncoder.matches(plainToken, t.getEncryptedToken()))
        .findFirst();

    token.ifPresent(t -> {
      if (!isTokenExpired(t)) {
        updatePassword(newPassword, t);
      } else {
        userPasswordResetTokenRepository.delete(t);
      }
    });

  }

  private void updatePassword(String newPassword, UserPasswordResetToken t) {
    User user = t.getKey().getUser();
    user.setPassword(passwordEncoder.encode(newPassword));

    userRepository.save(user);
    userPasswordResetTokenRepository.delete(t);
  }

  private boolean isTokenExpired(UserPasswordResetToken token) {
    return token.getExpireAt().before(new Date());
  }

  private UserPasswordResetToken updatedToken(String plainToken, UserPasswordResetToken existingToken) {
    existingToken.setEncryptedToken(getEncryptedToken(plainToken));
    existingToken.setExpireAt(calculateExpiredAt());
    return existingToken;
  }

  private UserPasswordResetToken prepareNewToken(User user, String plainToken) {
    return new UserPasswordResetToken(
        new UserPasswordResetTokenKey(user),
        getEncryptedToken(plainToken),
        calculateExpiredAt()
    );
  }

  private String getEncryptedToken(String plainToken) {
    return passwordEncoder.encode(plainToken);
  }

  private Date calculateExpiredAt() {
    return DateUtils.addDays(new Date(), passwordResetLinkExpiresAfterDays);
  }

  private static String generateToken() {
    return randomUUID().toString();
  }
}
