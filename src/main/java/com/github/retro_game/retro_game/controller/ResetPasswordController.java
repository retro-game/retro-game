package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.form.ResetPasswordForm;
import com.github.retro_game.retro_game.controller.form.ResetPasswordRequestForm;
import com.github.retro_game.retro_game.service.ResetPasswordService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class ResetPasswordController {
  private final ResetPasswordService resetPasswordService;

  public ResetPasswordController(ResetPasswordService resetPasswordService) {
    this.resetPasswordService = resetPasswordService;
  }

  @GetMapping("/reset-password")
  public String resetPassword(ResetPasswordRequestForm resetPasswordRequestForm) {

    return "reset-password-step-1";
  }

  @GetMapping("/change-password")
  public String changePassword(
      @RequestParam String token,
      ResetPasswordForm resetPasswordForm
  ) {

    return "reset-password-step-2";
  }

  @PostMapping("/reset-password")
  public String doResetPassword(@Valid ResetPasswordRequestForm resetPasswordRequestForm, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return "reset-password-step-1";
    }

    resetPasswordService.generateTokenAndSendEmail(resetPasswordRequestForm.getEmail());
    return "redirect:/?passwordResetSent";
  }

  @PostMapping("/change-password")
  public String doChangePassword(@RequestParam String token, @Valid ResetPasswordForm resetPasswordForm, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return "reset-password-step-2";
    }

    resetPasswordService.resetUserPassword(token, resetPasswordForm.getPassword());
    return "redirect:/?passwordWasChanged";
  }
}
