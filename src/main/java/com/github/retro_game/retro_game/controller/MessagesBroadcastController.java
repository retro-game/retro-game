package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.controller.form.SendBroadcastMessageForm;
import com.github.retro_game.retro_game.dto.BroadcastMessageDto;
import com.github.retro_game.retro_game.service.BroadcastMessageService;
import com.github.retro_game.retro_game.service.MessagesSummaryService;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
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
public class MessagesBroadcastController {
  private final BroadcastMessageService broadcastMessageService;
  private final MessagesSummaryService messagesSummaryService;

  public MessagesBroadcastController(BroadcastMessageService broadcastMessageService,
                                     MessagesSummaryService messagesSummaryService) {
    this.broadcastMessageService = broadcastMessageService;
    this.messagesSummaryService = messagesSummaryService;
  }

  @GetMapping("/messages/broadcast")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String messages(@RequestParam(name = "body") long bodyId,
                         @RequestParam(required = false, defaultValue = "1") @Min(1) int page,
                         @RequestParam(required = false, defaultValue = "10") @Range(min = 1, max = 1000) int size,
                         Model model) {
    PageRequest pageRequest = PageRequest.of(page - 1, size);
    List<BroadcastMessageDto> messages = broadcastMessageService.getMessages(bodyId, pageRequest);

    model.addAttribute("bodyId", bodyId);
    model.addAttribute("summary", messagesSummaryService.get(bodyId));
    model.addAttribute("page", page);
    model.addAttribute("size", size);
    model.addAttribute("messages", messages);

    return "messages-broadcast";
  }

  @GetMapping("/messages/broadcast/send")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String send(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    return "messages-broadcast-send";
  }

  @PostMapping("/messages/broadcast/send")
  @PreAuthorize("hasPermission(#form.body, 'ACCESS')")
  @Activity(bodies = "#form.body")
  public String doSend(@Valid SendBroadcastMessageForm form) {
    broadcastMessageService.send(form.getBody(), form.getMessage());
    return "redirect:/messages/broadcast?body=" + form.getBody();
  }
}
