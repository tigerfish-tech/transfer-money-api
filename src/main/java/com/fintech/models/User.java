package com.fintech.models;

import com.fintech.models.dao.UserDaoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class User {
  private String id;
  private String fullName;

  public static User valueOf(UserDaoEntity entity) {
    return new User(entity.getId(), entity.getFullName());
  }

}
