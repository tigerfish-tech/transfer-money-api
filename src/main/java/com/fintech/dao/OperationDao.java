package com.fintech.dao;

import java.math.BigDecimal;

public interface OperationDao<T, I> extends Dao<T, I> {

  BigDecimal accountBalance(String number);

}
