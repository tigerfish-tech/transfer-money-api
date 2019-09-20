package com.fintech.services;

import java.math.BigDecimal;

public interface TransactionService {

  void cashIn(String account, BigDecimal amount);

  void withdraw(String account, BigDecimal amount);

  BigDecimal balance(String account);

}
