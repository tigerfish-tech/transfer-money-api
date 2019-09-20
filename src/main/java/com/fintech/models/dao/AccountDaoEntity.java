package com.fintech.models.dao;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDaoEntity {

  private String userId;
  private String number;
  private String currency;
  private LocalDateTime created;

}
