package com.fintech.services.impl;

import com.fintech.dao.AccountDao;
import com.fintech.models.Account;
import com.fintech.models.dao.AccountDaoEntity;
import com.fintech.services.AccountService;
import com.fintech.services.UserService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultAccountService implements AccountService {

  private AccountDao<AccountDaoEntity, String> accountDao;
  private UserService userService;

  public DefaultAccountService(AccountDao<AccountDaoEntity, String> accountDao,
                               UserService userService) {
    this.accountDao = accountDao;
    this.userService = userService;
  }

  @Override
  public boolean exists(String number) {
    return accountDao.isExist(number);
  }

  @Override
  public Account getByNumber(String number) {
    if (!accountDao.isExist(number)) {
      throw new IllegalArgumentException(
          String.format("Account %s doesn't exists", number));
    }

    return Account.valueOf(accountDao.getById(number));
  }

  @Override
  public Account addAccountToUser(String userId, String currency) {
    if (!userService.exists(userId)) {
      throw new IllegalArgumentException("User " + userId + "doesn't exist");
    }
    if (Objects.isNull(currency)) {
      throw new IllegalArgumentException("Currency can't by null");
    }

    AccountDaoEntity entity = new AccountDaoEntity();
    entity.setUserId(userId);
    entity.setCurrency(currency);

    return Account.valueOf(accountDao.insert(entity));
  }

  @Override
  public void delete(String number) {
    if (!accountDao.isExist(number)) {
      throw new IllegalArgumentException("Account " + number + "doesn't exist");
    }

    accountDao.deleteById(number);
  }

  @Override
  public List<Account> userAccounts(String userId) {
    if (!userService.exists(userId)) {
      throw new IllegalArgumentException("User " + userId + "doesn't exist");
    }

    return accountDao.userAccounts(userId).stream()
        .map(Account::valueOf).collect(Collectors.toList());
  }
}
