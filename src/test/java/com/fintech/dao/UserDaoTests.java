package com.fintech.dao;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

import com.fintech.dao.impl.DbUserDao;
import com.fintech.models.dao.UserDaoEntity;
import com.fintech.testutils.DbUtils;
import java.sql.Connection;
import java.sql.SQLException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UserDaoTests {

  private UserDao<UserDaoEntity, String> userDao;

  @BeforeClass
  public static void initClass() {
    DbUtils.initDb();
  }

  @AfterClass
  public static void afterClass() {
    DbUtils.close();
  }

  @Before
  public void setUp() throws SQLException {
    userDao = new DbUserDao();

    try (Connection connection = DbConnectionManager.getConnection()) {
      connection.createStatement()
          .executeUpdate("INSERT INTO USERS (id, full_name) VALUES ('123', 'TEST')");
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
  public void userCreateTest() {
    UserDaoEntity entity = new UserDaoEntity();
    entity.setFullName("TEST USER");
    UserDaoEntity persisted = userDao.insert(entity);

    MatcherAssert.assertThat("Check id",
        persisted.getId(), is(IsNull.notNullValue()));
    MatcherAssert.assertThat("Check date",
        persisted.getCreated(), is(IsNull.notNullValue()));
  }

  @Test
  public void userGetByIdTest() {
    UserDaoEntity persisted = userDao.getById("123");

    MatcherAssert.assertThat("Check id",
        persisted.getId(), is("123"));
    MatcherAssert.assertThat("Check name",
        persisted.getFullName(), is("TEST"));
  }

  @Test
  public void userDeleteByIdSuccessTest() {
    userDao.deleteById("123");
  }

  @Test
  public void userDeleteTest() {
    UserDaoEntity user = userDao.getById("123");

    userDao.delete(user);
  }

  @Test
  public void userIsExistTest() {
    MatcherAssert.assertThat(userDao.isExist("123"), is(true));
  }

  @Test
  public void userUpdateTest() {
    UserDaoEntity entity = new UserDaoEntity();
    entity.setId("123");
    entity.setFullName("TEST USER1");

    UserDaoEntity updated = userDao.update(entity);

    MatcherAssert.assertThat("Check id",
        updated.getId(), is("123"));
    MatcherAssert.assertThat("Check name",
        updated.getFullName(), is("TEST USER1"));
  }

  @Test
  public void userListTest() {
    MatcherAssert.assertThat("Check user list",
        userDao.findAll(10, 0), hasSize(1));
  }

}
