package com.fintech.service;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fintech.dao.OperationDao;
import com.fintech.models.dao.OperationDaoEntity;
import com.fintech.services.AccountService;
import com.fintech.services.TransactionService;
import com.fintech.services.impl.DefaultTransactionService;
import java.math.BigDecimal;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTests {

  private TransactionService transactionService;
  private AccountService accountService;
  private OperationDao<OperationDaoEntity, Long> operationDao;

  @Before
  public void setUp() {
    accountService = mock(AccountService.class);
    operationDao = mock(OperationDao.class);

    transactionService = new DefaultTransactionService(operationDao, accountService);
  }

  @Test
  public void cashInSuccessTest() {
    String account = "123456789";
    BigDecimal amount = BigDecimal.valueOf(100);

    OperationDaoEntity operationDaoEntity = new OperationDaoEntity();
    operationDaoEntity.setAccountNumber(account);
    operationDaoEntity.setDebit(amount);

    given(accountService.exists(account)).willReturn(true);
    given(operationDao.insert(any())).willReturn(operationDaoEntity);

    transactionService.cashIn(account, amount);
    verify(accountService, times(1)).exists(any());

    ArgumentCaptor<OperationDaoEntity> argumentCaptor
        = ArgumentCaptor.forClass(OperationDaoEntity.class);
    verify(operationDao, times(1)).insert(argumentCaptor.capture());

    OperationDaoEntity arg = argumentCaptor.getValue();
    MatcherAssert.assertThat("Check account", arg.getAccountNumber(), is(account));
    MatcherAssert.assertThat("Check debit", arg.getDebit(), is(amount));
    MatcherAssert.assertThat("Check credit", arg.getCredit(), is(nullValue()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void cashInExceptionTest() {
    String account = "123456788";
    BigDecimal amount = BigDecimal.valueOf(50);
    given(accountService.exists(account)).willReturn(false);

    transactionService.cashIn(account, amount);
    verify(accountService, times(1)).exists(any());
    verify(operationDao, never()).insert(any());
  }

  @Test
  public void withdrawSuccessTest() {
    String account = "123456789";
    BigDecimal amount = BigDecimal.valueOf(50);

    OperationDaoEntity operationDaoEntity = new OperationDaoEntity();
    operationDaoEntity.setAccountNumber(account);
    operationDaoEntity.setCredit(amount);

    given(accountService.exists(account)).willReturn(true);
    given(operationDao.insert(any())).willReturn(operationDaoEntity);
    given(operationDao.accountBalance(account)).willReturn(BigDecimal.valueOf(100));

    transactionService.withdraw(account, amount);
    verify(accountService, times(1)).exists(any());

    ArgumentCaptor<OperationDaoEntity> argumentCaptor
        = ArgumentCaptor.forClass(OperationDaoEntity.class);
    verify(operationDao, times(1)).insert(argumentCaptor.capture());

    OperationDaoEntity arg = argumentCaptor.getValue();
    MatcherAssert.assertThat("Check account", arg.getAccountNumber(), is(account));
    MatcherAssert.assertThat("Check debit", arg.getDebit(), is(nullValue()));
    MatcherAssert.assertThat("Check credit", arg.getCredit(), is(amount));
  }

  @Test(expected = IllegalArgumentException.class)
  public void withdrawExceptionTest() {
    String account = "123456788";
    BigDecimal amount = BigDecimal.valueOf(50);

    given(accountService.exists(account)).willReturn(false);

    transactionService.withdraw(account, amount);
    verify(accountService, times(1)).exists(any());
    verify(operationDao, never()).insert(any());
  }

  @Test(expected = IllegalArgumentException.class)
  public void withdrawLowBalanceExceptionTest() {
    String account = "123456788";
    BigDecimal amount = BigDecimal.valueOf(50);

    given(accountService.exists(account)).willReturn(true);
    given(operationDao.accountBalance(account)).willReturn(BigDecimal.valueOf(10));

    transactionService.withdraw(account, amount);
    verify(accountService, times(1)).exists(any());
    verify(operationDao, times(1)).accountBalance(any());
    verify(operationDao, never()).insert(any());
  }

}
