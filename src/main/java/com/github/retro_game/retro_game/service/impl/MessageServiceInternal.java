package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.User;
import com.github.retro_game.retro_game.service.MessageService;

import java.util.List;

interface MessageServiceInternal extends MessageService {
  void sendToMultipleUsers(List<User> recipients, String message);
}
