package com.github.retro_game.retro_game.controller.validator;

import com.github.retro_game.retro_game.controller.form.SettingsForm;
import com.github.retro_game.retro_game.dto.UserContextDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

@Component
public class SettingsFormValidator implements Validator {
  private final List<String> languages;
  private final List<String> skins;

  public SettingsFormValidator(@Value("${retro-game.languages}") String languages,
                               @Value("${retro-game.skins}") String skins) {
    this.languages = List.of(languages.split(","));
    this.skins = List.of(skins.split(","));
  }

  @Override
  public boolean supports(Class<?> clazz) {
    // FIXME: Remove UserContextDto.
    return SettingsForm.class.equals(clazz) || UserContextDto.class.equals(clazz);
  }

  @Override
  public void validate(Object o, Errors errors) {
    if (o instanceof SettingsForm form) {
      if (!languages.contains(form.getLanguage())) {
        errors.rejectValue("language", "invalidLanguage");
      }
      if (!skins.contains(form.getSkin())) {
        errors.rejectValue("skin", "invalidSkin");
      }
    }
  }
}
