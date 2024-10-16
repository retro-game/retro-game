package com.github.retro_game.retro_game.controller.activity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// An annotation to handle user activity. Annotate a controller method specifying which bodies should be updated.
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Activity {
  String[] bodies();
}
