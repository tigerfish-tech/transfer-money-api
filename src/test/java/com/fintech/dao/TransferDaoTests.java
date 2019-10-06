package com.fintech.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import com.fintech.dao.impl.DbTransferDao;
import com.fintech.models.dao.TransferDaoEntity;
import com.fintech.testutils.DbUtils;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TransferDaoTests {

  private TransferDao<TransferDaoEntity, Long> transferDao;

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
    transferDao = new DbTransferDao();

    try (Connection connection = DbConnectionManager.getConnection()) {
      connection.createStatement()
          .executeUpdate("INSERT INTO USERS (id, full_name) VALUES ('123', 'TEST')");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO ACCOUNTS (number, user_id, currency) VALUES ('USD123', '123', 'USD')");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO ACCOUNTS (number, user_id, currency) VALUES ('USD456', '123', 'USD')");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO TRANSFERS (id) VALUES (1)");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO OPERATIONS (id, account, debit) VALUES (1, '123', 100)");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO OPERATIONS (id, account, credit) VALUES (2, '123', 100)");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO TRANSFER_OPERATIONS (transfer_id, operation_id) VALUES (1 ,1)");
      connection.createStatement()
          .executeUpdate(
              "INSERT INTO TRANSFER_OPERATIONS (transfer_id, operation_id) VALUES (1 ,2)");
    }
  }

  @After
  public void afterTest() throws SQLException {
    try (Connection connection = DbConnectionManager.getConnection()) {
      connection.createStatement()
          .executeUpdate("DELETE FROM TRANSFER_OPERATIONS WHERE transfer_id is not null");
      connection.createStatement()
          .executeUpdate("DELETE FROM OPERATIONS WHERE id is not null");
      connection.createStatement()
          .executeUpdate("DELETE FROM TRANSFERS WHERE id is not null");
      connection.createStatement()
          .executeUpdate("DELETE FROM ACCOUNTS WHERE number is not null");
      connection.createStatement()
          .executeUpdate("DELETE FROM USERS WHERE id is not null");
    }
  }

  @Test
  public void getByIdTest() {
    TransferDaoEntity transferDaoEntity = transferDao.getById(1L);

    Assert.assertEquals(Long.valueOf(1L), transferDaoEntity.getId());
    assertThat(transferDaoEntity.getOperations(), hasSize(2));
  }

  @Test
  public void isExistTest() {
    Assert.assertTrue(transferDao.isExist(1L));
    Assert.assertFalse(transferDao.isExist(2L));
  }

  @Test
  public void findAllTest() {
    assertThat(transferDao.findAll(10, 0), hasSize(1));
  }

  @Test
  public void deleteByIdTest() {
    transferDao.deleteById(1L);
  }

  @Test
  public void deleteTest() {
    TransferDaoEntity transferDaoEntity = transferDao.getById(1L);

    transferDao.delete(transferDaoEntity);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void updateTest() {
    transferDao.update(null);
  }

}
