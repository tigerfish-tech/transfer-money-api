package com.fintech.routers;

import com.fintech.dao.DbConnectionManager;
import com.fintech.dao.UserDao;
import com.fintech.dao.impl.DbUserDao;
import com.fintech.models.User;
import com.fintech.models.dao.UserDaoEntity;
import com.fintech.services.UserService;
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
import org.apache.http.client.methods.HttpPut;
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
public class UserRouterTests {

  private static DefaultUndertowServer server;

  @BeforeClass
  public static void initClass() {
    DbUtils.initDb();

    UserDao<UserDaoEntity, String> userDao = new DbUserDao();
    UserService userService = new DefaultUserService(userDao);
    UserRouter userRouter = new UserRouter(userService);

    server = DefaultUndertowServer.createServer(userRouter.handler());
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
          .executeUpdate("INSERT INTO USERS (id, full_name) VALUES ('456', 'TEST2')");
      connection.createStatement()
          .executeUpdate("INSERT INTO USERS (id, full_name) VALUES ('789', 'TEST3')");
    }
  }

  @After
  public void after() throws SQLException {
    try (Connection connection = DbConnectionManager.getConnection()) {
      connection.createStatement()
          .executeUpdate("DELETE FROM USERS WHERE id is not null");
    }
  }

  @Test
  public void userInfo_SuccessTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {

      HttpGet get = new HttpGet(server.getUrl() + "/users/123");
      HttpResponse result = httpClient.execute(get);

      Assert.assertEquals(StatusCodes.OK, result.getStatusLine().getStatusCode());
      Assert.assertEquals("{\"id\":\"123\",\"fullName\":\"TEST1\"}",
          HttpUtils.readBodyAsString(result.getEntity()));
    }
  }

  @Test
  public void userInfo_NotFoundTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {

      HttpGet get = new HttpGet(server.getUrl() + "/users/1234");
      HttpResponse result = httpClient.execute(get);

      Assert.assertEquals(StatusCodes.NOT_FOUND, result.getStatusLine().getStatusCode());
    }
  }

  @Test
  public void userList_SuccessTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      String jsonFullList = "[{\"id\":\"123\",\"fullName\":\"TEST1\"},"
          + "{\"id\":\"456\",\"fullName\":\"TEST2\"},{\"id\":\"789\",\"fullName\":\"TEST3\"}]";

      HttpGet get1 = new HttpGet(server.getUrl() + "/users");
      HttpResponse result1 = httpClient.execute(get1);

      Assert.assertEquals(StatusCodes.OK, result1.getStatusLine().getStatusCode());
      Assert.assertEquals(jsonFullList, HttpUtils.readBodyAsString(result1.getEntity()));

      String jsonLimitList = "[{\"id\":\"123\",\"fullName\":\"TEST1\"},"
          + "{\"id\":\"456\",\"fullName\":\"TEST2\"}]";

      HttpGet get2 = new HttpGet(server.getUrl() + "/users?limit=2");
      HttpResponse result2 = httpClient.execute(get2);

      Assert.assertEquals(StatusCodes.OK, result2.getStatusLine().getStatusCode());
      Assert.assertEquals(jsonLimitList, HttpUtils.readBodyAsString(result2.getEntity()));

      String jsonOffsetList
          = "[{\"id\":\"456\",\"fullName\":\"TEST2\"},{\"id\":\"789\",\"fullName\":\"TEST3\"}]";

      HttpGet get3 = new HttpGet(server.getUrl() + "/users?offset=1");
      HttpResponse result3 = httpClient.execute(get3);

      Assert.assertEquals(StatusCodes.OK, result3.getStatusLine().getStatusCode());
      Assert.assertEquals(jsonOffsetList, HttpUtils.readBodyAsString(result3.getEntity()));
    }
  }

  @Test
  public void userCreate_SuccessTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      String jsonString = "{\"fullName\": \"John Smith\"}";

      StringEntity requestEntity = new StringEntity(
          jsonString,
          ContentType.APPLICATION_JSON);

      HttpPost post = new HttpPost(server.getUrl() + "/users");
      post.setEntity(requestEntity);
      HttpResponse result = httpClient.execute(post);

      User user = HttpUtils.readBody(result.getEntity(), User.class);

      Assert.assertEquals(StatusCodes.CREATED, result.getStatusLine().getStatusCode());
      Assert.assertEquals("John Smith", user.getFullName());
    }
  }

  @Test
  public void userCreate_EmptyFullnameTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      String jsonString = "{\"fullName\": \"\"}";

      StringEntity requestEntity = new StringEntity(
          jsonString,
          ContentType.APPLICATION_JSON);

      HttpPost post = new HttpPost(server.getUrl() + "/users");
      post.setEntity(requestEntity);
      HttpResponse result = httpClient.execute(post);

      Assert.assertEquals(StatusCodes.BAD_REQUEST, result.getStatusLine().getStatusCode());
    }
  }

  @Test
  public void userUpdate_SuccessTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      String jsonString = "{\"fullName\": \"John Smith\"}";

      StringEntity requestEntity = new StringEntity(
          jsonString,
          ContentType.APPLICATION_JSON);
      HttpPut post = new HttpPut(server.getUrl() + "/users/123");
      post.setEntity(requestEntity);
      HttpResponse result = httpClient.execute(post);

      User user = HttpUtils.readBody(result.getEntity(), User.class);

      Assert.assertEquals(StatusCodes.OK, result.getStatusLine().getStatusCode());
      Assert.assertEquals("123", user.getId());
      Assert.assertEquals("John Smith", user.getFullName());
    }
  }

  @Test
  public void userUpdate_UserDoesntExistTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      String jsonString = "{\"fullName\": \"John Smith\"}";

      StringEntity requestEntity = new StringEntity(
          jsonString,
          ContentType.APPLICATION_JSON);
      HttpPut post = new HttpPut(server.getUrl() + "/users/1234");
      post.setEntity(requestEntity);
      HttpResponse result = httpClient.execute(post);

      Assert.assertEquals(StatusCodes.BAD_REQUEST, result.getStatusLine().getStatusCode());
    }
  }

  @Test
  public void userDeleteTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      HttpDelete post = new HttpDelete(server.getUrl() + "/users/123");
      HttpResponse result1 = httpClient.execute(post);

      Assert.assertEquals(StatusCodes.OK, result1.getStatusLine().getStatusCode());

      HttpResponse result2 = httpClient.execute(post);

      Assert.assertEquals(StatusCodes.NOT_FOUND, result2.getStatusLine().getStatusCode());
    }
  }

}
