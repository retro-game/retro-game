package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.PrivateMessageDto;
import com.github.retro_game.retro_game.dto.PrivateMessageKindDto;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface PrivateMessageService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void send(long bodyId, long recipientId, String message);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<PrivateMessageDto> getMessages(long bodyId, PrivateMessageKindDto kind, @Nullable Long correspondentId,
                                      Pageable pageable);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void delete(long bodyId, PrivateMessageKindDto kind, long messageId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void deleteAll(long bodyId, PrivateMessageKindDto kind);
}
