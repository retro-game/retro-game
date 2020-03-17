package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.dto.BodyTypeAndImagePairDto;
import com.github.retro_game.retro_game.dto.BodyTypeDto;
import com.github.retro_game.retro_game.dto.CoordinatesKindDto;
import com.github.retro_game.retro_game.service.BodyService;
import org.hibernate.validator.constraints.Range;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Validated
public class BodySettingsChangeImageController {
  private final BodyService bodyService;

  public BodySettingsChangeImageController(BodyService bodyService) {
    this.bodyService = bodyService;
  }

  @GetMapping("/body-settings/change-image")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  public String changeImage(@RequestParam(name = "body") long bodyId, Model model) {
    BodyTypeAndImagePairDto pair = bodyService.getBodyTypeAndImagePair(bodyId);
    BodyTypeDto type = pair.getType();
    int image = pair.getImage();

    boolean isPlanet = bodyService.getBodyBasicInfo(bodyId).getCoordinates().getKind() == CoordinatesKindDto.PLANET;

    model.addAttribute("bodyId", bodyId);
    model.addAttribute("type", type);
    model.addAttribute("image", image);
    model.addAttribute("isPlanet", isPlanet);
    return "body-settings-change-image";
  }

  @PostMapping("/body-settings/change-image")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  public String doChangeImage(@RequestParam(name = "body") long bodyId,
                              @RequestParam @Range(min = 1, max = 10) int image) {
    bodyService.setImage(bodyId, image);
    return "redirect:/overview?body=" + bodyId;
  }
}
