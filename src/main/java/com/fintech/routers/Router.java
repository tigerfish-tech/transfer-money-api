package com.fintech.routers;

import com.fintech.dao.AccountDao;
import com.fintech.dao.OperationDao;
import com.fintech.dao.TransferDao;
import com.fintech.dao.UserDao;
import com.fintech.dao.impl.DbAccountDao;
import com.fintech.dao.impl.DbOperationDao;
import com.fintech.dao.impl.DbTransferDao;
import com.fintech.dao.impl.DbUserDao;
import com.fintech.models.ErrorResponse;
import com.fintech.models.dao.AccountDaoEntity;
import com.fintech.models.dao.OperationDaoEntity;
import com.fintech.models.dao.TransferDaoEntity;
import com.fintech.models.dao.UserDaoEntity;
import com.fintech.services.AccountService;
import com.fintech.services.TransactionService;
import com.fintech.services.UserService;
import com.fintech.services.impl.DefaultAccountService;
import com.fintech.services.impl.DefaultTransactionService;
import com.fintech.services.impl.DefaultUserService;
import com.google.gson.Gson;
import io.undertow.server.RoutingHandler;
import io.undertow.util.HttpString;

public enum Router {

  INSTANCE;

  private UserDao<UserDaoEntity, String> userDao = new DbUserDao();
  private UserService userService = new DefaultUserService(userDao);
  private UserRouter userRouter = new UserRouter(userService);

  private AccountDao<AccountDaoEntity, String> accountDao = new DbAccountDao();
  private AccountService accountService = new DefaultAccountService(accountDao, userService);
  private AccountRouter accountRouter = new AccountRouter(accountService);

  private OperationDao<OperationDaoEntity, Long> operationDao = new DbOperationDao();
  private TransferDao<TransferDaoEntity, Long> transferDao = new DbTransferDao();
  private TransactionService transactionService
      = new DefaultTransactionService(operationDao, transferDao, accountService);
  private TransactionRouter transactionRouter = new TransactionRouter(transactionService);

  private final RoutingHandler handler = new RoutingHandler()
      .addAll(userRouter.userRoutingHandler())
      .addAll(accountRouter.handler())
      .addAll(transferRoutingHandler())
      .setFallbackHandler(exchange -> {
        Gson gson = new Gson();
        exchange.setStatusCode(400);
        exchange.getResponseHeaders()
            .add(HttpString.tryFromString("Content-Type"), "application/json");
        exchange.getResponseSender().send(gson.toJson(
            ErrorResponse.builder().code(400)
                .message("Method not found")
                .timestamp(System.currentTimeMillis()).build()));
      })
      .setInvalidMethodHandler(exchange -> {
        Gson gson = new Gson();
        exchange.setStatusCode(400);
        exchange.getResponseHeaders()
            .add(HttpString.tryFromString("Content-Type"), "application/json");
        exchange.getResponseSender().send(gson.toJson(
            ErrorResponse.builder().code(400)
                .message("Wrong method format")
                .timestamp(System.currentTimeMillis()).build()));
      });

  public static Router getInstance() {
    return INSTANCE;
  }

  private RoutingHandler transferRoutingHandler() {
    return new RoutingHandler()
        .post("/accounts/{number}/cash-in", transactionRouter::cashIn)
        .post("/accounts/{number}/withdraw", transactionRouter::withdraw)
        .get("/accounts/{number}/balance", transactionRouter::balance)
        .post("/transfers", transactionRouter::transfer)
        .get("/transfers", transactionRouter::list)
        .delete("/transfers/{transferId}", transactionRouter::delete);
  }

  public RoutingHandler routingHandler() {
    return handler;
  }

}
