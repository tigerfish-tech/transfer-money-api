package com.fintech.routers;

import com.fintech.models.ErrorResponse;
import com.fintech.models.TransferOperation;
import com.fintech.models.TransferRepresentation;
import com.fintech.services.TransactionService;
import com.fintech.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

  void transfer(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      String from = exc.getQueryParameters().get("from").getFirst();
      String to = exc.getQueryParameters().get("to").getFirst();
      double amount = Double.parseDouble(exc.getQueryParameters().get("amount").getFirst());
      try {
        TransferOperation operation = TransferOperation.builder()
            .accountFrom(from).accountTo(to).amount(BigDecimal.valueOf(amount)).build();
        transactionService.transfer(operation);

        exc.setStatusCode(200);
      } catch (IllegalArgumentException e) {
        createErrorResponse(exc, e);
      }
    });
  }

  void list(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      Integer limit = 100;
      Integer offset = 0;
      if (exc.getQueryParameters().containsKey("limit")) {
        limit = Integer.valueOf(exc.getQueryParameters().get("limit").getFirst());
      }
      if (exc.getQueryParameters().containsKey("offset")) {
        offset = Integer.valueOf(exc.getQueryParameters().get("offset").getFirst());
      }

      List<TransferRepresentation> transferRepresentations
          = transactionService.findAll(limit, offset);
      exc.setStatusCode(200);
      exc.getResponseHeaders()
          .add(HttpString.tryFromString("Content-Type"), "application/json");
      final Gson gson = new GsonBuilder()
          .setPrettyPrinting()
          .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
      exc.getResponseSender().send(gson.toJson(transferRepresentations));
    });
  }

  void delete(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullBytes((exc, bytes) -> {
      Long transferId = Long.parseLong(exc.getQueryParameters().get("transferId").getFirst());
      try {
        transactionService.delete(transferId);
        exc.setStatusCode(200);
      } catch (IllegalArgumentException e) {
        exc.setStatusCode(404);
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
