package com.fintech.services;

import com.fintech.models.User;
import java.util.List;

public interface UserService {

  User getById(String id);

  User save(User user);

  void delete(String id);

  List<User> findAll();

}
