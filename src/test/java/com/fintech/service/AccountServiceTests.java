package com.fintech.service;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fintech.dao.AccountDao;
import com.fintech.models.Account;
import com.fintech.models.dao.AccountDaoEntity;
import com.fintech.services.AccountService;
import com.fintech.services.UserService;
import com.fintech.services.impl.DefaultAccountService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTests {

  private AccountService accountService;
  private AccountDao<AccountDaoEntity, String> accountDao;
  private UserService userService;

  @Before
  public void startUp() {
    accountDao = mock(AccountDao.class);
    userService = mock(UserService.class);
    accountService = new DefaultAccountService(accountDao, userService);
  }

  //Add new account to user
  @Test
  public void createNewAccountSuccessTest() {
    String userId = UUID.randomUUID().toString();

    AccountDaoEntity entity = new AccountDaoEntity();
    entity.setUserId(userId);
    entity.setNumber("123456789");
    entity.setCurrency("USD");

    given(userService.exists(userId)).willReturn(true);
    given(accountDao.insert(any())).willReturn(entity);

    Account result = accountService.addAccountToUser(userId, "USD");

    verify(userService, times(1)).exists(any());
    verify(accountDao, times(1)).insert(any());

    MatcherAssert.assertThat("Check number",
        result.getNumber(), is("123456789"));
    MatcherAssert.assertThat("Check currency",
        result.getCurrency(), is("USD"));
  }

  //Add new account to user
  @Test(expected = IllegalArgumentException.class)
  public void createNewAccountNoUserExceptionTest() {
    String userId = UUID.randomUUID().toString();

    given(userService.exists(userId)).willReturn(false);

    accountService.addAccountToUser(userId, "USD");
    verify(userService, times(1)).exists(any());
    verify(accountDao, never()).insert(any());
  }

  //Add new account to user
  @Test(expected = IllegalArgumentException.class)
  public void createNewAccountEmptyCurrencyExceptionTest() {
    String userId = UUID.randomUUID().toString();

    given(userService.exists(userId)).willReturn(true);

    accountService.addAccountToUser(userId, null);
    verify(userService, times(1)).exists(any());
    verify(accountDao, never()).insert(any());
  }

  @Test
  public void isExistPositiveTest() {
    String id = "123456789";

    given(accountDao.isExist(id)).willReturn(true);

    MatcherAssert.assertThat("Check account number",
        accountService.exists(id), is(true));
    verify(accountDao, times(1)).isExist(any());
  }

  @Test
  public void isExistNegativeTest() {
    String id = "123456788";

    given(accountDao.isExist(id)).willReturn(false);

    MatcherAssert.assertThat("Check account number",
        accountService.exists(id), is(false));
    verify(accountDao, times(1)).isExist(any());
  }

  //Get account by number
  @Test
  public void getAccountByNumberSuccessTest() {
    String number = "123456789";
    String currency = "USD";

    AccountDaoEntity entity = new AccountDaoEntity();
    entity.setUserId(UUID.randomUUID().toString());
    entity.setNumber(number);
    entity.setCurrency(currency);

    given(accountDao.isExist(number)).willReturn(true);
    given(accountDao.getById(number)).willReturn(entity);

    Account result = accountService.getByNumber(number);

    verify(accountDao, times(1)).isExist(any());
    verify(accountDao, times(1)).getById(any());
    MatcherAssert.assertThat("Check number",
        result.getNumber(), is(entity.getNumber()));
    MatcherAssert.assertThat("Check currency",
        result.getCurrency(), is(entity.getCurrency()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getAccountByNumberExceptionTest() {
    String number = "123456788";

    given(accountDao.isExist(number)).willReturn(false);

    accountService.getByNumber(number);
    verify(accountDao, times(1)).isExist(any());
    verify(accountDao, never()).getById(any());
  }

  //Delete user by id
  @Test
  public void deleteAccountByNumberSuccessTest() {
    String number = "123456789";
    given(accountDao.isExist(number)).willReturn(true);

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

    accountService.delete(number);

    verify(accountDao, times(1)).isExist(any());
    verify(accountDao, times(1)).deleteById(argumentCaptor.capture());
    MatcherAssert.assertThat("Check param", argumentCaptor.getValue(), is(number));
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteAccountByNumberExceptionTest() {
    String number = "123456788";
    given(accountDao.isExist(number)).willReturn(false);

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

    accountService.delete(number);

    verify(accountDao, times(1)).isExist(argumentCaptor.capture());
    verify(accountDao, never()).deleteById(any());
    MatcherAssert.assertThat("Check param", argumentCaptor.getValue(), is(number));
  }

  @Test
  public void userAccountsSuccessTest() {
    final String userId = UUID.randomUUID().toString();

    List<AccountDaoEntity> accounts = IntStream.range(0, 10)
        .boxed()
        .map(number -> {
          AccountDaoEntity entity = new AccountDaoEntity();
          entity.setUserId(userId);
          entity.setNumber("Number" + number);
          entity.setCurrency("USD");
          return entity;
        })
        .collect(Collectors.toList());

    given(userService.exists(userId)).willReturn(true);
    given(accountDao.userAccounts(userId)).willReturn(accounts);
    MatcherAssert.assertThat("Check user list",
        accountService.userAccounts(userId), hasSize(10));
    verify(accountDao, times(1)).userAccounts(any());
    verify(userService, times(1)).exists(any());
  }

  @Test(expected = IllegalArgumentException.class)
  public void userAccountsExceptionTest() {
    final String userId = UUID.randomUUID().toString();

    given(userService.exists(userId)).willReturn(false);
    MatcherAssert.assertThat("Check user list",
        accountService.userAccounts(userId), hasSize(10));
    verify(accountDao, never()).userAccounts(any());
    verify(userService, times(1)).exists(any());
  }

}
