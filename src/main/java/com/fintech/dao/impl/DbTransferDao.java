package com.fintech.dao.impl;

import com.fintech.dao.DbConnectionManager;
import com.fintech.dao.TransferDao;
import com.fintech.models.dao.TransferDaoEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DbTransferDao implements TransferDao<TransferDaoEntity, Long> {

  @Override
  public TransferDaoEntity getById(Long id) {
    TransferDaoEntity entity = null;
    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement(
          "SELECT TR.id as id, TR.created as created, TR_OP.operation_id as op_id "
              + "FROM TRANSFERS TR JOIN TRANSFER_OPERATIONS TR_OP ON TR.id = TR_OP.transfer_id "
              + "WHERE TR.id = ?");
      preparedStatement.setLong(1, id);

      ResultSet resultSet = preparedStatement.executeQuery();

      while (resultSet.next()) {
        if (Objects.isNull(entity)) {
          entity = mapRow(resultSet);
        }
        entity.getOperations().add(resultSet.getLong("op_id"));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return entity;
  }

  @Override
  public TransferDaoEntity insert(TransferDaoEntity obj) {
    TransferDaoEntity operation = null;

    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement transferPreparedStatement
          = connection.prepareStatement(
          "INSERT INTO TRANSFERS DEFAULT VALUES",
          Statement.RETURN_GENERATED_KEYS);

      transferPreparedStatement.executeUpdate();

      ResultSet generatedKeys = transferPreparedStatement.getGeneratedKeys();
      if (generatedKeys.next()) {
        long id = generatedKeys.getLong(1);

        for (Long operationId : obj.getOperations()) {
          PreparedStatement preparedStatement
              = connection.prepareStatement(
              "INSERT INTO TRANSFER_OPERATIONS (transfer_id, operation_id) VALUES (? ,?)");
          preparedStatement.setLong(1, id);
          preparedStatement.setLong(2, operationId);

          preparedStatement.executeUpdate();
        }

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
  public TransferDaoEntity update(TransferDaoEntity obj) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<TransferDaoEntity> findAll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteById(Long id) {
    try (Connection connection = DbConnectionManager.getConnection()) {
      TransferDaoEntity entity = getById(id);

      for (Long operationId : entity.getOperations()) {
        PreparedStatement operationDeleteStatement
            = connection.prepareStatement(
            "DELETE FROM OPERATIONS WHERE id = ?");
        operationDeleteStatement.setLong(1, operationId);

        PreparedStatement transferOpDeleteStatement
            = connection.prepareStatement(
            "DELETE FROM TRANSFER_OPERATIONS WHERE transfer_id = ? AND operation_id = ?");
        transferOpDeleteStatement.setLong(1, id);
        transferOpDeleteStatement.setLong(2, operationId);

        transferOpDeleteStatement.executeUpdate();
      }

      PreparedStatement transferDeleteStatement
          = connection.prepareStatement(
          "DELETE FROM TRANSFERS WHERE id = ?");
      transferDeleteStatement.setLong(1, id);
      transferDeleteStatement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void delete(TransferDaoEntity obj) {
    deleteById(obj.getId());
  }

  @Override
  public boolean isExist(Long id) {
    throw new UnsupportedOperationException();
  }

  private TransferDaoEntity mapRow(ResultSet resultSet) throws SQLException {
    TransferDaoEntity entity = new TransferDaoEntity();
    entity.setId(resultSet.getLong("id"));
    entity.setCreated(resultSet.getTimestamp("created").toLocalDateTime());
    List<Long> operations = new ArrayList<>();
    entity.setOperations(operations);

    return entity;
  }

}
