package com.example.migrator

import com.google.cloud.bigquery._
import io.github.cdimascio.dotenv.Dotenv
import scala.jdk.CollectionConverters._

object BigQueryConnection {
  // Khởi tạo Dotenv
  private val dotenv = Dotenv.configure().ignoreIfMissing().load()

  /**  
   * Lấy client BigQuery dựa vào projectId từ file .env  
   */
  def getClient(): BigQuery = {
    val projectId = dotenv.get("BQ_PROJECT_ID")
    if (projectId == null) {
      throw new RuntimeException("Thiếu BQ_PROJECT_ID trong .env")
    }
    BigQueryOptions
      .newBuilder()
      .setProjectId(projectId)
      .build()
      .getService
  }

  /**  
   * Chạy query test để kiểm tra kết nối đến BigQuery  
   */
  def testConnection(): Unit = {
    val projectId = dotenv.get("BQ_PROJECT_ID")
    val dataset   = dotenv.get("BQ_DATASET")
    if (projectId == null || dataset == null) {
      throw new RuntimeException("Thiếu BQ_PROJECT_ID hoặc BQ_DATASET trong .env")
    }

    val bq = getClient()
    val tableToTest   = "sample_commits"
    val qualifiedTable = s"`$projectId.$dataset.$tableToTest`"
    val query         = s"SELECT COUNT(*) AS total_rows FROM $qualifiedTable"

    println(s"[BigQueryConnection] Chạy kiểm tra truy vấn: $query")
    val queryConfig = QueryJobConfiguration.newBuilder(query).setUseLegacySql(false).build()
    val jobId       = JobId.of(s"test_conn_${System.currentTimeMillis()}")
    val queryJob    = bq.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build())
    queryJob.waitFor()

    if (queryJob == null) {
      throw new RuntimeException("[BigQueryConnection] Lỗi: Job BigQuery không tồn tại.")
    }
    if (queryJob.getStatus.getError != null) {
      throw new RuntimeException(s"[BigQueryConnection] Lỗi khi chạy truy vấn: ${queryJob.getStatus.getError.getMessage}")
    }

    val result: TableResult = queryJob.getQueryResults()
    val iter = result.iterateAll().iterator().asScala
    if (iter.hasNext) {
      val row       = iter.next()
      val totalRows = row.get("total_rows").getLongValue
      println(s"[BigQueryConnection] Tổng số dòng trong $qualifiedTable = $totalRows")
    } else {
      println(s"[BigQueryConnection] Không có kết quả trả về từ truy vấn kiểm tra.")
    }
  }

  def main(args: Array[String]): Unit = {
    try {
      println("[BigQueryConnection] Bắt đầu test kết nối BigQuery…")
      testConnection()
      println("[BigQueryConnection] Kiểm tra kết nối BigQuery thành công!")
    } catch {
      case e: Exception =>
        println(s"[BigQueryConnection] Exception: ${e.getMessage}")
        e.printStackTrace()
    }
  }
}
