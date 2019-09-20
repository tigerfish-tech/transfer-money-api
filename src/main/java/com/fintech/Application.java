package com.fintech;

import com.fintech.dao.DbConnectionManager;
import com.fintech.routers.Router;
import com.zaxxer.hikari.HikariConfig;
import io.undertow.Undertow;
import org.jboss.logging.Logger;

public class Application {

  private static final Logger log = Logger.getLogger(Application.class);

  public static void main(String[] args) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:mem:transfers");
    config.setUsername("sa");
    config.setPassword("sa");

    DbConnectionManager.setConfig(config);
    DbConnectionManager.create();

    Undertow server = Undertow.builder()
        .addHttpListener(8080, "0.0.0.0")
        .setHandler(Router.getInstance().routingHandler())
        .build();

    server.start();
  }

}
