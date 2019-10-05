package com.fintech.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
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
import org.junit.Assert;
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
      createUserAccount(connection);
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
  public void userAccountsTest() {
    List<AccountDaoEntity> accounts = accountDao.userAccounts("123");

    MatcherAssert.assertThat("Check account list", accounts, hasSize(2));
  }

  @Test
  public void getByIdTest() {
    AccountDaoEntity entity = accountDao.getById("USD123");
    AccountDaoEntity nullEntity = accountDao.getById("123121321");
    MatcherAssert.assertThat("Check number", entity.getNumber(), is("USD123"));
    MatcherAssert.assertThat("Check user id", entity.getUserId(), is("123"));
    MatcherAssert.assertThat("Check currency", entity.getCurrency(), is("USD"));
    MatcherAssert.assertThat("Check null value", nullEntity, nullValue());
  }

  @Test
  public void updateTest() {
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

  @Test
  public void findAllTest() {
    List<AccountDaoEntity> accounts1 = accountDao.findAll(1, 0);
    List<AccountDaoEntity> accounts2 = accountDao.findAll(10, 0);

    MatcherAssert.assertThat("Check account list", accounts1, hasSize(1));
    MatcherAssert.assertThat("Check account list", accounts2, hasSize(2));
  }

  @Test
  public void deleteByIdTest() {
    AccountDaoEntity entity = accountDao.getById("USD123");
    accountDao.deleteById("USD123");
    AccountDaoEntity nullEntity = accountDao.getById("USD123");

    MatcherAssert.assertThat("Check account before", entity, notNullValue());
    MatcherAssert.assertThat("Check account after", nullEntity, nullValue());
  }

  @Test
  public void deleteTest() {
    AccountDaoEntity entity = accountDao.getById("USD123");
    accountDao.delete(entity);
    AccountDaoEntity nullEntity = accountDao.getById("USD123");

    MatcherAssert.assertThat("Check account before", entity, notNullValue());
    MatcherAssert.assertThat("Check account after", nullEntity, nullValue());
  }

  @Test
  public void isExistTest() {
    Assert.assertTrue(accountDao.isExist("USD123"));
    Assert.assertFalse(accountDao.isExist("fdsfdsfdsfds"));
  }

  private void createUserAccount(Connection connection) throws SQLException {
    connection.createStatement()
        .executeUpdate(
            "INSERT INTO ACCOUNTS (number, user_id, currency) VALUES ('USD123', '123', 'USD')");
    connection.createStatement()
        .executeUpdate(
            "INSERT INTO ACCOUNTS (number, user_id, currency) VALUES ('USD456', '123', 'USD')");
  }

}
