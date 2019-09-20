package com.fintech.models.dao;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationDaoEntity {

  private Long id;
  private String accountNumber;
  private BigDecimal debit;
  private BigDecimal credit;

}
