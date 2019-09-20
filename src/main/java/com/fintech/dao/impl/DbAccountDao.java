package com.fintech.dao.impl;

import com.fintech.dao.AccountDao;
import com.fintech.dao.DbConnectionManager;
import com.fintech.models.dao.AccountDaoEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DbAccountDao implements AccountDao<AccountDaoEntity, String> {

  @Override
  public List<AccountDaoEntity> userAccounts(String userId) {
    List<AccountDaoEntity> accounts = new ArrayList<>();
    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement = connection.prepareStatement(
          "SELECT * FROM ACCOUNTS WHERE USER_ID LIKE ?");
      preparedStatement.setString(1, userId);

      ResultSet resultSet = preparedStatement.executeQuery();

      while (resultSet.next()) {
        accounts.add(mapRow(resultSet));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return accounts;
  }

  @Override
  public AccountDaoEntity getById(String s) {
    AccountDaoEntity user = null;

    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement("SELECT * FROM ACCOUNTS WHERE number like ?");
      preparedStatement.setString(1, s);

      ResultSet resultSet = preparedStatement.executeQuery();
      if (resultSet.next()) {
        user = mapRow(resultSet);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return user;
  }

  @Override
  public AccountDaoEntity update(AccountDaoEntity obj) {
    AccountDaoEntity account = null;

    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement(
          "UPDATE ACCOUNTS SET user_id = ?, currency = ? WHERE number like ?");
      preparedStatement.setString(1, obj.getUserId());
      preparedStatement.setString(2, obj.getCurrency());
      preparedStatement.setString(3, obj.getNumber());

      preparedStatement.executeUpdate();

      account = getById(obj.getNumber());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return account;
  }

  @Override
  public AccountDaoEntity insert(AccountDaoEntity obj) {
    AccountDaoEntity account = null;

    try (Connection connection = DbConnectionManager.getConnection()) {
      String number = createNumber();

      PreparedStatement preparedStatement
          = connection.prepareStatement(
          "INSERT INTO ACCOUNTS (number, user_id, currency) VALUES (?, ?, ?)");
      preparedStatement.setString(1, number);
      preparedStatement.setString(2, obj.getUserId());
      preparedStatement.setString(3, obj.getCurrency());

      preparedStatement.executeUpdate();

      account = getById(number);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return account;
  }

  @Override
  public List<AccountDaoEntity> findAll() {
    List<AccountDaoEntity> accounts = new ArrayList<>();
    try (Connection connection = DbConnectionManager.getConnection()) {
      ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM ACCOUNTS");

      while (resultSet.next()) {
        accounts.add(mapRow(resultSet));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return accounts;
  }

  @Override
  public void deleteById(String s) {
    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement("DELETE FROM ACCOUNTS WHERE NUMBER LIKE ?");

      preparedStatement.setString(1, s);
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void delete(AccountDaoEntity obj) {
    deleteById(obj.getNumber());
  }

  @Override
  public boolean isExist(String s) {
    boolean exists = false;

    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement("SELECT * FROM ACCOUNTS WHERE number like ?");
      preparedStatement.setString(1, s);

      exists = preparedStatement.executeQuery().next();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return exists;
  }

  private AccountDaoEntity mapRow(ResultSet resultSet) throws SQLException {
    AccountDaoEntity entity = new AccountDaoEntity();
    entity.setCurrency(resultSet.getString("currency"));
    entity.setUserId(resultSet.getString("user_id"));
    entity.setNumber(resultSet.getString("number"));
    entity.setCreated(resultSet.getTimestamp("created").toLocalDateTime());

    return entity;
  }

  private String createNumber() {
    Random random = new Random(899999999999L);

    String id;
    do {
      long number = Math.abs(random.nextLong()) + 100000000000L;

      id = "KZ" + number;
    } while (isExist(id));

    return id;
  }
}
