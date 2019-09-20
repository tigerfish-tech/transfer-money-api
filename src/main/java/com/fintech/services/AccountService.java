package com.fintech.services;

import com.fintech.models.Account;
import java.util.List;

public interface AccountService {

  boolean exists(String number);

  Account getByNumber(String number);

  Account addAccountToUser(String userId, String currency);

  void delete(String number);

  List<Account> userAccounts(String userId);

}
