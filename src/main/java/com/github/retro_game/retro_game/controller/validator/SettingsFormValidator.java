package com.github.retro_game.retro_game.controller.validator;

import com.github.retro_game.retro_game.controller.form.SettingsForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class SettingsFormValidator implements Validator {
  private final List<String> languages;
  private final List<String> skins;

  public SettingsFormValidator(@Value("${retro-game.languages}") String languages,
                               @Value("${retro-game.skins}") String skins) {
    this.languages = Collections.unmodifiableList(Arrays.asList(languages.split(",")));
    this.skins = Collections.unmodifiableList(Arrays.asList(skins.split(",")));
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return SettingsForm.class.equals(clazz);
  }

  @Override
  public void validate(Object o, Errors errors) {
    SettingsForm form = (SettingsForm) o;
    if (!languages.contains(form.getLanguage())) {
      errors.rejectValue("language", "invalidLanguage");
    }
    if (!skins.contains(form.getSkin())) {
      errors.rejectValue("skin", "invalidSkin");
    }
  }
}
