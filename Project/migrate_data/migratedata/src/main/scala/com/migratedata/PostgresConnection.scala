package com.migratedata

import java.sql.{Connection, DriverManager, SQLException}

object PostgresConnection {

  /**
   * Trả về một Connection JDBC đến PostgreSQL dựa vào .env:
   *   POSTGRES_URL, POSTGRES_USER, POSTGRES_PASSWORD
   */
  def getConnection(): Connection = {
    val url      = AppConfig.get("POSTGRES_URL")      // e.g. jdbc:postgresql://localhost:5432/github_repo
    val user     = AppConfig.get("POSTGRES_USER")     // e.g. pnhan_init
    val password = AppConfig.get("POSTGRES_PASSWORD") // e.g. pnhan_pass

    // Load driver
    Class.forName("org.postgresql.Driver")
    val conn = DriverManager.getConnection(url, user, password)
    conn.setAutoCommit(false)
    conn
  }
}
