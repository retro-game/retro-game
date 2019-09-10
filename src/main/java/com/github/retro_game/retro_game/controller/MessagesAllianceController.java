package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.form.SendAllianceMessageForm;
import com.github.retro_game.retro_game.service.AllianceMessagesService;
import com.github.retro_game.retro_game.service.dto.AllianceMessageDto;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Controller
@Validated
public class MessagesAllianceController {
  private final AllianceMessagesService allianceMessagesService;

  public MessagesAllianceController(AllianceMessagesService allianceMessagesService) {
    this.allianceMessagesService = allianceMessagesService;
  }

  @GetMapping("/messages/alliance")
  public String messages(@RequestParam(name = "body") long bodyId,
                         @RequestParam(required = false, defaultValue = "1") @Valid @Min(1) int page,
                         @RequestParam(required = false, defaultValue = "10") @Valid @Range(min = 1, max = 1000) int size,
                         Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("page", page);
    model.addAttribute("size", size);

    PageRequest pageRequest = PageRequest.of(page - 1, size);
    List<AllianceMessageDto> messages = allianceMessagesService.getCurrentUserAllianceMessages(bodyId, pageRequest);
    model.addAttribute("messages", messages);

    return "messages-alliance";
  }

  @GetMapping("/messages/alliance/send")
  public String send(@RequestParam(name = "body") long bodyId,
                     @RequestParam(name = "alliance") long allianceId,
                     Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("allianceId", allianceId);
    return "messages-alliance-send";
  }

  @PostMapping("/messages/alliance/send")
  public String doSend(@Valid SendAllianceMessageForm sendAllianceMessageForm) {
    allianceMessagesService.send(sendAllianceMessageForm.getBody(), sendAllianceMessageForm.getAlliance(),
        sendAllianceMessageForm.getMessage());
    return "redirect:/messages/alliance?body=" + sendAllianceMessageForm.getBody();
  }
}
