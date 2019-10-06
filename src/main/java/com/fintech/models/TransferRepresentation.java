package com.fintech.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TransferRepresentation {

  private Long id;
  private String accountFrom;
  private String accountTo;
  private BigDecimal amount;
  private LocalDateTime created;

}
