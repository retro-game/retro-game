package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.service.MessageService;
import org.springframework.stereotype.Service;

@Service("messageService")
class MessageServiceImpl implements MessageService {
  @Override
  public int getNumNewMessages(long bodyId) {
    return 0;
  }
}
