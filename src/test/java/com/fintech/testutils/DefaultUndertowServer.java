package com.fintech.testutils;

import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;

public class DefaultUndertowServer {

  private static final Integer MIN_PORT = 8000;
  private static final Integer MAX_PORT = 65000;

  private Integer port;
  private Undertow undertow;

  private DefaultUndertowServer(RoutingHandler handler) {
    port = findFreePort();

    undertow = Undertow.builder()
        .addHttpListener(port, "127.0.0.1")
        .setHandler(handler).build();

    undertow.start();
  }

  public static DefaultUndertowServer createServer(RoutingHandler handler) {
    return new DefaultUndertowServer(handler);
  }

  public String getUrl() {
    return "http://127.0.0.1:" + port;
  }

  private int findFreePort() {
    int freePort = 8000;
    for (int port = MIN_PORT; port <= MAX_PORT; port++) {
      if (isPortAvailable(port)) {
        return port;
      }
    }
    return freePort;
  }

  private boolean isPortAvailable(Integer portNumber) {
    boolean portFree;
    try (ServerSocket socket = new ServerSocket(portNumber)) {
      portFree = true;
    } catch (IOException e) {
      portFree = false;
    }
    return portFree;
  }

  public void stop() {
    if (Objects.nonNull(undertow)) {
      this.undertow.stop();
    }
  }

}
