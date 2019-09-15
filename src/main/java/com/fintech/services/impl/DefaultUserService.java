package com.fintech.services.impl;

import com.fintech.models.User;
import com.fintech.services.UserService;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DefaultUserService implements UserService {

  private static final Map<String, User> userStorage = new LinkedHashMap<>();

  @Override
  public User getById(String id) {
    if (!userStorage.containsKey(id)) {
      throw new InvalidParameterException(
          String.format("User with id %s doesn't exists", id));
    }

    return userStorage.get(id);
  }

  @Override
  public User save(User user) {
    if (Objects.isNull(user.getFullName()) || user.getFullName().isEmpty()) {
      throw new InvalidParameterException("User's fullName can't be empty");
    }

    if (Objects.nonNull(user.getId())) {
      if (!userStorage.containsKey(user.getId())) {
        throw new InvalidParameterException(
            String.format("User with id %s doesn't exists", user.getId()));
      }
      userStorage.put(user.getId(), user);
    } else {
      user = User.builder()
          .id(Objects.isNull(user.getId()) ? UUID.randomUUID().toString() : user.getId())
          .fullName(user.getFullName())
          .build();

      userStorage.put(user.getId(), user);
    }

    return user;
  }

  @Override
  public void delete(String id) {
    if (!userStorage.containsKey(id)) {
      throw new InvalidParameterException(
          String.format("User with id %s doesn't exists", id));
    }

    userStorage.remove(id);
  }

  @Override
  public List<User> findAll() {
    return new ArrayList<>(userStorage.values());
  }

}
