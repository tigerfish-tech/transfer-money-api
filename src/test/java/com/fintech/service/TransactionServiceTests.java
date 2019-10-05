package com.fintech.service;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fintech.dao.OperationDao;
import com.fintech.dao.TransferDao;
import com.fintech.models.Account;
import com.fintech.models.TransferOperation;
import com.fintech.models.TransferRepresentation;
import com.fintech.models.dao.OperationDaoEntity;
import com.fintech.models.dao.TransferDaoEntity;
import com.fintech.services.AccountService;
import com.fintech.services.impl.DefaultTransactionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTests {

  @Mock
  private AccountService accountService;
  @Mock
  private OperationDao<OperationDaoEntity, Long> operationDao;
  @Mock
  private TransferDao<TransferDaoEntity, Long> transferDao;
  @InjectMocks
  @Spy
  private DefaultTransactionService transactionService
      = new DefaultTransactionService(operationDao, transferDao, accountService);

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
    verify(accountService).exists(any());

    ArgumentCaptor<OperationDaoEntity> argumentCaptor
        = ArgumentCaptor.forClass(OperationDaoEntity.class);
    verify(operationDao).insert(argumentCaptor.capture());

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
    verify(accountService).exists(any());
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
    doReturn(true).when(transactionService).isMoneyEnough(eq(account), eq(amount));

    transactionService.withdraw(account, amount);
    verify(accountService).exists(any());

    ArgumentCaptor<OperationDaoEntity> argumentCaptor
        = ArgumentCaptor.forClass(OperationDaoEntity.class);
    verify(operationDao).insert(argumentCaptor.capture());
    verify(transactionService).isMoneyEnough(any(), any());

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
    verify(accountService).exists(any());
    verify(transactionService, never()).isMoneyEnough(any(), any());
    verify(operationDao, never()).insert(any());
  }

  @Test(expected = IllegalArgumentException.class)
  public void withdrawLowBalanceExceptionTest() {
    String account = "123456788";
    BigDecimal amount = BigDecimal.valueOf(50);

    given(accountService.exists(account)).willReturn(true);
    doReturn(false).when(transactionService).isMoneyEnough(eq(account), eq(amount));

    transactionService.withdraw(account, amount);
    verify(accountService).exists(any());
    verify(transactionService).isMoneyEnough(any(), any());
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
    doReturn(true).when(transactionService).isMoneyEnough(eq(from), eq(amount));

    OperationDaoEntity operationFrom = createOperation(1L, from, null, amount);
    OperationDaoEntity operationTo = createOperation(2L, to, amount, null);
    given(operationDao.insert(any())).willReturn(operationFrom).willReturn(operationTo);

    given(transferDao.insert(any())).willReturn(entity);

    TransferOperation operation = TransferOperation.builder()
        .accountFrom(from).accountTo(to).amount(amount).build();

    transactionService.transfer(operation);

    verify(accountService, times(2)).exists(any());
    verify(transactionService).isMoneyEnough(any(), any());
    verify(accountService, times(2)).getByNumber(any());
    verify(operationDao, times(2)).insert(any());

    ArgumentCaptor<TransferDaoEntity> argumentCaptor
        = ArgumentCaptor.forClass(TransferDaoEntity.class);
    verify(transferDao).insert(argumentCaptor.capture());

    TransferDaoEntity arg = argumentCaptor.getValue();
    MatcherAssert.assertThat("Check account", arg.getOperations(), hasSize(2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void transferNoAccountFromExceptionTest() {
    String from = "12345";
    String to = "23456";
    BigDecimal amount = BigDecimal.valueOf(50);

    given(accountService.exists(eq(from))).willReturn(false);

    TransferOperation operation = TransferOperation.builder()
        .accountFrom(from).accountTo(to).amount(amount).build();

    transactionService.transfer(operation);

    verify(accountService).exists(any());
    verify(transactionService, never()).isMoneyEnough(any(), any());
    verify(accountService, never()).getByNumber(any());
    verify(operationDao, never()).insert(any());
    verify(transferDao, never()).insert(any());
  }

  @Test(expected = IllegalArgumentException.class)
  public void transferNoAccountToExceptionTest() {
    String from = "12345";
    String to = "23456";
    BigDecimal amount = BigDecimal.valueOf(50);

    given(accountService.exists(eq(from))).willReturn(true);
    given(accountService.exists(eq(to))).willReturn(false);

    TransferOperation operation = TransferOperation.builder()
        .accountFrom(from).accountTo(to).amount(amount).build();

    transactionService.transfer(operation);

    verify(accountService, times(2)).exists(any());
    verify(transactionService, never()).isMoneyEnough(any(), any());
    verify(accountService, never()).getByNumber(any());
    verify(operationDao, never()).insert(any());
    verify(transferDao, never()).insert(any());
  }

  @Test(expected = IllegalArgumentException.class)
  public void transferWrongAmountExceptionTest() {
    String from = "12345";
    String to = "23456";
    BigDecimal amount = BigDecimal.valueOf(-50);

    given(accountService.exists(any())).willReturn(true);

    TransferOperation operation = TransferOperation.builder()
        .accountFrom(from).accountTo(to).amount(amount).build();

    transactionService.transfer(operation);

    verify(accountService, times(2)).exists(any());
    verify(transactionService, never()).isMoneyEnough(any(), any());
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
    doReturn(false).when(transactionService).isMoneyEnough(eq(from), eq(amount));
    Account accFrom = Account.builder().number(from).currency("USD").build();
    Account accTo = Account.builder().number(to).currency("USD").build();
    given(accountService.getByNumber(any())).willReturn(accFrom).willReturn(accTo);

    TransferOperation operation = TransferOperation.builder()
        .accountFrom(from).accountTo(to).amount(amount).build();

    transactionService.transfer(operation);

    verify(accountService, times(2)).exists(any());
    verify(transactionService).isMoneyEnough(any(), any());
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
    Account accFrom = Account.builder().number(from).currency("USD").build();
    Account accTo = Account.builder().number(to).currency("EUR").build();
    given(accountService.getByNumber(any())).willReturn(accFrom).willReturn(accTo);

    TransferOperation operation = TransferOperation.builder()
        .accountFrom(from).accountTo(to).amount(amount).build();

    transactionService.transfer(operation);

    verify(accountService).exists(any());
    verify(accountService, times(2)).getByNumber(any());
    verify(transactionService, never()).isMoneyEnough(any(), any());
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
    verify(transferDao).isExist(any());
    verify(transferDao).deleteById(argumentCaptor.capture());
    MatcherAssert.assertThat("Check param", argumentCaptor.getValue(), is(id));
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteTransferByIdExceptionTest() {
    Long id = 1L;
    given(transferDao.isExist(id)).willReturn(false);

    transactionService.delete(id);

    ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
    verify(transferDao).isExist(argumentCaptor.capture());
    verify(transferDao, never()).deleteById(any());
    MatcherAssert.assertThat("Check param", argumentCaptor.getValue(), is(id));
  }

  @Test
  public void balanceSuccessTest() {
    String account = "12345";
    BigDecimal amount = BigDecimal.valueOf(100);

    given(operationDao.accountBalance(eq(account))).willReturn(amount);
    given(accountService.exists(eq(account))).willReturn(true);

    Assert.assertEquals(amount, transactionService.balance(account));

    ArgumentCaptor<String> accountCaptor = ArgumentCaptor.forClass(String.class);
    verify(operationDao).accountBalance(accountCaptor.capture());
    verify(accountService).exists(any());

    Assert.assertEquals(account, accountCaptor.getValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void balanceExceptionTest() {
    String account = "12345";

    given(accountService.exists(eq(account))).willReturn(false);

    transactionService.balance(account);

    verify(operationDao, never()).accountBalance(any());
    verify(accountService).exists(any());
  }

  @Test
  public void isMoneyEnoughTrueTest() {
    String account = "12345";
    BigDecimal amount = new BigDecimal(50);

    doReturn(BigDecimal.valueOf(100)).when(operationDao).accountBalance(account);

    Assert.assertTrue(transactionService.isMoneyEnough(account, amount));
    verify(operationDao).accountBalance(any());
  }

  @Test
  public void isMoneyEnoughFalseTest() {
    String account = "12345";
    BigDecimal amount = new BigDecimal(50);

    doReturn(BigDecimal.valueOf(40)).when(operationDao).accountBalance(account);

    Assert.assertFalse(transactionService.isMoneyEnough(account, amount));
    verify(operationDao).accountBalance(any());
  }

  @Test
  public void valueOf_TransferRepresentationTest() {
    List<Long> operationIds = Collections.unmodifiableList(Arrays.asList(1L, 2L));

    LocalDateTime dateTime = LocalDateTime.now();

    TransferDaoEntity transferDaoEntity = new TransferDaoEntity();
    transferDaoEntity.setId(100L);
    transferDaoEntity.setCreated(dateTime);
    transferDaoEntity.setOperations(operationIds);

    BigDecimal amount = BigDecimal.valueOf(100);

    String accountFrom = "12345";
    OperationDaoEntity firstOperation = new OperationDaoEntity();
    firstOperation.setId(1L);
    firstOperation.setCredit(amount);
    firstOperation.setAccountNumber(accountFrom);
    firstOperation.setCreated(dateTime);
    doReturn(firstOperation).when(operationDao).getById(eq(1L));

    String accountTo = "67890";
    OperationDaoEntity secondOperation = new OperationDaoEntity();
    secondOperation.setId(2L);
    secondOperation.setDebit(amount);
    secondOperation.setAccountNumber(accountTo);
    secondOperation.setCreated(dateTime);
    doReturn(secondOperation).when(operationDao).getById(eq(2L));

    TransferRepresentation transferRepresentation = transactionService.valueOf(transferDaoEntity);

    Assert.assertEquals(dateTime, transferRepresentation.getCreated());
    Assert.assertEquals(accountFrom, transferRepresentation.getAccountFrom());
    Assert.assertEquals(accountTo, transferRepresentation.getAccountTo());
    Assert.assertEquals(amount, transferRepresentation.getAmount());
  }

  @Test
  public void findAllSuccessTest() {
    List<TransferDaoEntity> transfers = transferList();

    int limit = 10;
    int offset = 0;

    doReturn(transfers).when(transferDao).findAll(eq(limit), eq(offset));
    doReturn(TransferRepresentation.builder().build()).when(transactionService).valueOf(any());

    MatcherAssert.assertThat("Check user list",
        transactionService.findAll(limit, offset), Matchers.hasSize(10));
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

  private List<TransferDaoEntity> transferList() {
    return LongStream.rangeClosed(0, 9).boxed().map(number -> {
      TransferDaoEntity entity = new TransferDaoEntity();
      entity.setId(number);
      List<Long> operations = new ArrayList<>();
      operations.add(number * 10 + 1);
      operations.add(number * 10 + 2);
      entity.setOperations(operations);

      return entity;
    }).collect(Collectors.toList());
  }

}
