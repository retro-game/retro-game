package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.form.JoinForm;
import com.github.retro_game.retro_game.controller.validator.JoinFormValidator;
import com.github.retro_game.retro_game.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class JoinController {
  private final UserService userService;
  private final JoinFormValidator joinFormValidator;

  public JoinController(UserService userService, JoinFormValidator joinFormValidator) {
    this.userService = userService;
    this.joinFormValidator = joinFormValidator;
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.addValidators(joinFormValidator);
  }

  @GetMapping("/join")
  public String join(JoinForm joinForm) {
    return "join";
  }

  @PostMapping("/join")
  public String doJoin(@Valid JoinForm joinForm, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return "join";
    }
    userService.create(joinForm.getEmail(), joinForm.getName(), joinForm.getPassword());
    return "redirect:/?joined";
  }
}
