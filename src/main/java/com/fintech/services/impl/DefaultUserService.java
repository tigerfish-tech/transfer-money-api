package com.fintech.services.impl;

import com.fintech.dao.UserDao;
import com.fintech.models.User;
import com.fintech.models.dao.UserDaoEntity;
import com.fintech.services.UserService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultUserService implements UserService {

  private UserDao<UserDaoEntity, String> userDao;

  public DefaultUserService(UserDao<UserDaoEntity, String> userDao) {
    this.userDao = userDao;
  }

  @Override
  public User getById(String id) {
    if (!userDao.isExist(id)) {
      throw new IllegalArgumentException(
          String.format("User with id %s doesn't exists", id));
    }

    return User.valueOf(userDao.getById(id));
  }

  @Override
  public User save(User user) {
    if (Objects.isNull(user.getFullName()) || user.getFullName().isEmpty()) {
      throw new IllegalArgumentException("User's fullName can't be empty");
    }
    UserDaoEntity result;
    if (Objects.isNull(user.getId())) {
      UserDaoEntity entity = new UserDaoEntity();
      entity.setFullName(user.getFullName());

      result = userDao.insert(entity);
    } else {
      UserDaoEntity entity = userDao.getById(user.getId());

      if (Objects.nonNull(entity)) {
        entity.setFullName(user.getFullName());
        result = userDao.update(entity);
      } else {
        throw new IllegalArgumentException("User doesn't exist, id " + user.getId());
      }
    }
    return User.valueOf(result);
  }

  @Override
  public void delete(String id) {
    if (!userDao.isExist(id)) {
      throw new IllegalArgumentException(
          String.format("User with id %s doesn't exists", id));
    }

    userDao.deleteById(id);
  }

  @Override
  public List<User> findAll() {
    return userDao.findAll().stream().map(User::valueOf).collect(Collectors.toList());
  }

  @Override
  public boolean exists(String id) {
    return userDao.isExist(id);
  }

}
