package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.service.BodyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Controller
@Validated
public class BodySettingsRenameController {
  private final BodyService bodyService;

  public BodySettingsRenameController(BodyService bodyService) {
    this.bodyService = bodyService;
  }

  @GetMapping("/body-settings/rename")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  public String rename(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    return "body-settings-rename";
  }

  @PostMapping("/body-settings/rename")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  public String doRename(@RequestParam(name = "body") long bodyId,
                         @RequestParam @NotNull @Size(min = 1, max = 16) @Pattern(regexp = "^[0-9A-Za-z\\-._]+( ?[0-9A-Za-z\\-._])*$") String name) {
    bodyService.rename(bodyId, name);
    return "redirect:/overview?body=" + bodyId;
  }
}
