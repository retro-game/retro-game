package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.PrivateMessageDto;
import com.github.retro_game.retro_game.dto.PrivateMessageKindDto;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

import java.util.List;

public interface PrivateMessageService {
  void send(long bodyId, long recipientId, String message);

  List<PrivateMessageDto> getMessages(long bodyId, PrivateMessageKindDto kind, @Nullable Long correspondentId,
                                      Pageable pageable);

  void delete(long bodyId, PrivateMessageKindDto kind, long messageId);

  void deleteAll(long bodyId, PrivateMessageKindDto kind);
}
