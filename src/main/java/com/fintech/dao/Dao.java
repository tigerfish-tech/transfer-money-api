package com.fintech.dao;

import java.util.List;

public interface Dao<T, I> {

  T getById(I id);

  T insert(T obj);

  T update(T obj);

  List<T> findAll();

  void deleteById(I id);

  void delete(T obj);

  boolean isExist(I id);

}
