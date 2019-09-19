package com.fintech.dao;

import java.util.List;

public interface Dao<T, ID> {

  T getById(ID id);

  T insert(T obj);

  T update(T obj);

  List<T> findAll();

  void deleteById(ID id);

  void delete(T obj);

  boolean isExist(ID id);

}
