package com.fintech.routers;

import com.fintech.models.Account;
import com.fintech.models.ErrorResponse;
import com.fintech.services.AccountService;
import com.google.gson.Gson;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.util.HttpString;
import java.nio.charset.Charset;
import java.util.List;

public class AccountRouter implements RoutingHandlerFactory {

  private AccountService accountService;

  public AccountRouter(AccountService accountService) {
    this.accountService = accountService;
  }

  void accountInfo(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      String number = exc.getQueryParameters().get("number").getFirst();

      final Gson gson = new Gson();
      try {
        Account account = accountService.getByNumber(number);

        exc.setStatusCode(200);
        exc.getResponseHeaders()
            .add(HttpString.tryFromString("Content-Type"), "application/json");
        exc.getResponseSender().send(gson.toJson(account));
      } catch (IllegalArgumentException e) {
        exc.setStatusCode(404);
        exc.getResponseHeaders()
            .add(HttpString.tryFromString("Content-Type"), "application/json");
        exc.getResponseSender().send(gson.toJson(
            ErrorResponse.builder().code(400)
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis()).build()));
      }

    });
  }

  void userAccounts(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      String userId = exc.getQueryParameters().get("userId").getFirst();

      final Gson gson = new Gson();

      try {
        List<Account> accounts = accountService.userAccounts(userId);

        exc.setStatusCode(200);
        exc.getResponseHeaders()
            .add(HttpString.tryFromString("Content-Type"), "application/json");
        exc.getResponseSender().send(gson.toJson(accounts));
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

  void createAccount(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      String userId = exc.getQueryParameters().get("userId").getFirst();

      final Gson gson = new Gson();
      Account account = gson.fromJson(new String(bytes, Charset.defaultCharset()), Account.class);
      try {
        Account result = accountService.addAccountToUser(userId, account.getCurrency());

        exc.setStatusCode(201);
        exc.getResponseHeaders()
            .add(HttpString.tryFromString("Content-Type"), "application/json");
        exc.getResponseSender().send(gson.toJson(result));
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

  void delete(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      String number = exc.getQueryParameters().get("number").getFirst();
      try {
        accountService.delete(number);
        exc.setStatusCode(200);
      } catch (IllegalArgumentException e) {
        exc.setStatusCode(404);
      }
    });
  }

  @Override
  public RoutingHandler handler() {
    return new RoutingHandler()
        .get("/accounts/{number}", this::accountInfo)
        .post("/users/{userId}/accounts", this::createAccount)
        .get("/users/{userId}/accounts", this::userAccounts)
        .delete("/accounts/{number}", this::delete);
  }
}
