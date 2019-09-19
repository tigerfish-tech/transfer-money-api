package com.fintech.services.impl;

import com.fintech.dao.UserDao;
import com.fintech.models.User;
import com.fintech.services.UserService;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Objects;

public class DefaultUserService implements UserService {

  private UserDao<User, String> userDao;

  public DefaultUserService(UserDao<User, String> userDao) {
    this.userDao = userDao;
  }

  @Override
  public User getById(String id) {
    if (!userDao.isExist(id)) {
      throw new InvalidParameterException(
          String.format("User with id %s doesn't exists", id));
    }

    return userDao.getById(id);
  }

  @Override
  public User save(User user) {
    if (Objects.isNull(user.getFullName()) || user.getFullName().isEmpty()) {
      throw new InvalidParameterException("User's fullName can't be empty");
    }
    User result;
    if (Objects.isNull(user.getId())) {
      result = userDao.insert(user);
    } else {
      result = userDao.update(user);
    }
    return result;
  }

  @Override
  public void delete(String id) {
    if (!userDao.isExist(id)) {
      throw new InvalidParameterException(
          String.format("User with id %s doesn't exists", id));
    }

    userDao.deleteById(id);
  }

  @Override
  public List<User> findAll() {
    return userDao.findAll();
  }

  @Override
  public boolean isUserExists(String id) {
    return userDao.isExist(id);
  }

}
