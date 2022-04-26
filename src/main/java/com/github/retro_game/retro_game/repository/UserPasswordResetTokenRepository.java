package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.entity.UserPasswordResetToken;
import com.github.retro_game.retro_game.entity.UserPasswordResetTokenKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPasswordResetTokenRepository extends JpaRepository<UserPasswordResetToken, UserPasswordResetTokenKey> {
    Optional<UserPasswordResetToken> findByKey_User(User user);
}
