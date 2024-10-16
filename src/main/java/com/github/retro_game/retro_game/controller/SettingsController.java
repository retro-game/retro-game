package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.controller.form.SettingsForm;
import com.github.retro_game.retro_game.controller.validator.SettingsFormValidator;
import com.github.retro_game.retro_game.dto.UserSettingsDto;
import com.github.retro_game.retro_game.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

@Controller
public class SettingsController {
  private final UserService userService;
  private final SettingsFormValidator settingsFormValidator;
  private final List<String> languages;
  private final List<String> skins;

  public SettingsController(@Value("${retro-game.languages}") String languages,
                            @Value("${retro-game.skins}") String skins,
                            UserService userService,
                            SettingsFormValidator settingsFormValidator) {
    this.userService = userService;
    this.settingsFormValidator = settingsFormValidator;
    this.languages = List.of(languages.split(","));
    this.skins = List.of(skins.split(","));
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.addValidators(settingsFormValidator);
  }

  @GetMapping("/settings")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String settings(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    model.addAttribute("languages", languages);
    model.addAttribute("skins", skins);
    return "settings";
  }

  @PostMapping("/settings")
  @PreAuthorize("hasPermission(#form.body, 'ACCESS')")
  @Activity(bodies = "#form.body")
  public String saveSettings(@Valid SettingsForm form) {
    UserSettingsDto settings = new UserSettingsDto(form.getLanguage(), form.getSkin(), form.getNumProbes(),
        form.getBodiesSortOrder(), form.getBodiesSortDirection(), form.isNumberInputScrollingEnabled(),
        form.isShowNewMessagesInOverviewEnabled(), form.isShowNewReportsInOverviewEnabled(),
        form.isStickyMoonsEnabled());
    userService.saveCurrentUserSettings(settings);
    return "redirect:/settings?body=" + form.getBody();
  }
}
