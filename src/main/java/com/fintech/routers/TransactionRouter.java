package com.fintech.routers;

import com.fintech.models.ErrorResponse;
import com.fintech.services.TransactionService;
import com.google.gson.Gson;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.math.BigDecimal;

public class TransactionRouter {

  private TransactionService transactionService;

  public TransactionRouter(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  void cashIn(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      String number = exc.getQueryParameters().get("number").getFirst();
      double amount = Double.parseDouble(exc.getQueryParameters().get("amount").getFirst());
      try {
        transactionService.cashIn(number, BigDecimal.valueOf(amount));
        exc.setStatusCode(200);
      } catch (IllegalArgumentException e) {
        createErrorResponse(exc, e);
      }
    });
  }

  void withdraw(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      String number = exc.getQueryParameters().get("number").getFirst();
      double amount = Double.parseDouble(exc.getQueryParameters().get("amount").getFirst());
      try {
        transactionService.withdraw(number, BigDecimal.valueOf(amount));
        exc.setStatusCode(200);
      } catch (IllegalArgumentException e) {
        createErrorResponse(exc, e);
      }
    });
  }

  void balance(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      String number = exc.getQueryParameters().get("number").getFirst();
      try {
        exc.setStatusCode(200);
        exc.getResponseSender().send(transactionService.balance(number).toPlainString());
      } catch (IllegalArgumentException e) {
        createErrorResponse(exc, e);
      }
    });
  }

  private void createErrorResponse(HttpServerExchange exc, Exception e) {
    Gson gson = new Gson();

    exc.setStatusCode(400);
    exc.getResponseHeaders()
        .add(HttpString.tryFromString("Content-Type"), "application/json");
    exc.getResponseSender().send(gson.toJson(
        ErrorResponse.builder().code(400)
            .message(e.getMessage())
            .timestamp(System.currentTimeMillis()).build()));
  }

}
