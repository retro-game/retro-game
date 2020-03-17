package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.PrivateMessageDto;
import com.github.retro_game.retro_game.dto.PrivateMessageKindDto;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

import java.util.List;

public interface PrivateMessageService {
  @Activity(bodies = "#bodyId")
  void send(long bodyId, long recipientId, String message);

  @Activity(bodies = "#bodyId")
  List<PrivateMessageDto> getMessages(long bodyId, PrivateMessageKindDto kind, @Nullable Long correspondentId,
                                      Pageable pageable);

  @Activity(bodies = "#bodyId")
  void delete(long bodyId, PrivateMessageKindDto kind, long messageId);

  @Activity(bodies = "#bodyId")
  void deleteAll(long bodyId, PrivateMessageKindDto kind);
}
