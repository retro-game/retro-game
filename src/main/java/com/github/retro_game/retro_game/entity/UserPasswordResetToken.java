package com.github.retro_game.retro_game.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_password_reset_tokens")
public class UserPasswordResetToken {

  @EmbeddedId
  private UserPasswordResetTokenKey key;

  @Column(name = "encrypted_token", nullable = false)
  private String encryptedToken;

  @Column(name = "expire_at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date expireAt;

  public UserPasswordResetTokenKey getKey() {
    return key;
  }

  public void setKey(UserPasswordResetTokenKey key) {
    this.key = key;
  }

  public String getEncryptedToken() {
    return encryptedToken;
  }

  public void setEncryptedToken(String encryptedToken) {
    this.encryptedToken = encryptedToken;
  }

  public Date getExpireAt() {
    return expireAt;
  }

  public void setExpireAt(Date expireAt) {
    this.expireAt = expireAt;
  }
}
