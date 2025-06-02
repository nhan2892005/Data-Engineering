package com.migratedata

import org.apache.spark.sql.{DataFrame, SparkSession}

class BigQueryConnection(spark: SparkSession) {
  /**
   * Đọc table BigQuery từ project bigquery-public-data, dataset github_repos.
   * Ví dụ: tableName = "sample_commits" → đọc bigquery-public-data.github_repos.sample_commits
   */
  def readTable(tableName: String): DataFrame = {
    spark.read
      .format("bigquery")
      .option("table", s"focal-future-456710-g8.intro.$tableName")
      .load()
  }
}

object BigQueryConnection {
  /**
   * Khởi SparkSession đã cấu hình để đọc BigQuery.
   * Nếu không thực sự cần read view, bạn có thể bỏ "viewsEnabled" và "materializationDataset".
   */
  def createSparkSession(): SparkSession = {
    val bqTempDataset = AppConfig.get("BQ_MATERIALIZATION_DATASET")
    SparkSession.builder()
      .appName("BigQueryToHadoopToPostgres")
      .config("spark.master", "local[*]")
      .config("spark.sql.shuffle.partitions", "10")
      .config("viewsEnabled", "true")
      .config("materializationDataset", bqTempDataset)
      .config("spark.sql.legacy.parquet.nanosAsLong", "true")
      .config("spark.sql.parquet.int96RebaseModeInRead", "CORRECTED")
      .config("spark.sql.parquet.int96RebaseModeInWrite", "CORRECTED")
      .config("spark.sql.parquet.datetimeRebaseModeInRead", "CORRECTED")
      .config("spark.sql.parquet.datetimeRebaseModeInWrite", "CORRECTED")
      .config("spark.sql.parquet.enableVectorizedReader", "false")
      .config("spark.sql.session.timeZone", "UTC")
      .getOrCreate()
  }
}
