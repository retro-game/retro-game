package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
public class EmailServiceImpl implements EmailService {
  private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
  private final JavaMailSender emailSender;
  private final ResourceBundleMessageSource messageSource;
  private final String passwordResetMailFrom;
  private final String passwordResetLinkExpiresAfterDays;
  private final String domain;

  EmailServiceImpl(
      @Value("${retro-game.password-reset-mail-from}") String passwordResetMailFrom,
      @Value("${retro-game.password-reset-link-expires-after-days}") String passwordResetLinkExpiresAfterDays,
      @Value("${retro-game.domain}") String domain,
      JavaMailSender emailSender,
      ResourceBundleMessageSource messageSource
  ) {
    this.passwordResetMailFrom = passwordResetMailFrom;
    this.passwordResetLinkExpiresAfterDays = passwordResetLinkExpiresAfterDays;
    this.domain = domain;
    this.emailSender = emailSender;
    this.messageSource = messageSource;
  }

  @Override
  public void sendResetPasswordEmail(User user, String plainToken) {
    logger.info("Attempting to send password reset email to {}", user.getEmail());
    try {
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
      String messageContent = getTranslation(
          "resetPasswordEmailContent",
          passwordResetLinkExpiresAfterDays,
          domain,
          plainToken
      );

      helper.setText(messageContent, true);
      helper.setFrom(passwordResetMailFrom);
      helper.setTo(user.getEmail());
      helper.setSubject(getTranslation("resetPasswordEmailTitle"));
      emailSender.send(message);
    } catch (MessagingException e) {
      logger.error("Error when sending message", e);
    }
  }

  private String getTranslation(String key, String... args) {
    return messageSource.getMessage(key, getParams(args), LocaleContextHolder.getLocale());
  }

  private Object[] getParams(String... args) {
    return Arrays.stream(args).toArray();
  }
}
