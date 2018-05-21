package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.Message;
import com.github.retro_game.retro_game.model.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
  int countByRecipientAndDeletedIsFalseAndAtAfter(User recipient, Date at);

  List<Message> findByRecipientAndDeletedIsFalse(User recipient, Pageable pageable);
}
