package com.fintech.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
  private String id;
  private String fullName;
}
