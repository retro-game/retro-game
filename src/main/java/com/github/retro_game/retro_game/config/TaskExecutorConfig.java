package com.github.retro_game.retro_game.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class TaskExecutorConfig {
  @Bean
  TaskExecutor gameTaskSchedulerThread() {
    return new SimpleAsyncTaskExecutor();
  }
}
