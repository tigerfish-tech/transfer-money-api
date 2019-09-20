package com.fintech.models;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransferOperation {

  private String accountFrom;
  private String accountTo;
  private BigDecimal amount;

}
