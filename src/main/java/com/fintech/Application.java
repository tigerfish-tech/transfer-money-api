package com.fintech;

import com.fintech.routers.Router;
import io.undertow.Undertow;
import org.jboss.logging.Logger;

public class Application {

  private static final Logger log = Logger.getLogger(Application.class);

  public static void main(String[] args) {
    Undertow server = Undertow.builder()
        .addHttpListener(8080, "0.0.0.0")
        .setHandler(Router.getInstance().routingHandler())
        .build();

    server.start();
  }

}
