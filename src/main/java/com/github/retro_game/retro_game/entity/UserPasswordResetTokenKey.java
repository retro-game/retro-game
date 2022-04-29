package com.github.retro_game.retro_game.entity;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordResetTokenKey implements Serializable {

  @JoinColumn(name = "user_id", nullable = false, unique = true, updatable = false)
  @OneToOne(fetch = FetchType.LAZY)
  private User user;
}
