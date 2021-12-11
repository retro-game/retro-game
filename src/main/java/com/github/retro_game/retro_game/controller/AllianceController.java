package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.dto.*;
import com.github.retro_game.retro_game.service.AllianceService;
import com.github.retro_game.retro_game.service.UserService;
import org.hibernate.validator.constraints.URL;
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
import java.util.List;

@Controller
@Validated
public class AllianceController {
  private final AllianceService allianceService;
  private final UserService userService;

  public AllianceController(AllianceService allianceService, UserService userService) {
    this.allianceService = allianceService;
    this.userService = userService;
  }

  @GetMapping("/alliance")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String alliance(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));

    // Check whether the user already has an alliance.
    AllianceDto alliance = allianceService.getCurrentUserAlliance(bodyId);
    if (alliance != null) {
      model.addAttribute("alliance", alliance);
      return "alliance-view";
    }

    // Check whether the user is applying to an alliance.
    AllianceApplicationDto application = allianceService.getCurrentUserApplication(bodyId);
    if (application != null) {
      model.addAttribute("app", application);
      return "alliance-application";
    }

    // No alliance, ask user to create one.
    return "alliance-create";
  }

  @PostMapping("/alliance/create")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String create(@RequestParam(name = "body") long bodyId,
                       @RequestParam @NotNull @Size(min = 3, max = 8) @Pattern(regexp = "^[A-Za-z0-9]+( ?[A-Za-z0-9])*$") String tag,
                       @RequestParam @NotNull @Size(min = 3, max = 16) @Pattern(regexp = "^[0-9A-Za-z\\-._]+( ?[0-9A-Za-z\\-._])*$") String name) {
    allianceService.create(bodyId, tag, name);
    return "redirect:/alliance?body=" + bodyId;
  }

  @GetMapping("/alliance/view")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String view(@RequestParam(name = "body") long bodyId,
                     @RequestParam(name = "alliance") long allianceId,
                     Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    AllianceDto alliance = allianceService.getById(bodyId, allianceId);
    model.addAttribute("alliance", alliance);
    return "alliance-view";
  }

  @GetMapping("/alliance/members")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String members(@RequestParam(name = "body") long bodyId,
                        @RequestParam(name = "alliance") long allianceId,
                        Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    List<AllianceMemberDto> members = allianceService.getMembers(bodyId, allianceId);
    model.addAttribute("members", members);
    return "alliance-members";
  }

  @GetMapping("/alliance/leave")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String leave(@RequestParam(name = "body") long bodyId,
                      @RequestParam(name = "alliance") long allianceId,
                      Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    model.addAttribute("allianceId", allianceId);
    return "alliance-leave";
  }

  @PostMapping("/alliance/leave")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String doLeave(@RequestParam(name = "body") long bodyId,
                        @RequestParam(name = "alliance") long allianceId) {
    allianceService.leave(bodyId, allianceId);
    return "redirect:/alliance?body=" + bodyId;
  }

  // Applications.

  @GetMapping("/alliance/apply")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String apply(@RequestParam(name = "body") long bodyId,
                      @RequestParam(name = "alliance") long allianceId,
                      Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    model.addAttribute("allianceId", allianceId);
    String applicationText = allianceService.getText(bodyId, allianceId, AllianceTextKindDto.APPLICATION);
    model.addAttribute("applicationText", applicationText);
    return "alliance-apply";
  }

  @PostMapping("/alliance/apply")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String doApply(@RequestParam(name = "body") long bodyId,
                        @RequestParam(name = "alliance") long allianceId,
                        @RequestParam(name = "application-text") @NotNull @Size(max = 10000) String text) {
    allianceService.apply(bodyId, allianceId, text);
    return "redirect:/alliance?body=" + bodyId;
  }

  @PostMapping("/alliance/cancel-application")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String cancelApplication(@RequestParam(name = "body") long bodyId) {
    allianceService.cancelCurrentUserApplication(bodyId);
    return "redirect:/alliance?body=" + bodyId;
  }

  @GetMapping("/alliance/applications")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String applications(@RequestParam(name = "body") long bodyId,
                             @RequestParam(name = "alliance") long allianceId,
                             Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    AllianceApplicationListDto list = allianceService.getApplications(bodyId, allianceId);
    model.addAttribute("list", list);
    return "alliance-applications";
  }

  @PostMapping("/alliance/applications/accept")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String applicationAccept(@RequestParam(name = "body") long bodyId,
                                  @RequestParam(name = "application") long applicationId) {
    allianceService.acceptApplication(bodyId, applicationId);
    return "redirect:/alliance?body=" + bodyId;
  }

  @PostMapping("/alliance/applications/reject")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String applicationReject(@RequestParam(name = "body") long bodyId,
                                  @RequestParam(name = "application") long applicationId) {
    allianceService.rejectApplication(bodyId, applicationId);
    return "redirect:/alliance?body=" + bodyId;
  }

  // Management.

  @GetMapping("/alliance/manage")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String manage(@RequestParam(name = "body") long bodyId,
                       @RequestParam(name = "alliance") long allianceId,
                       Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    model.addAttribute("allianceId", allianceId);
    return "alliance-manage";
  }

  @GetMapping("/alliance/manage/members")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String manageMembers(@RequestParam(name = "body") long bodyId,
                              @RequestParam(name = "alliance") long allianceId,
                              Model model) {
    // FIXME: Kick action should be visible only for users with corresponding privileges.
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    model.addAttribute("allianceId", allianceId);
    List<AllianceMemberDto> members = allianceService.getMembers(bodyId, allianceId);
    model.addAttribute("members", members);
    return "alliance-manage-members";
  }

  @PostMapping("/alliance/manage/members/kick")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String manageMembersKick(@RequestParam(name = "body") long bodyId,
                                  @RequestParam(name = "alliance") long allianceId,
                                  @RequestParam(name = "user") long userId) {
    allianceService.kickUser(bodyId, allianceId, userId);
    return "redirect:/alliance/manage/members?body=" + bodyId + "&alliance=" + allianceId;
  }

  @GetMapping("/alliance/manage/logo")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String manageLogo(@RequestParam(name = "body") long bodyId,
                           @RequestParam(name = "alliance") long allianceId,
                           Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    model.addAttribute("allianceId", allianceId);
    AllianceDto alliance = allianceService.getById(bodyId, allianceId);
    model.addAttribute("url", alliance.getLogo());
    return "alliance-manage-logo";
  }

  @PostMapping("/alliance/manage/logo/save")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String manageLogoSave(@RequestParam(name = "body") long bodyId,
                               @RequestParam(name = "alliance") long allianceId,
                               @RequestParam @URL @Size(max = 128) String url) {
    allianceService.saveLogo(bodyId, allianceId, url);
    return "redirect:/alliance/manage?body=" + bodyId + "&alliance=" + allianceId;
  }

  @GetMapping("/alliance/manage/text")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String manageText(@RequestParam(name = "body") long bodyId,
                           @RequestParam(name = "alliance") long allianceId,
                           @RequestParam @NotNull AllianceTextKindDto kind,
                           Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    model.addAttribute("allianceId", allianceId);
    model.addAttribute("kind", kind);
    String text = allianceService.getText(bodyId, allianceId, kind);
    model.addAttribute("text", text);
    return "alliance-manage-text";
  }

  @PostMapping("/alliance/manage/text/save")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String manageTextSave(@RequestParam(name = "body") long bodyId,
                               @RequestParam(name = "alliance") long allianceId,
                               @RequestParam @NotNull AllianceTextKindDto kind,
                               @RequestParam @NotNull @Size(max = 10000) String text) {
    allianceService.saveText(bodyId, allianceId, kind, text);
    return "redirect:/alliance/manage?body=" + bodyId + "&alliance=" + allianceId;
  }

  @GetMapping("/alliance/manage/disband")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String manageDisband(@RequestParam(name = "body") long bodyId,
                              @RequestParam(name = "alliance") long allianceId,
                              Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("ctx", userService.getCurrentUserContext(bodyId));
    model.addAttribute("allianceId", allianceId);
    return "alliance-manage-disband";
  }

  @PostMapping("/alliance/manage/disband")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String manageDoDisband(@RequestParam(name = "body") long bodyId,
                                @RequestParam(name = "alliance") long allianceId,
                                @NotNull String password) {
    allianceService.disband(bodyId, allianceId, password);
    return "redirect:/alliance?body=" + bodyId;
  }
}
