package com.fintech.models;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ErrorResponse {

  private int code;
  private String message;
  private long timestamp;

}
