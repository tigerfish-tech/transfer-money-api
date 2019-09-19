package com.fintech.dao;

import java.util.List;

public interface AccountDao<T, I> extends Dao<T, I> {

  List<T> userAccounts(String userId);

}
