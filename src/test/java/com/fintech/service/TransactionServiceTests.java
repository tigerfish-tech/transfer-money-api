package com.fintech.service;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fintech.dao.OperationDao;
import com.fintech.dao.TransferDao;
import com.fintech.models.Account;
import com.fintech.models.TransferOperation;
import com.fintech.models.dao.OperationDaoEntity;
import com.fintech.models.dao.TransferDaoEntity;
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
  private TransferDao<TransferDaoEntity, Long> transferDao;

  @Before
  public void setUp() {
    accountService = mock(AccountService.class);
    operationDao = mock(OperationDao.class);
    transferDao = mock(TransferDao.class);

    transactionService = new DefaultTransactionService(operationDao, transferDao, accountService);
  }

  //Cash in money
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

  //Withdraw money
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

  //Transfer money between accounts
  @Test
  public void transferMoneySuccessTest() {
    String from = "12345";
    String to = "23456";
    BigDecimal amount = BigDecimal.valueOf(50);

    TransferDaoEntity entity = new TransferDaoEntity();

    Account accFrom = Account.builder().number(from).currency("USD").build();
    Account accTo = Account.builder().number(to).currency("USD").build();

    given(accountService.exists(any())).willReturn(true);
    given(accountService.getByNumber(any())).willReturn(accFrom).willReturn(accTo);
    given(operationDao.accountBalance(from)).willReturn(BigDecimal.valueOf(100));

    OperationDaoEntity operationFrom = createOperation(1L, from, null, amount);
    OperationDaoEntity operationTo = createOperation(2L, to, amount, null);
    given(operationDao.insert(any())).willReturn(operationFrom).willReturn(operationTo);

    given(transferDao.insert(any())).willReturn(entity);

    TransferOperation operation = TransferOperation.builder()
        .accountFrom(from).accountTo(to).amount(amount).build();

    transactionService.transfer(operation);

    verify(accountService, times(2)).exists(any());
    verify(operationDao, times(1)).accountBalance(any());
    verify(accountService, times(2)).getByNumber(any());
    verify(operationDao, times(2)).insert(any());

    ArgumentCaptor<TransferDaoEntity> argumentCaptor
        = ArgumentCaptor.forClass(TransferDaoEntity.class);
    verify(transferDao, times(1)).insert(argumentCaptor.capture());

    TransferDaoEntity arg = argumentCaptor.getValue();
    MatcherAssert.assertThat("Check account", arg.getOperations(), hasSize(2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void transferNoAccountExceptionTest() {
    String from = "12345";
    String to = "23456";
    BigDecimal amount = BigDecimal.valueOf(50);

    given(accountService.exists(any())).willReturn(false);

    TransferOperation operation = TransferOperation.builder()
        .accountFrom(from).accountTo(to).amount(amount).build();

    transactionService.transfer(operation);

    verify(accountService, times(2)).exists(any());
    verify(operationDao, never()).accountBalance(any());
    verify(accountService, never()).getByNumber(any());
    verify(operationDao, never()).insert(any());
    verify(transferDao, never()).insert(any());
  }

  @Test(expected = IllegalArgumentException.class)
  public void transferNoMoneyExceptionTest() {
    String from = "12345";
    String to = "23456";
    BigDecimal amount = BigDecimal.valueOf(50);

    given(accountService.exists(any())).willReturn(true);
    given(operationDao.accountBalance(from)).willReturn(BigDecimal.valueOf(10));

    TransferOperation operation = TransferOperation.builder()
        .accountFrom(from).accountTo(to).amount(amount).build();

    transactionService.transfer(operation);

    verify(accountService, times(2)).exists(any());
    verify(operationDao, times(1)).accountBalance(any());
    verify(accountService, never()).getByNumber(any());
    verify(operationDao, never()).insert(any());
    verify(transferDao, never()).insert(any());
  }

  @Test(expected = IllegalArgumentException.class)
  public void transferMismatchCurrenciesExceptionTest() {
    String from = "12345";
    String to = "23456";
    BigDecimal amount = BigDecimal.valueOf(50);

    given(accountService.exists(any())).willReturn(true);
    given(operationDao.accountBalance(from)).willReturn(BigDecimal.valueOf(100));
    Account accFrom = Account.builder().number(from).currency("USD").build();
    Account accTo = Account.builder().number(to).currency("EUR").build();
    given(accountService.getByNumber(any())).willReturn(accFrom).willReturn(accTo);

    TransferOperation operation = TransferOperation.builder()
        .accountFrom(from).accountTo(to).amount(amount).build();

    transactionService.transfer(operation);

    verify(accountService, times(1)).exists(any());
    verify(accountService, times(2)).getByNumber(any());
    verify(operationDao, times(1)).accountBalance(any());
    verify(operationDao, never()).insert(any());
    verify(transferDao, never()).insert(any());
  }

  //Delete transfer by id
  @Test
  public void deleteTransferByIdSuccessTest() {
    Long id = 1L;
    given(transferDao.isExist(id)).willReturn(true);

    transactionService.delete(id);

    ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
    verify(transferDao, times(1)).isExist(any());
    verify(transferDao, times(1)).deleteById(argumentCaptor.capture());
    MatcherAssert.assertThat("Check param", argumentCaptor.getValue(), is(id));
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteTransferByIdExceptionTest() {
    Long id = 1L;
    given(transferDao.isExist(id)).willReturn(false);

    transactionService.delete(id);

    ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
    verify(transferDao, times(1)).isExist(argumentCaptor.capture());
    verify(transferDao, never()).deleteById(any());
    MatcherAssert.assertThat("Check param", argumentCaptor.getValue(), is(id));
  }

  private OperationDaoEntity createOperation(Long id, String account,
                                             BigDecimal debit, BigDecimal credit) {
    OperationDaoEntity operationDaoEntity = new OperationDaoEntity();
    operationDaoEntity.setId(id);
    operationDaoEntity.setAccountNumber(account);
    operationDaoEntity.setDebit(debit);
    operationDaoEntity.setCredit(credit);

    return operationDaoEntity;
  }

}
