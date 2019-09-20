package com.fintech.dao.impl;

import com.fintech.dao.DbConnectionManager;
import com.fintech.dao.OperationDao;
import com.fintech.models.dao.OperationDaoEntity;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DbOperationDao implements OperationDao<OperationDaoEntity, Long> {

  @Override
  public OperationDaoEntity getById(Long id) {
    OperationDaoEntity operation = null;

    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement("SELECT * FROM OPERATIONS WHERE id = ?");
      preparedStatement.setLong(1, id);

      ResultSet resultSet = preparedStatement.executeQuery();
      if (resultSet.next()) {
        operation = mapRow(resultSet);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return operation;
  }

  @Override
  public OperationDaoEntity insert(OperationDaoEntity obj) {
    OperationDaoEntity operation = null;

    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement(
          "INSERT INTO OPERATIONS (account, debit, credit) VALUES (?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setString(1, obj.getAccountNumber());
      preparedStatement.setBigDecimal(2, obj.getDebit());
      preparedStatement.setBigDecimal(3, obj.getCredit());

      preparedStatement.executeUpdate();

      ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
      if (generatedKeys.next()) {
        long id = generatedKeys.getLong(1);
        operation = getById(id);
      } else {
        throw new IllegalArgumentException("Persistence error");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return operation;
  }

  @Override
  public OperationDaoEntity update(OperationDaoEntity obj) {
    OperationDaoEntity operation = null;

    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement(
          "UPDATE OPERATIONS set account = ?, debit = ?, credit = ? WHERE id = ?");
      preparedStatement.setString(1, obj.getAccountNumber());
      preparedStatement.setBigDecimal(2, obj.getDebit());
      preparedStatement.setBigDecimal(3, obj.getCredit());
      preparedStatement.setLong(4, obj.getId());

      preparedStatement.executeUpdate();

      operation = getById(obj.getId());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return operation;
  }

  @Override
  public List<OperationDaoEntity> findAll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteById(Long id) {
    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement("DELETE FROM OPERATIONS WHERE id = ?");

      preparedStatement.setLong(1, id);
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void delete(OperationDaoEntity obj) {
    deleteById(obj.getId());
  }

  @Override
  public boolean isExist(Long id) {
    boolean exists = false;

    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement("SELECT * FROM OPERATIONS WHERE id = ?");
      preparedStatement.setLong(1, id);

      exists = preparedStatement.executeQuery().next();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return exists;
  }

  @Override
  public BigDecimal accountBalance(String number) {
    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement(
          "SELECT IFNULL(SUM(debit), 0) - IFNULL(SUM(credit), 0) "
              + "as balance FROM OPERATIONS WHERE account like ?");
      preparedStatement.setString(1, number);
      ResultSet resultSet = preparedStatement.executeQuery();

      if (resultSet.next()) {
        return resultSet.getBigDecimal("balance");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return null;
  }

  private OperationDaoEntity mapRow(ResultSet resultSet) throws SQLException {
    OperationDaoEntity entity = new OperationDaoEntity();
    entity.setId(resultSet.getLong("id"));
    entity.setAccountNumber(resultSet.getString("account"));
    entity.setDebit(resultSet.getBigDecimal("debit"));
    entity.setCredit(resultSet.getBigDecimal("credit"));

    return entity;
  }

}
