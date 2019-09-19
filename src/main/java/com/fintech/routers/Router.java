package com.fintech.routers;

import com.fintech.dao.UserDao;
import com.fintech.dao.impl.DbUserDao;
import com.fintech.models.ErrorResponse;
import com.fintech.models.User;
import com.fintech.services.UserService;
import com.fintech.services.impl.DefaultUserService;
import com.google.gson.Gson;
import io.undertow.server.RoutingHandler;
import io.undertow.util.HttpString;

public enum Router {

  INSTANCE;

  private UserDao<User, String> userDao = new DbUserDao();
  private UserService userService = new DefaultUserService(userDao);
  private UserRouter userRouter = new UserRouter(userService);

  private final RoutingHandler handler = new RoutingHandler()
      .get("/users", userRouter::list)
      .get("/users/{userId}", userRouter::userInfo)
      .post("/users", userRouter::createUser)
      .put("/users/{userId}", userRouter::updateUser)
      .delete("/users/{userId}", userRouter::delete)
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

  public RoutingHandler routingHandler() {
    return handler;
  }

}
