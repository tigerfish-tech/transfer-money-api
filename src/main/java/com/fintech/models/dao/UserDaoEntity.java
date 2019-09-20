package com.fintech.models.dao;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDaoEntity {

  private String id;
  private String fullName;
  private LocalDateTime created;

}
