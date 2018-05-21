package com.github.retro_game.retro_game.controller.validator;

import com.github.retro_game.retro_game.controller.form.JoinForm;
import com.github.retro_game.retro_game.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class JoinFormValidator implements Validator {
  private final UserService userService;

  public JoinFormValidator(UserService userService) {
    this.userService = userService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return JoinForm.class.equals(clazz);
  }

  @Override
  public void validate(Object o, Errors errors) {
    JoinForm form = (JoinForm) o;
    // FIXME: query db only when the fields are valid.
    if (userService.existsByEmailIgnoreCase(form.getEmail())) {
      errors.rejectValue("email", "exists");
    }
    if (userService.existsByNameIgnoreCase(form.getName())) {
      errors.rejectValue("name", "exists");
    }
  }
}
