package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.cache.UserInfoCache;
import com.github.retro_game.retro_game.service.PushDetectionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashSet;

@Controller
public class AdminPushDetectionController {
  private final UserInfoCache userInfoCache;
  private final PushDetectionService pushDetectionService;

  public AdminPushDetectionController(UserInfoCache userInfoCache, PushDetectionService pushDetectionService) {
    this.userInfoCache = userInfoCache;
    this.pushDetectionService = pushDetectionService;
  }

  @GetMapping("/admin/push-detection")
  public String pushDetection(Model model) {
    var pushes = pushDetectionService.findPushes();

    var userIds = new HashSet<Long>();
    for (var push : pushes) {
      var firstReport = push.get(0).reportAndPoints().report();
      userIds.add(firstReport.userId());
      userIds.add(firstReport.partnerId());
    }
    var userInfos = userInfoCache.getAll(userIds);

    model.addAttribute("pushes", pushes);
    model.addAttribute("userInfos", userInfos);
    return "admin-push-detection";
  }
}
