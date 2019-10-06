package com.fintech.dao;

import com.fintech.dao.impl.DbOperationDao;
import com.fintech.models.dao.OperationDaoEntity;
import com.fintech.testutils.DbUtils;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class OperationDaoTests {

  private OperationDao<OperationDaoEntity, Long> operationDao;

  @BeforeClass
  public static void initClass() {
    DbUtils.initDb();
  }

  @AfterClass
  public static void afterClass() {
    DbUtils.close();
  }

  @Before
  public void initTest() throws SQLException {
    operationDao = new DbOperationDao();

    try (Connection connection = DbConnectionManager.getConnection()) {
      connection.createStatement()
          .executeUpdate("INSERT INTO USERS (id, full_name) VALUES ('123', 'TEST')");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO ACCOUNTS (number, user_id, currency) VALUES ('USD123', '123', 'USD')");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO ACCOUNTS (number, user_id, currency) VALUES ('USD456', '123', 'USD')");
    }
  }

  @After
  public void afterTest() throws SQLException {
    try (Connection connection = DbConnectionManager.getConnection()) {
      connection.createStatement()
          .executeUpdate("DELETE FROM ACCOUNTS WHERE number is not null");
      connection.createStatement()
          .executeUpdate("DELETE FROM USERS WHERE id is not null");
      connection.createStatement()
          .executeUpdate("DELETE FROM OPERATIONS WHERE id is not null");
    }
  }

  @Test
  public void getByIdTest() throws SQLException {
    Long id = createOperation();

    OperationDaoEntity entity = operationDao.getById(id);

    Assert.assertEquals("USD123", entity.getAccountNumber());
    Assert.assertEquals(BigDecimal.valueOf(50.0), entity.getDebit());
    Assert.assertEquals(id, entity.getId());
  }

  @Test
  public void insertTest() {
    BigDecimal amount = BigDecimal.valueOf(50.0);
    OperationDaoEntity operationDaoEntity = new OperationDaoEntity();
    operationDaoEntity.setDebit(amount);
    operationDaoEntity.setAccountNumber("USD123");

    OperationDaoEntity result = operationDao.insert(operationDaoEntity);

    Assert.assertNull(operationDaoEntity.getId());
    Assert.assertNotNull(result.getId());
  }

  @Test
  public void accountBalanceTest() {
    BigDecimal cashIn = BigDecimal.valueOf(50.0);

    OperationDaoEntity operationDaoEntity = new OperationDaoEntity();
    operationDaoEntity.setDebit(cashIn);
    operationDaoEntity.setAccountNumber("USD123");

    BigDecimal balanceBefore = operationDao.accountBalance("USD123");
    Assert.assertEquals(BigDecimal.valueOf(0.0), balanceBefore);

    operationDao.insert(operationDaoEntity);

    BigDecimal balanceAfter = operationDao.accountBalance("USD123");

    Assert.assertEquals(cashIn, balanceAfter);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void updateTest() {
    operationDao.update(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void findAllTest() {
    operationDao.findAll(null, null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void deleteTest() {
    operationDao.delete(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void deleteByIdTest() {
    operationDao.deleteById(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void isExistTest() {
    operationDao.isExist(null);
  }

  private Long createOperation() throws SQLException {
    Long id = null;
    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement(
          "INSERT INTO OPERATIONS (account, debit, credit) VALUES (?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setString(1, "USD123");
      preparedStatement.setBigDecimal(2, BigDecimal.valueOf(50.0));
      preparedStatement.setBigDecimal(3, null);

      preparedStatement.executeUpdate();

      ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
      if (generatedKeys.next()) {
        id = generatedKeys.getLong(1);
      }
    }

    return id;
  }

}
