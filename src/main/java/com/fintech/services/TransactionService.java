package com.fintech.services;

import com.fintech.models.TransferOperation;
import java.math.BigDecimal;

public interface TransactionService {

  void cashIn(String account, BigDecimal amount);

  void withdraw(String account, BigDecimal amount);

  BigDecimal balance(String account);

  void transfer(TransferOperation operation);

}
