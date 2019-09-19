package com.fintech.models;

import com.fintech.models.dao.AccountDaoEntity;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Account {

  private String number;
  private String currency;

  public static Account valueOf(AccountDaoEntity entity) {
    return Account.builder().number(entity.getNumber()).currency(entity.getCurrency()).build();
  }

}
