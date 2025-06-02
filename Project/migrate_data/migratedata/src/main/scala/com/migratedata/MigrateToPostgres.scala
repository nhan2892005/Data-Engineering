package com.migratedata

import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.functions._

object MigrateToPostgres {

  def flattenCommitDF(df: DataFrame)(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._

    df
      // Flatten author (struct)
      .withColumn("author_name", col("author.name"))
      .withColumn("author_email", col("author.email"))
      .withColumn("author_time_sec", col("author.time_sec"))
      .withColumn("author_tz_offset", col("author.tz_offset"))
      .withColumn("author_date", col("author.date"))

      // Flatten committer (struct)
      .withColumn("committer_name", col("committer.name"))
      .withColumn("committer_email", col("committer.email"))
      .withColumn("committer_time_sec", col("committer.time_sec"))
      .withColumn("committer_tz_offset", col("committer.tz_offset"))
      .withColumn("committer_date", col("committer.date"))

      // Flatten parent (array<string>) → parent_0, parent_1,...
      .withColumn("parent", expr("transform(parent, x -> x)"))
      .withColumn("parent_str", concat_ws(",", col("parent")))
      .withColumn("parent_0", element_at(col("parent"), 1))
      .withColumn("parent_1", element_at(col("parent"), 2))

      // Flatten trailer (array<struct>) → json string
      .withColumn("trailer_json", to_json(col("trailer")))

      // Flatten difference (array<struct>) → json string
      .withColumn("difference_json", to_json(col("difference")))

      // Drop nested fields
      .drop("author", "committer", "parent", "trailer", "difference")
  }

  def main(args: Array[String]): Unit = {
    // 1. Khởi SparkSession (cùng config như DataIngestion)
    val spark: SparkSession = BigQueryConnection.createSparkSession()

    // 2. Lấy thông tin kết nối PostgreSQL (nếu cần để test), tuy nhiên Spark sẽ tự quản Connection
    //    val conn = PostgresConnection.getConnection()

    // 3. Đọc Parquet từ HDFS
    val tableName    = "sample_commits"
    val hdfsInputDir = AppConfig.get("HDFS_OUTPUT_DIR") // e.g. "/user/hduser/bigquery_raw"
    val inputPath    = s"hdfs://localhost:9000/$hdfsInputDir/$tableName.parquet"

    println(s"Đang đọc Parquet từ HDFS: $inputPath")
    val df: DataFrame = spark.read.parquet(inputPath)
    val flattenedDF = flattenCommitDF(df)(spark)

    println("Schema DataFrame:")
    flattenedDF.printSchema()
    println("Show 5 dòng đầu:")
    flattenedDF.show(5)

    // 4. Ghi DataFrame vào PostgreSQL qua Spark JDBC
    println(s"Ghi DataFrame vào PostgreSQL, table: $tableName")
    flattenedDF.write
      .format("jdbc")
      .option("url", AppConfig.get("POSTGRES_URL"))       // ví dụ: "jdbc:postgresql://localhost:5432/github_repo"
      .option("dbtable", tableName)                       // Spark sẽ tự tạo table nếu chưa tồn tại
      .option("user", AppConfig.get("POSTGRES_USER"))     // e.g. "pnhan_init"
      .option("password", AppConfig.get("POSTGRES_PASSWORD")) // e.g. "pnhan_pass"
      .mode(SaveMode.Append) // hoặc Overwrite tùy bạn muốn
      .save()

    println(s"[MigrateToPostgres] Đã ghi thành công table '$tableName' vào PostgreSQL.")

    spark.stop()
    // conn.close() // nếu bạn đã gọi getConnection(), nhớ đóng lại
  }
}
