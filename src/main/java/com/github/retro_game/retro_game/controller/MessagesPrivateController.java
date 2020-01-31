package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.form.DeleteAllPrivateMessagesForm;
import com.github.retro_game.retro_game.controller.form.DeletePrivateMessageForm;
import com.github.retro_game.retro_game.controller.form.DeletePrivateMessageResponse;
import com.github.retro_game.retro_game.controller.form.SendPrivateMessageForm;
import com.github.retro_game.retro_game.service.MessagesSummaryService;
import com.github.retro_game.retro_game.service.PrivateMessageService;
import com.github.retro_game.retro_game.service.UserService;
import com.github.retro_game.retro_game.service.dto.PrivateMessageDto;
import com.github.retro_game.retro_game.service.dto.PrivateMessageKindDto;
import com.github.retro_game.retro_game.service.exception.PrivateMessageDoesNotExist;
import com.github.retro_game.retro_game.service.exception.UnauthorizedPrivateMessageAccessException;
import org.hibernate.validator.constraints.Range;
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
public class MessagesPrivateController {
  private final MessagesSummaryService messagesSummaryService;
  private final PrivateMessageService privateMessageService;
  private final UserService userService;

  public MessagesPrivateController(MessagesSummaryService messagesSummaryService,
                                   PrivateMessageService privateMessageService, UserService userService) {
    this.messagesSummaryService = messagesSummaryService;
    this.privateMessageService = privateMessageService;
    this.userService = userService;
  }

  @GetMapping("/messages/private")
  public String messages(@RequestParam(name = "body") long bodyId,
                         @RequestParam PrivateMessageKindDto kind,
                         @RequestParam(name = "correspondent", required = false) Long correspondentId,
                         @RequestParam(required = false, defaultValue = "1") @Valid @Min(1) int page,
                         @RequestParam(required = false, defaultValue = "10") @Valid @Range(min = 1, max = 1000) int size,
                         Model model) {
    PageRequest pageRequest = PageRequest.of(page - 1, size);
    List<PrivateMessageDto> messages = privateMessageService.getMessages(bodyId, kind, correspondentId, pageRequest);

    model.addAttribute("bodyId", bodyId);
    model.addAttribute("summary", messagesSummaryService.get(bodyId));
    model.addAttribute("kind", kind);
    model.addAttribute("page", page);
    model.addAttribute("size", size);
    model.addAttribute("messages", messages);

    return "messages-private";
  }

  @GetMapping("/messages/private/send")
  public String send(@RequestParam(name = "body") long bodyId,
                     @RequestParam(name = "recipient") long recipientId,
                     Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("recipientId", recipientId);

    String recipientName = userService.getName(recipientId);
    model.addAttribute("recipientName", recipientName);

    return "messages-private-send";
  }

  @PostMapping("/messages/private/send")
  public String doSend(@Valid SendPrivateMessageForm sendPrivateMessageForm) {
    privateMessageService.send(sendPrivateMessageForm.getBody(), sendPrivateMessageForm.getRecipient(),
        sendPrivateMessageForm.getMessage());
    return "redirect:/messages/private?body=" + sendPrivateMessageForm.getBody() + "&kind=SENT";
  }

  @PostMapping("/messages/private/delete")
  @ResponseBody
  public DeletePrivateMessageResponse delete(@RequestBody @Valid DeletePrivateMessageForm form) {
    DeletePrivateMessageResponse response = new DeletePrivateMessageResponse();
    try {
      privateMessageService.delete(form.getBody(), form.getKind(), form.getMessage());
      response.setSuccess(true);
    } catch (PrivateMessageDoesNotExist | UnauthorizedPrivateMessageAccessException e) {
      response.setSuccess(false);
    }
    return response;
  }

  @PostMapping("/messages/private/delete-all")
  public String deleteAll(@Valid DeleteAllPrivateMessagesForm form) {
    privateMessageService.deleteAll(form.getBody(), form.getKind());
    return "redirect:/messages/private?body=" + form.getBody() + "&kind=" + form.getKind();
  }
}
