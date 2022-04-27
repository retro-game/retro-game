package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.entity.User;

public interface EmailService {

  void sendResetPasswordEmail(User user, String plainToken);
}
