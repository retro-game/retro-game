package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.form.JoinForm;
import com.github.retro_game.retro_game.controller.validator.JoinFormValidator;
import com.github.retro_game.retro_game.service.CaptchaService;
import com.github.retro_game.retro_game.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.regex.Pattern;

@Controller
public class JoinController {
  private final boolean enableJoinCaptcha;
  private final String googleRecaptchaKeySite;
  private final CaptchaService captchaService;
  private final UserService userService;
  private final JoinFormValidator joinFormValidator;

  public JoinController(@Value("${retro-game.enable-join-captcha}") boolean enableJoinCaptcha,
                        @Value("${retro-game.google-recaptcha-key-site}") String googleRecaptchaKeySite,
                        CaptchaService captchaService,
                        UserService userService,
                        JoinFormValidator joinFormValidator) {
    this.enableJoinCaptcha = enableJoinCaptcha;
    this.googleRecaptchaKeySite = googleRecaptchaKeySite;
    this.captchaService = captchaService;
    this.userService = userService;
    this.joinFormValidator = joinFormValidator;
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.addValidators(joinFormValidator);
  }

  @GetMapping("/join")
  public String join(JoinForm joinForm, Model model) {
    model.addAttribute("enableJoinCaptcha", enableJoinCaptcha);
    model.addAttribute("googleRecaptchaKeySite", googleRecaptchaKeySite);
    model.addAttribute("hasErrors", false);
    model.addAttribute("captchaOk", true);
    return "join";
  }

  @PostMapping("/join")
  public String doJoin(@Valid JoinForm joinForm,
                       BindingResult bindingResult,
                       Model model,
                       HttpServletRequest request) {
    var captchaOk = true;
    if (enableJoinCaptcha) {
      captchaOk = false;
      var recaptchaResponse = request.getParameter("g-recaptcha-response");
      if (recaptchaResponse != null && !recaptchaResponse.isEmpty() && Pattern.matches("^[A-Za-z0-9_-]+$", recaptchaResponse)) {
        captchaOk = captchaService.verify(recaptchaResponse, request.getRemoteAddr());
      }
    }
    if (!captchaOk || bindingResult.hasErrors()) {
      model.addAttribute("enableJoinCaptcha", enableJoinCaptcha);
      model.addAttribute("googleRecaptchaKeySite", googleRecaptchaKeySite);
      model.addAttribute("hasErrors", true);
      model.addAttribute("captchaOk", captchaOk);
      return "join";
    }
    userService.create(joinForm.getEmail(), joinForm.getName(), joinForm.getPassword());
    return "redirect:/?joined";
  }
}
