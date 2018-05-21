package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.form.DeleteMessageForm;
import com.github.retro_game.retro_game.controller.form.DeleteMessageResponse;
import com.github.retro_game.retro_game.controller.form.SendMessageForm;
import com.github.retro_game.retro_game.controller.form.SendSpamForm;
import com.github.retro_game.retro_game.service.MessageService;
import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.service.dto.MessageDto;
import com.github.retro_game.retro_game.service.exception.MessageDoesntExistException;
import com.github.retro_game.retro_game.service.exception.UnauthorizedMessageAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Controller
@Validated
public class MessagesController {
  private final MessageService messageService;
  private final UserService userService;

  public MessagesController(MessageService messageService, UserService userService) {
    this.messageService = messageService;
    this.userService = userService;
  }

  @GetMapping("/messages")
  public String messages(@RequestParam(name = "body") long bodyId,
                         @RequestParam(required = false, defaultValue = "1") @Valid @Min(1) int page,
                         @RequestParam(required = false, defaultValue = "10") @Valid @Min(1) int size,
                         Model model) {
    PageRequest pageRequest = PageRequest.of(page - 1, size);
    List<MessageDto> messages = messageService.getMessages(bodyId, pageRequest);
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("page", page);
    model.addAttribute("size", size);
    model.addAttribute("messages", messages);
    return "messages";
  }

  @GetMapping("/messages/send")
  public String sendMessage(@RequestParam(name = "body") long bodyId,
                            @RequestParam(name = "recipient") long recipientId,
                            Model model) {
    String recipientName = userService.getName(recipientId);
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("recipientId", recipientId);
    model.addAttribute("recipientName", recipientName);
    return "messages-send";
  }

  @PostMapping("/messages/send")
  public String doSendMessage(@RequestParam(name = "body") long bodyId, @Valid SendMessageForm sendMessageForm) {
    messageService.send(bodyId, sendMessageForm.getRecipient(), sendMessageForm.getMessage());
    return "redirect:/messages?body=" + bodyId;
  }

  @GetMapping("/messages/send-spam")
  public String sendSpam(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    return "messages-send-spam";
  }

  @PostMapping("/messages/send-spam")
  public String doSendSpam(@RequestParam(name = "body") long bodyId, @Valid SendSpamForm sendSpamForm) {
    messageService.sendSpam(bodyId, sendSpamForm.getMessage());
    return "redirect:/messages?body=" + bodyId;
  }

  @PostMapping("/messages/delete")
  @ResponseBody
  public DeleteMessageResponse reportsEspionageDelete(@RequestBody @Valid DeleteMessageForm form) {
    DeleteMessageResponse response = new DeleteMessageResponse();
    try {
      messageService.delete(form.getBodyId(), form.getMessageId());
      response.setSuccess(true);
    } catch (MessageDoesntExistException | UnauthorizedMessageAccessException e) {
      response.setSuccess(false);
    }
    return response;
  }
}
