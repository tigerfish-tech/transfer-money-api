package com.fintech.routers;

import com.fintech.models.ErrorResponse;
import com.fintech.models.User;
import com.fintech.services.UserService;
import com.google.gson.Gson;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.nio.charset.Charset;
import java.util.List;

public class UserRouter {

  private UserService userService;

  UserRouter(UserService userService) {
    this.userService = userService;
  }

  void userInfo(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      String userId = exc.getQueryParameters().get("userId").getFirst();

      final Gson gson = new Gson();
      try {
        User user = userService.getById(userId);

        exc.setStatusCode(200);
        exc.getResponseHeaders()
            .add(HttpString.tryFromString("Content-Type"), "application/json");
        exc.getResponseSender().send(gson.toJson(user));
      } catch (IllegalArgumentException e) {
        exc.setStatusCode(400);
        exc.getResponseHeaders()
            .add(HttpString.tryFromString("Content-Type"), "application/json");
        exc.getResponseSender().send(gson.toJson(
            ErrorResponse.builder().code(400)
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis()).build()));
      }

    });
  }

  void createUser(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      final Gson gson = new Gson();

      User user = gson.fromJson(new String(bytes, Charset.defaultCharset()), User.class);
      try {
        User persistedUser = userService.save(user);
        exc.setStatusCode(201);
        exc.getResponseHeaders()
            .add(HttpString.tryFromString("Content-Type"), "application/json");
        exc.getResponseSender().send(gson.toJson(persistedUser));
      } catch (IllegalArgumentException e) {
        exc.setStatusCode(400);
        exc.getResponseHeaders()
            .add(HttpString.tryFromString("Content-Type"), "application/json");
        exc.getResponseSender().send(gson.toJson(
            ErrorResponse.builder().code(400)
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis()).build()));
      }
    });
  }

  void updateUser(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      String userId = exc.getQueryParameters().get("userId").getFirst();

      final Gson gson = new Gson();
      User body = gson.fromJson(new String(bytes, Charset.defaultCharset()), User.class);

      if (userService.exists(userId)) {
        User update = User.builder().id(userId).fullName(body.getFullName()).build();

        User persistedUser = userService.save(update);
        exc.setStatusCode(200);
        exc.getResponseHeaders()
            .add(HttpString.tryFromString("Content-Type"), "application/json");
        exc.getResponseSender().send(gson.toJson(persistedUser));
      } else {
        exc.setStatusCode(400);
        exc.getResponseHeaders()
            .add(HttpString.tryFromString("Content-Type"), "application/json");
        exc.getResponseSender().send(gson.toJson(
            ErrorResponse.builder().code(400)
                .message("User doesn't exist")
                .timestamp(System.currentTimeMillis()).build()));
      }
    });
  }

  void delete(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      String userId = exc.getQueryParameters().get("userId").getFirst();
      try {
        userService.delete(userId);
        exc.setStatusCode(200);
      } catch (IllegalArgumentException e) {
        exc.setStatusCode(404);
      }
    });
  }

  void list(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      final Gson gson = new Gson();
      Integer limit = 100;
      Integer offset = 0;
      if (exc.getQueryParameters().containsKey("limit")) {
        limit = Integer.valueOf(exc.getQueryParameters().get("limit").getFirst());
      }
      if (exc.getQueryParameters().containsKey("offset")) {
        offset = Integer.valueOf(exc.getQueryParameters().get("offset").getFirst());
      }

      List<User> users = userService.findAll(limit, offset);
      exc.setStatusCode(200);
      exc.getResponseHeaders()
          .add(HttpString.tryFromString("Content-Type"), "application/json");
      exc.getResponseSender().send(gson.toJson(users));
    });
  }

}
