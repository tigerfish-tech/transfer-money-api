package com.fintech.routers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;

import com.fintech.dao.AccountDao;
import com.fintech.dao.DbConnectionManager;
import com.fintech.dao.OperationDao;
import com.fintech.dao.TransferDao;
import com.fintech.dao.UserDao;
import com.fintech.dao.impl.DbAccountDao;
import com.fintech.dao.impl.DbOperationDao;
import com.fintech.dao.impl.DbTransferDao;
import com.fintech.dao.impl.DbUserDao;
import com.fintech.models.dao.AccountDaoEntity;
import com.fintech.models.dao.OperationDaoEntity;
import com.fintech.models.dao.TransferDaoEntity;
import com.fintech.models.dao.UserDaoEntity;
import com.fintech.services.AccountService;
import com.fintech.services.TransactionService;
import com.fintech.services.UserService;
import com.fintech.services.impl.DefaultAccountService;
import com.fintech.services.impl.DefaultTransactionService;
import com.fintech.services.impl.DefaultUserService;
import com.fintech.testutils.DbUtils;
import com.fintech.testutils.DefaultUndertowServer;
import com.fintech.testutils.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import io.undertow.util.StatusCodes;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TransactionRouterTests {

  private static DefaultUndertowServer server;

  @BeforeClass
  public static void initClass() {
    DbUtils.initDb();

    AccountDao<AccountDaoEntity, String> accountDao = new DbAccountDao();
    UserDao<UserDaoEntity, String> userDao = new DbUserDao();
    OperationDao<OperationDaoEntity, Long> operationDao = new DbOperationDao();
    TransferDao<TransferDaoEntity, Long> transferDao = new DbTransferDao();
    UserService userService = new DefaultUserService(userDao);
    AccountService accountService = new DefaultAccountService(accountDao, userService);

    TransactionService transactionService
        = new DefaultTransactionService(operationDao, transferDao, accountService);
    TransactionRouter transactionRouter = new TransactionRouter(transactionService);

    server = DefaultUndertowServer.createServer(transactionRouter.handler());
  }

  @AfterClass
  public static void afterClass() {
    server.stop();
    DbUtils.close();
  }

  @Before
  public void setUp() throws SQLException {
    try (Connection connection = DbConnectionManager.getConnection()) {
      connection.createStatement()
          .executeUpdate("INSERT INTO USERS (id, full_name) "
              + "VALUES ('123', 'TEST1')");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO ACCOUNTS (number, user_id, currency) "
                  + "VALUES ('USD123', '123', 'USD')");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO ACCOUNTS (number, user_id, currency) "
                  + "VALUES ('USD456', '123', 'USD')");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO ACCOUNTS (number, user_id, currency) "
                  + "VALUES ('EUR456', '123', 'EUR')");
    }
  }

  @After
  public void after() throws SQLException {
    try (Connection connection = DbConnectionManager.getConnection()) {
      connection.createStatement()
          .executeUpdate("DELETE FROM ACCOUNTS WHERE number is not null");
      connection.createStatement()
          .executeUpdate("DELETE FROM USERS WHERE id is not null");
    }
  }

  @Test
  public void transferOperationsSuccessTests() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      //Account balance
      HttpGet startingBalance = new HttpGet(server.getUrl() + "/accounts/USD123/balance");
      HttpResponse startingBalanceResult = httpClient.execute(startingBalance);

      Assert.assertEquals(StatusCodes.OK, startingBalanceResult.getStatusLine().getStatusCode());
      Assert.assertEquals("0.0",
          HttpUtils.readBodyAsString(startingBalanceResult.getEntity()));

      //Account cash in
      String cashInJsonString = "{\"amount\": 100.0}";

      StringEntity cashInEntitySuccess = new StringEntity(
          cashInJsonString,
          ContentType.APPLICATION_JSON);

      HttpPost cashIn = new HttpPost(server.getUrl() + "/accounts/USD123/cash-in");
      cashIn.setEntity(cashInEntitySuccess);
      HttpResponse cashInResult = httpClient.execute(cashIn);
      Assert.assertEquals(StatusCodes.OK, cashInResult.getStatusLine().getStatusCode());

      HttpGet balanceAfterCashIn = new HttpGet(server.getUrl() + "/accounts/USD123/balance");
      HttpResponse balanceAfterCashInResult = httpClient.execute(balanceAfterCashIn);

      Assert.assertEquals(StatusCodes.OK,
          balanceAfterCashInResult.getStatusLine().getStatusCode());
      Assert.assertEquals("100.0",
          HttpUtils.readBodyAsString(balanceAfterCashInResult.getEntity()));

      //Account withdraw
      String withdrawJsonString = "{\"amount\": 10.0}";

      StringEntity withdrawEntitySuccess = new StringEntity(
          withdrawJsonString,
          ContentType.APPLICATION_JSON);

      HttpPost withdraw = new HttpPost(server.getUrl() + "/accounts/USD123/withdraw");
      withdraw.setEntity(withdrawEntitySuccess);

      HttpResponse withdrawResult = httpClient.execute(withdraw);
      Assert.assertEquals(StatusCodes.OK, withdrawResult.getStatusLine().getStatusCode());

      HttpGet balanceAfterWithdraw = new HttpGet(server.getUrl() + "/accounts/USD123/balance");
      HttpResponse balanceAfterWithdrawResult = httpClient.execute(balanceAfterWithdraw);

      Assert.assertEquals(StatusCodes.OK,
          balanceAfterWithdrawResult.getStatusLine().getStatusCode());
      Assert.assertEquals("90.0",
          HttpUtils.readBodyAsString(balanceAfterWithdrawResult.getEntity()));

      //Transfer money
      String jsonString = "{\"from\": \"USD123\", \"to\": \"USD456\", \"amount\": 50.0}";

      StringEntity requestEntitySuccess = new StringEntity(
          jsonString,
          ContentType.APPLICATION_JSON);

      HttpPost transfer = new HttpPost(server.getUrl() + "/transfers");
      transfer.setEntity(requestEntitySuccess);

      HttpResponse transferResult = httpClient.execute(transfer);
      Assert.assertEquals(StatusCodes.OK, transferResult.getStatusLine().getStatusCode());

      HttpGet balanceFromAfterTransfer
          = new HttpGet(server.getUrl() + "/accounts/USD123/balance");
      HttpResponse balanceFromAfterTransferResult
          = httpClient.execute(balanceFromAfterTransfer);

      Assert.assertEquals(StatusCodes.OK,
          balanceFromAfterTransferResult.getStatusLine().getStatusCode());
      Assert.assertEquals("40.0",
          HttpUtils.readBodyAsString(balanceFromAfterTransferResult.getEntity()));

      HttpGet balanceToAfterTransfer
          = new HttpGet(server.getUrl() + "/accounts/USD456/balance");
      HttpResponse balanceToAfterTransferResult
          = httpClient.execute(balanceToAfterTransfer);

      Assert.assertEquals(StatusCodes.OK,
          balanceToAfterTransferResult.getStatusLine().getStatusCode());
      Assert.assertEquals("50.0",
          HttpUtils.readBodyAsString(balanceToAfterTransferResult.getEntity()));

      //List transfers
      HttpGet transfers = new HttpGet(server.getUrl() + "/transfers?limit=10&offset=0");
      HttpResponse transfersResult = httpClient.execute(transfers);

      Assert.assertEquals(StatusCodes.OK, transfersResult.getStatusLine().getStatusCode());

      String transfersJson = HttpUtils.readBodyAsString(transfersResult.getEntity());

      Object[] objects = new Gson().fromJson(transfersJson, Object[].class);

      assertThat(objects, arrayWithSize(1));

      LinkedTreeMap<String, Object> transferMap = (LinkedTreeMap<String, Object>) objects[0];

      Assert.assertEquals(50d, transferMap.get("amount"));

      //Delete transfer
      Long id = ((Double) transferMap.get("id")).longValue();

      HttpDelete deleteTransfer = new HttpDelete(server.getUrl() + "/transfers/" + id);
      HttpResponse deleteTransferResult = httpClient.execute(deleteTransfer);

      Assert.assertEquals(StatusCodes.OK, deleteTransferResult.getStatusLine().getStatusCode());

      HttpResponse deleteTransferAgainResult = httpClient.execute(deleteTransfer);

      Assert.assertEquals(StatusCodes.NOT_FOUND,
          deleteTransferAgainResult.getStatusLine().getStatusCode());

      HttpGet balanceFromAfterDeleteTransfer
          = new HttpGet(server.getUrl() + "/accounts/USD123/balance");
      HttpResponse balanceFromAfterDeleteTransferResult
          = httpClient.execute(balanceFromAfterDeleteTransfer);

      Assert.assertEquals(StatusCodes.OK,
          balanceFromAfterDeleteTransferResult.getStatusLine().getStatusCode());
      Assert.assertEquals("90.0",
          HttpUtils.readBodyAsString(balanceFromAfterDeleteTransferResult.getEntity()));

      HttpGet balanceToAfterDeleteTransfer
          = new HttpGet(server.getUrl() + "/accounts/USD456/balance");
      HttpResponse balanceToAfterDeleteTransferResult
          = httpClient.execute(balanceToAfterDeleteTransfer);

      Assert.assertEquals(StatusCodes.OK,
          balanceToAfterDeleteTransferResult.getStatusLine().getStatusCode());
      Assert.assertEquals("0.0",
          HttpUtils.readBodyAsString(balanceToAfterDeleteTransferResult.getEntity()));
    }
  }

  @Test
  public void balanceErrorTests() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      //Wrong account balance
      HttpGet wrongAccountBalance = new HttpGet(server.getUrl() + "/accounts/USD126/balance");
      HttpResponse wrongAccountBalanceResult = httpClient.execute(wrongAccountBalance);

      Assert.assertEquals(StatusCodes.BAD_REQUEST,
          wrongAccountBalanceResult.getStatusLine().getStatusCode());
    }
  }

  @Test
  public void withdrawErrorTest() throws IOException {
    //Wrong account withdraw
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      String withdrawJsonString = "{\"amount\": 10.0}";

      StringEntity withdrawEntitySuccess = new StringEntity(
          withdrawJsonString,
          ContentType.APPLICATION_JSON);

      HttpPost wrongAccountWithdraw
          = new HttpPost(server.getUrl() + "/accounts/USD126/withdraw");
      wrongAccountWithdraw.setEntity(withdrawEntitySuccess);
      HttpResponse wrongAccountWithdrawResult = httpClient.execute(wrongAccountWithdraw);
      Assert.assertEquals(StatusCodes.BAD_REQUEST,
          wrongAccountWithdrawResult.getStatusLine().getStatusCode());
    }
  }

  @Test
  public void cashInErrorTest() throws IOException {
    //Wrong account cash in
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      String cashInJsonString = "{\"amount\": 100.0}";

      StringEntity cashInEntitySuccess = new StringEntity(
          cashInJsonString,
          ContentType.APPLICATION_JSON);

      HttpPost wrongAccountCashIn
          = new HttpPost(server.getUrl() + "/accounts/USD126/cash-in");
      wrongAccountCashIn.setEntity(cashInEntitySuccess);
      HttpResponse wrongAccountCashInResult = httpClient.execute(wrongAccountCashIn);
      Assert.assertEquals(StatusCodes.BAD_REQUEST,
          wrongAccountCashInResult.getStatusLine().getStatusCode());
    }
  }

  @Test
  public void transferWrongAccountFromTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      String jsonString = "{\"from\": \"USD126\", \"to\": \"USD456\", \"amount\": 50.0}";

      StringEntity requestEntitySuccess = new StringEntity(
          jsonString,
          ContentType.APPLICATION_JSON);

      HttpPost transfer = new HttpPost(server.getUrl() + "/transfers");
      transfer.setEntity(requestEntitySuccess);

      HttpResponse transferResult = httpClient.execute(transfer);
      Assert.assertEquals(StatusCodes.BAD_REQUEST, transferResult.getStatusLine().getStatusCode());
    }
  }

  @Test
  public void transferWrongAccountToTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      String jsonString = "{\"from\": \"USD126\", \"to\": \"USD458\", \"amount\": 50.0}";

      StringEntity requestEntitySuccess = new StringEntity(
          jsonString,
          ContentType.APPLICATION_JSON);

      HttpPost transfer = new HttpPost(server.getUrl() + "/transfers");
      transfer.setEntity(requestEntitySuccess);

      HttpResponse transferResult = httpClient.execute(transfer);
      Assert.assertEquals(StatusCodes.BAD_REQUEST, transferResult.getStatusLine().getStatusCode());
    }
  }

  @Test
  public void transferWrongAmountTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      String jsonString = "{\"from\": \"USD126\", \"to\": \"USD458\", \"amount\": 120.0}";

      StringEntity requestEntitySuccess = new StringEntity(
          jsonString,
          ContentType.APPLICATION_JSON);

      HttpPost transfer = new HttpPost(server.getUrl() + "/transfers");
      transfer.setEntity(requestEntitySuccess);

      HttpResponse transferResult = httpClient.execute(transfer);
      Assert.assertEquals(StatusCodes.BAD_REQUEST, transferResult.getStatusLine().getStatusCode());
    }
  }

  @Test
  public void transferWrongNegativeAmountTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      String jsonString = "{\"from\": \"USD126\", \"to\": \"USD458\", \"amount\": -20.0}";

      StringEntity requestEntitySuccess = new StringEntity(
          jsonString,
          ContentType.APPLICATION_JSON);

      HttpPost transfer = new HttpPost(server.getUrl() + "/transfers");
      transfer.setEntity(requestEntitySuccess);

      HttpResponse transferResult = httpClient.execute(transfer);
      Assert.assertEquals(StatusCodes.BAD_REQUEST, transferResult.getStatusLine().getStatusCode());
    }
  }

}
