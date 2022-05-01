package com.github.retro_game.retro_game.service;

public interface ResetPasswordService {

  void generateTokenAndSendEmail(String email);

  void resetUserPassword(String resetPasswordToken, String newPassword);
}
