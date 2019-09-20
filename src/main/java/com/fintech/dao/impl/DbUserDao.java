package com.fintech.dao.impl;

import com.fintech.dao.DbConnectionManager;
import com.fintech.dao.UserDao;
import com.fintech.models.dao.UserDaoEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DbUserDao implements UserDao<UserDaoEntity, String> {

  @Override
  public UserDaoEntity getById(String s) {
    UserDaoEntity user = null;

    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement("SELECT * FROM USERS WHERE id like ?");
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
  public UserDaoEntity update(UserDaoEntity obj) {
    UserDaoEntity user = null;

    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement(
          "UPDATE USERS SET full_name = ? WHERE id like ?");
      preparedStatement.setString(1, obj.getFullName());
      preparedStatement.setString(2, obj.getId());

      preparedStatement.executeUpdate();

      user = getById(obj.getId());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return user;
  }

  @Override
  public UserDaoEntity insert(UserDaoEntity obj) {
    UserDaoEntity user = null;

    try (Connection connection = DbConnectionManager.getConnection()) {
      String id = createId();

      PreparedStatement preparedStatement
          = connection.prepareStatement("INSERT INTO USERS (id, full_name) VALUES (?, ?)");
      preparedStatement.setString(1, id);
      preparedStatement.setString(2, obj.getFullName());

      preparedStatement.executeUpdate();

      user = getById(id);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return user;
  }

  @Override
  public List<UserDaoEntity> findAll() {
    List<UserDaoEntity> users = new ArrayList<>();
    try (Connection connection = DbConnectionManager.getConnection()) {
      ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM USERS");

      while (resultSet.next()) {
        users.add(mapRow(resultSet));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return users;
  }

  @Override
  public void deleteById(String s) {
    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement("DELETE FROM USERS WHERE ID LIKE ?");

      preparedStatement.setString(1, s);
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void delete(UserDaoEntity obj) {
    deleteById(obj.getId());
  }

  @Override
  public boolean isExist(String s) {
    boolean exists = false;

    try (Connection connection = DbConnectionManager.getConnection()) {
      PreparedStatement preparedStatement
          = connection.prepareStatement("SELECT * FROM USERS WHERE id like ?");
      preparedStatement.setString(1, s);

      exists = preparedStatement.executeQuery().next();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return exists;
  }

  private UserDaoEntity mapRow(ResultSet resultSet) throws SQLException {
    UserDaoEntity entity = new UserDaoEntity();
    entity.setId(resultSet.getString("id"));
    entity.setFullName(resultSet.getString("full_name"));
    entity.setCreated(resultSet.getTimestamp("created").toLocalDateTime());

    return entity;
  }

  private String createId() {
    String id;
    do {
      id = UUID.randomUUID().toString();
    } while (isExist(id));

    return id;
  }

}
