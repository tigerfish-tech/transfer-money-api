package com.fintech.routers;

import com.fintech.dao.AccountDao;
import com.fintech.dao.DbConnectionManager;
import com.fintech.dao.UserDao;
import com.fintech.dao.impl.DbAccountDao;
import com.fintech.dao.impl.DbUserDao;
import com.fintech.models.Account;
import com.fintech.models.ErrorResponse;
import com.fintech.models.dao.AccountDaoEntity;
import com.fintech.models.dao.UserDaoEntity;
import com.fintech.services.AccountService;
import com.fintech.services.UserService;
import com.fintech.services.impl.DefaultAccountService;
import com.fintech.services.impl.DefaultUserService;
import com.fintech.testutils.DbUtils;
import com.fintech.testutils.DefaultUndertowServer;
import com.fintech.testutils.HttpUtils;
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
public class AccountRouterTests {

  private static DefaultUndertowServer server;

  @BeforeClass
  public static void initClass() {
    DbUtils.initDb();

    AccountDao<AccountDaoEntity, String> accountDao = new DbAccountDao();
    UserDao<UserDaoEntity, String> userDao = new DbUserDao();

    UserService userService = new DefaultUserService(userDao);
    AccountService accountService = new DefaultAccountService(accountDao, userService);
    AccountRouter accountRouter = new AccountRouter(accountService);

    server = DefaultUndertowServer.createServer(accountRouter.handler());
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
          .executeUpdate("INSERT INTO USERS (id, full_name) VALUES ('123', 'TEST1')");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO ACCOUNTS (number, user_id, currency) VALUES ('USD123', '123', 'USD')");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO ACCOUNTS (number, user_id, currency) VALUES ('USD456', '123', 'USD')");
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
  public void accountInfoTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {

      String jsonString = "{\"number\":\"USD123\",\"currency\":\"USD\"}";

      HttpGet getExisting = new HttpGet(server.getUrl() + "/accounts/USD123");
      HttpResponse result1 = httpClient.execute(getExisting);

      Assert.assertEquals(StatusCodes.OK, result1.getStatusLine().getStatusCode());
      Assert.assertEquals(jsonString, HttpUtils.readBodyAsString(result1.getEntity()));

      HttpGet getNotExisting = new HttpGet(server.getUrl() + "/accounts/USD125");
      HttpResponse result2 = httpClient.execute(getNotExisting);
      ErrorResponse errorResponse = HttpUtils.readBody(result2.getEntity(), ErrorResponse.class);
      Assert.assertEquals(StatusCodes.NOT_FOUND, result2.getStatusLine().getStatusCode());
      Assert.assertEquals(404, errorResponse.getCode());
      Assert.assertEquals("Account USD125 doesn't exists", errorResponse.getMessage());
    }
  }

  @Test
  public void userAccountsTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {

      String jsonString = "[{\"number\":\"USD123\",\"currency\":\"USD\"},"
          + "{\"number\":\"USD456\",\"currency\":\"USD\"}]";

      HttpGet getExisting = new HttpGet(server.getUrl() + "/users/123/accounts");
      HttpResponse result1 = httpClient.execute(getExisting);

      Assert.assertEquals(StatusCodes.OK, result1.getStatusLine().getStatusCode());
      Assert.assertEquals(jsonString, HttpUtils.readBodyAsString(result1.getEntity()));

      HttpGet getNotExisting = new HttpGet(server.getUrl() + "/users/125/accounts");
      HttpResponse result2 = httpClient.execute(getNotExisting);
      ErrorResponse errorResponse = HttpUtils.readBody(result2.getEntity(), ErrorResponse.class);
      Assert.assertEquals(StatusCodes.BAD_REQUEST, result2.getStatusLine().getStatusCode());
      Assert.assertEquals(400, errorResponse.getCode());
      Assert.assertEquals("User 125 doesn't exist", errorResponse.getMessage());
    }
  }

  @Test
  public void createAccountTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      String jsonString = "{\"currency\": \"EUR\"}";

      StringEntity requestEntitySuccess = new StringEntity(
          jsonString,
          ContentType.APPLICATION_JSON);

      HttpPost postSuccess = new HttpPost(server.getUrl() + "/users/123/accounts");
      postSuccess.setEntity(requestEntitySuccess);
      HttpResponse resultSuccess = httpClient.execute(postSuccess);

      Account user = HttpUtils.readBody(resultSuccess.getEntity(), Account.class);

      Assert.assertEquals(StatusCodes.CREATED, resultSuccess.getStatusLine().getStatusCode());
      Assert.assertEquals("EUR", user.getCurrency());
      Assert.assertNotNull(user.getNumber());

      StringEntity requestEntityError = new StringEntity(
          jsonString,
          ContentType.APPLICATION_JSON);

      HttpPost postError = new HttpPost(server.getUrl() + "/users/125/accounts");
      postError.setEntity(requestEntityError);
      HttpResponse resultError = httpClient.execute(postError);

      Assert.assertEquals(StatusCodes.BAD_REQUEST, resultError.getStatusLine().getStatusCode());
    }
  }

  @Test
  public void accountDeleteTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      HttpDelete post = new HttpDelete(server.getUrl() + "/accounts/USD123");
      HttpResponse result1 = httpClient.execute(post);

      Assert.assertEquals(StatusCodes.OK, result1.getStatusLine().getStatusCode());

      HttpResponse result2 = httpClient.execute(post);

      Assert.assertEquals(StatusCodes.NOT_FOUND, result2.getStatusLine().getStatusCode());
    }
  }


}
