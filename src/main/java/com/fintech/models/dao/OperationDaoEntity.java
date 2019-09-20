package com.fintech.models.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationDaoEntity {

  private Long id;
  private String accountNumber;
  private BigDecimal debit;
  private BigDecimal credit;
  private LocalDateTime created;

}
