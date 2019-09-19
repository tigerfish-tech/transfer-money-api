package com.fintech.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;

public class DbConnectionManager {

  private static HikariConfig config = new HikariConfig();
  private static HikariDataSource ds;

  static {
    config.setJdbcUrl("jdbc:h2:mem:test");
    config.setUsername("sa");
    config.setPassword("sa");
    ds = new HikariDataSource(config);

    Flyway flyway = Flyway.configure()
        .dataSource("jdbc:h2:mem:test", "sa", "sa").load();
    flyway.migrate();
  }

  private DbConnectionManager() {
  }

  public static Connection getConnection() throws SQLException {
    return ds.getConnection();
  }
}
