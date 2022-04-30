package com.github.retro_game.retro_game.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record GoogleRecaptchaResponseDto(
    @JsonProperty("success") boolean success,
    @JsonProperty("challenge_ts") String challengeTs,
    @JsonProperty("hostname") String hostname,
    @JsonProperty("error-codes") ErrorCode[] errorCodes
) {
  enum ErrorCode {
    MISSING_SECRET, INVALID_SECRET, MISSING_RESPONSE, INVALID_RESPONSE;

    private static final Map<String, ErrorCode> errors = Map.of(
        "missing-input-secret", MISSING_SECRET,
        "invalid-input-secret", INVALID_SECRET,
        "missing-input-response", MISSING_RESPONSE,
        "invalid-input-response", INVALID_RESPONSE
    );

    @JsonCreator
    public static ErrorCode forValue(String value) {
      return errors.get(value.toLowerCase());
    }
  }
}
