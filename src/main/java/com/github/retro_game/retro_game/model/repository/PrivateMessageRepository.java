package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.PrivateMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PrivateMessageRepository extends CrudRepository<PrivateMessage, Long> {
  void deleteByRecipientIdAndDeletedBySenderIsTrue(long recipientId);

  void deleteBySenderIdAndDeletedByRecipientIsTrue(long senderId);

  @Transactional
  @Modifying
  @Query(value = "delete from private_messages where at < now() - interval '30 days'", nativeQuery = true)
  void deleteOlderThan30Days();

  List<PrivateMessage> getByRecipientIdAndDeletedByRecipientIsFalseOrderByAtDesc(long recipientId, Pageable pageable);

  List<PrivateMessage> getByRecipientIdAndSenderIdAndDeletedByRecipientIsFalseOrderByAtDesc(long recipientId,
                                                                                            long senderId,
                                                                                            Pageable pageable);

  List<PrivateMessage> getBySenderIdAndDeletedBySenderIsFalseOrderByAtDesc(long senderId, Pageable pageable);

  List<PrivateMessage> getBySenderIdAndRecipientIdAndDeletedBySenderIsFalseOrderByAtDesc(long senderId,
                                                                                         long recipientId,
                                                                                         Pageable pageable);

  @Transactional
  @Modifying
  @Query("update PrivateMessage pm set pm.deletedBySender = true where pm.senderId = ?1")
  void markAllAsDeletedBySender(long senderId);

  @Transactional
  @Modifying
  @Query("update PrivateMessage pm set pm.deletedByRecipient = true where pm.recipientId = ?1")
  void markAllAsDeletedByRecipient(long recipientId);
}
