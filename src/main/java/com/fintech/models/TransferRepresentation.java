package com.fintech.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransferRepresentation {

  private Long id;
  private String accountFrom;
  private String accountTo;
  private BigDecimal amount;
  private LocalDateTime created;

}
