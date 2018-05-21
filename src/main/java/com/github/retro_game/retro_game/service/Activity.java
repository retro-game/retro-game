package com.github.retro_game.retro_game.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Annotation to handle user's 'clicks'. When a method is annotated, the service will update the activity of the current
// user and the specified bodies.
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Activity {
  String[] bodies();
}
