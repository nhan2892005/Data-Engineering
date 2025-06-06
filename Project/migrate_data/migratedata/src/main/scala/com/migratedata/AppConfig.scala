package com.migratedata

import io.github.cdimascio.dotenv.Dotenv

/**
 * Giúp load biến môi trường từ file .env (nằm ở project root).
 * Ví dụ trong .env có:
 *   POSTGRES_URL=jdbc:postgresql://localhost:5432/github_repo
 *   POSTGRES_USER=pnhan_init
 *   POSTGRES_PASSWORD=pnhan_pass
 *   HDFS_OUTPUT_DIR=/user/hduser/bigquery_raw
 *   BQ_MATERIALIZATION_DATASET=my_temp_dataset
 */
object AppConfig {
  private val dotenv = Dotenv.configure().ignoreIfMissing().load()

  def get(key: String): String = {
    val v = dotenv.get(key)
    if (v == null) throw new RuntimeException(s"Missing environment variable $key")
    v
  }
}
