package com.fintech.services.impl;

import com.fintech.dao.OperationDao;
import com.fintech.models.dao.OperationDaoEntity;
import com.fintech.services.AccountService;
import com.fintech.services.TransactionService;
import java.math.BigDecimal;

public class DefaultTransactionService implements TransactionService {

  private OperationDao<OperationDaoEntity, Long> operationDao;
  private AccountService accountService;

  public DefaultTransactionService(OperationDao<OperationDaoEntity, Long> operationDao,
                                   AccountService accountService) {
    this.operationDao = operationDao;
    this.accountService = accountService;
  }

  @Override
  public void cashIn(String account, BigDecimal amount) {
    if (!accountService.exists(account)) {
      throw new IllegalArgumentException("Account " + account + " doesn't exist");
    }

    OperationDaoEntity operationDaoEntity = new OperationDaoEntity();
    operationDaoEntity.setAccountNumber(account);
    operationDaoEntity.setDebit(amount);

    operationDao.insert(operationDaoEntity);
  }

  @Override
  public void withdraw(String account, BigDecimal amount) {
    if (!accountService.exists(account)) {
      throw new IllegalArgumentException("Account " + account + " doesn't exist");
    }

    BigDecimal balance = operationDao.accountBalance(account);
    if (balance.compareTo(amount) < 0) {
      throw new IllegalArgumentException("There is no enough money");
    }

    OperationDaoEntity operationDaoEntity = new OperationDaoEntity();
    operationDaoEntity.setAccountNumber(account);
    operationDaoEntity.setCredit(amount);

    operationDao.insert(operationDaoEntity);
  }

  @Override
  public BigDecimal balance(String account) {
    if (!accountService.exists(account)) {
      throw new IllegalArgumentException("Account " + account + " doesn't exist");
    }

    return operationDao.accountBalance(account);
  }
}
