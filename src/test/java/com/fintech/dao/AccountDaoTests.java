package com.fintech.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import com.fintech.dao.impl.DbAccountDao;
import com.fintech.models.dao.AccountDaoEntity;
import com.zaxxer.hikari.HikariConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AccountDaoTests {

  private AccountDao<AccountDaoEntity, String> accountDao;

  @BeforeClass
  public static void init() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:mem:test");
    config.setUsername("sa");
    config.setPassword("sa");

    DbConnectionManager.setConfig(config);
    DbConnectionManager.create();
  }

  @Before
  public void initTest() throws SQLException {
    accountDao = new DbAccountDao();

    try (Connection connection = DbConnectionManager.getConnection()) {
      connection.createStatement()
          .executeUpdate("INSERT INTO USERS (id, full_name) VALUES ('123', 'TEST')");
    }
  }

  @After
  public void afterTest() throws SQLException {
    try (Connection connection = DbConnectionManager.getConnection()) {
      connection.createStatement()
          .executeUpdate("DELETE FROM ACCOUNTS WHERE number is not null");
      connection.createStatement()
          .executeUpdate("DELETE FROM USERS WHERE id is not null");
    }
  }

  @Test
  public void userAccountsTest() throws SQLException {
    createUserAccount();

    List<AccountDaoEntity> accounts = accountDao.userAccounts("123");

    MatcherAssert.assertThat("Check account list", accounts, hasSize(2));
  }

  @Test
  public void getByIdTest() throws SQLException {
    createUserAccount();

    AccountDaoEntity entity = accountDao.getById("USD123");
    MatcherAssert.assertThat("Check number", entity.getNumber(), is("USD123"));
    MatcherAssert.assertThat("Check user id", entity.getUserId(), is("123"));
    MatcherAssert.assertThat("Check currency", entity.getCurrency(), is("USD"));
  }

  @Test
  public void updateTest() throws SQLException {
    createUserAccount();

    String newCurrency = "EUR";

    AccountDaoEntity entity = accountDao.getById("USD123");

    String oldCurrency = entity.getCurrency();

    entity.setCurrency(newCurrency);

    accountDao.update(entity);

    AccountDaoEntity updated = accountDao.getById("USD123");
    MatcherAssert.assertThat("Check currency", oldCurrency, is("USD"));
    MatcherAssert.assertThat("Check number", updated.getNumber(), is("USD123"));
    MatcherAssert.assertThat("Check user id", updated.getUserId(), is("123"));
    MatcherAssert.assertThat("Check currency", updated.getCurrency(), is(newCurrency));
  }

  @Test
  public void insertTest() {
    AccountDaoEntity newAccount = new AccountDaoEntity();
    newAccount.setCurrency("EUR");
    newAccount.setUserId("123");

    AccountDaoEntity accountDaoEntity = accountDao.insert(newAccount);

    MatcherAssert.assertThat("Check number", accountDaoEntity.getNumber(), notNullValue());
    MatcherAssert.assertThat("Check user id", accountDaoEntity.getUserId(), is("123"));
    MatcherAssert.assertThat("Check currency", accountDaoEntity.getCurrency(), is("EUR"));
  }

  private void createUserAccount() throws SQLException {
    try (Connection connection = DbConnectionManager.getConnection()) {
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO ACCOUNTS (number, user_id, currency) VALUES ('USD123', '123', 'USD')");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO ACCOUNTS (number, user_id, currency) VALUES ('USD456', '123', 'USD')");
    }
  }

}
