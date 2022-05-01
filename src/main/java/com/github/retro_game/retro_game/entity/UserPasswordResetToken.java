package com.github.retro_game.retro_game.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_password_reset_tokens")
public class UserPasswordResetToken {

  @EmbeddedId
  private UserPasswordResetTokenKey key;

  @Column(name = "encrypted_token", nullable = false)
  private String encryptedToken;

  @Column(name = "expire_at", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date expireAt;
}
