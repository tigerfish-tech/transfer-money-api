package com.fintech.services;

import com.fintech.models.TransferOperation;
import com.fintech.models.TransferRepresentation;
import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

  void cashIn(String account, BigDecimal amount);

  void withdraw(String account, BigDecimal amount);

  BigDecimal balance(String account);

  void transfer(TransferOperation operation);

  List<TransferRepresentation> findAll(Integer limit, Integer offset);

  void delete(Long id);

}
