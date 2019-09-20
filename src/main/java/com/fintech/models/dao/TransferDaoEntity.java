package com.fintech.models.dao;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferDaoEntity {

  private Long id;
  private LocalDateTime created;
  private List<Long> operations;

}
