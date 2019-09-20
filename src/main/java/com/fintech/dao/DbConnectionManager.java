package com.fintech.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import org.flywaydb.core.Flyway;

public class DbConnectionManager {

  private static HikariConfig config;
  private static HikariDataSource ds;

  private DbConnectionManager() {
  }

  public static void setConfig(HikariConfig config) {
    DbConnectionManager.config = config;
  }

  public static void create() {
    if (Objects.isNull(config)) {
      config = new HikariConfig();
      config.setJdbcUrl("jdbc:h2:mem:test");
      config.setUsername("sa");
      config.setPassword("sa");
    }
    ds = new HikariDataSource(config);

    Flyway flyway = Flyway.configure()
        .dataSource(config.getJdbcUrl(), config.getUsername(), config.getPassword()).load();
    flyway.migrate();
  }

  public static Connection getConnection() throws SQLException {
    return ds.getConnection();
  }
}
