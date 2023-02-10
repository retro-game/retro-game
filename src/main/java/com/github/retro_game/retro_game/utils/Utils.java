package com.github.retro_game.retro_game.utils;

import org.springframework.mobile.device.Device;

public class Utils {
  public static String getAppropriateView(Device device, String defaultview) {
    String viewName = defaultview;

		if (device.isMobile()) {
      viewName = "mobile/" + defaultview;
    } else if (device.isTablet()) {
      // viewName = "tablet/" + defaultview;
      viewName = "mobile/" + defaultview;
    }

  return viewName;
  }
}
