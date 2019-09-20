package com.fintech.services.impl;

import com.fintech.dao.OperationDao;
import com.fintech.dao.TransferDao;
import com.fintech.models.Account;
import com.fintech.models.TransferOperation;
import com.fintech.models.dao.OperationDaoEntity;
import com.fintech.models.dao.TransferDaoEntity;
import com.fintech.services.AccountService;
import com.fintech.services.TransactionService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class DefaultTransactionService implements TransactionService {

  private OperationDao<OperationDaoEntity, Long> operationDao;
  private TransferDao<TransferDaoEntity, Long> transferDao;
  private AccountService accountService;

  public DefaultTransactionService(OperationDao<OperationDaoEntity, Long> operationDao,
                                   TransferDao<TransferDaoEntity, Long> transferDao,
                                   AccountService accountService) {
    this.operationDao = operationDao;
    this.transferDao = transferDao;
    this.accountService = accountService;
  }

  @Override
  public void cashIn(String account, BigDecimal amount) {
    if (!isAccountExist(account)) {
      throw new IllegalArgumentException("Account " + account + " doesn't exist");
    }

    OperationDaoEntity operationDaoEntity = new OperationDaoEntity();
    operationDaoEntity.setAccountNumber(account);
    operationDaoEntity.setDebit(amount);

    operationDao.insert(operationDaoEntity);
  }

  @Override
  public void withdraw(String account, BigDecimal amount) {
    if (!isAccountExist(account)) {
      throw new IllegalArgumentException("Account " + account + " doesn't exist");
    }
    if (!isMoneyEnough(account, amount)) {
      throw new IllegalArgumentException("There is no enough money");
    }

    OperationDaoEntity operationDaoEntity = new OperationDaoEntity();
    operationDaoEntity.setAccountNumber(account);
    operationDaoEntity.setCredit(amount);

    operationDao.insert(operationDaoEntity);
  }

  @Override
  public BigDecimal balance(String account) {
    if (!isAccountExist(account)) {
      throw new IllegalArgumentException("Account " + account + " doesn't exist");
    }

    return operationDao.accountBalance(account);
  }

  @Override
  public void transfer(TransferOperation operation) {
    if (!isAccountExist(operation.getAccountFrom())) {
      throw new IllegalArgumentException(
          "Account " + operation.getAccountFrom() + " doesn't exist");
    }
    if (!isAccountExist(operation.getAccountTo())) {
      throw new IllegalArgumentException(
          "Account " + operation.getAccountTo() + " doesn't exist");
    }
    if (!isMoneyEnough(operation.getAccountFrom(), operation.getAmount())) {
      throw new IllegalArgumentException("There is no enough money");
    }
    Account accountFrom = accountService.getByNumber(operation.getAccountFrom());
    Account accountTo = accountService.getByNumber(operation.getAccountTo());

    if (!accountFrom.getCurrency().equals(accountTo.getCurrency())) {
      throw new IllegalArgumentException("Accounts with different currencies");
    }

    OperationDaoEntity from = operationDao.insert(
        createOperation(operation.getAccountFrom(), null, operation.getAmount()));
    OperationDaoEntity to = operationDao.insert(
        createOperation(operation.getAccountTo(), operation.getAmount(), null));

    List<Long> operations = Arrays.asList(from.getId(), to.getId());

    TransferDaoEntity transferDaoEntity = new TransferDaoEntity();
    transferDaoEntity.setOperations(operations);

    transferDao.insert(transferDaoEntity);
  }

  private boolean isAccountExist(String account) {
    return accountService.exists(account);
  }

  private boolean isMoneyEnough(String account, BigDecimal amount) {
    BigDecimal balance = operationDao.accountBalance(account);

    return balance.compareTo(amount) >= 0;
  }

  private OperationDaoEntity createOperation(String account, BigDecimal debit, BigDecimal credit) {
    OperationDaoEntity entity = new OperationDaoEntity();
    entity.setAccountNumber(account);
    entity.setDebit(debit);
    entity.setCredit(credit);

    return entity;
  }

}
