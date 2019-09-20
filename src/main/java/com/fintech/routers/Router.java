package com.fintech.routers;

import com.fintech.dao.AccountDao;
import com.fintech.dao.UserDao;
import com.fintech.dao.impl.DbAccountDao;
import com.fintech.dao.impl.DbUserDao;
import com.fintech.models.ErrorResponse;
import com.fintech.models.dao.AccountDaoEntity;
import com.fintech.models.dao.UserDaoEntity;
import com.fintech.services.AccountService;
import com.fintech.services.UserService;
import com.fintech.services.impl.DefaultAccountService;
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

  private final RoutingHandler handler = new RoutingHandler()
      .addAll(userRoutingHandler())
      .addAll(accountRoutingHandler())
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

  private RoutingHandler userRoutingHandler() {
    return new RoutingHandler()
        .get("/users", userRouter::list)
        .get("/users/{userId}", userRouter::userInfo)
        .post("/users", userRouter::createUser)
        .put("/users/{userId}", userRouter::updateUser)
        .delete("/users/{userId}", userRouter::delete);
  }

  private RoutingHandler accountRoutingHandler() {
    return new RoutingHandler()
        .get("/account/{number}", accountRouter::accountInfo)
        .post("/users/{userId}/account", accountRouter::createAccount)
        .get("/users/{userId}/account", accountRouter::userAccounts)
        .delete("/account/{number}", accountRouter::delete);
  }

  public static Router getInstance() {
    return INSTANCE;
  }

  public RoutingHandler routingHandler() {
    return handler;
  }

}
