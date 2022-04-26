package com.github.retro_game.retro_game.controller.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ResetPasswordRequestForm {
    @NotNull
    @Size(min = 3, max = 128)
    @Email
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
