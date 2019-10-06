package com.fintech.testutils;

import com.fintech.dao.DbConnectionManager;
import com.zaxxer.hikari.HikariConfig;

public class DbUtils {

  public static void initDb() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:mem:test");
    config.setUsername("sa");
    config.setPassword("sa");

    DbConnectionManager.setConfig(config);
    DbConnectionManager.create();
  }

  public static void close() {
    DbConnectionManager.close();
  }


}
