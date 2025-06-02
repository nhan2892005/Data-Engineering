package com.migratedata

import org.apache.spark.sql.{DataFrame, SparkSession}

class BigQueryConnection(spark: SparkSession) {
  def readTable(tableName: String, dataset: String = "intro", project: String = "bigquery-public-data"): DataFrame = {
    spark.read
      .format("bigquery")
      .option("table", s"$project.$dataset.$tableName")
      .load()
  }

  def readGithubDataset(tableName: String): DataFrame = {
    readTable(tableName, dataset = "github_repos", project = "bigquery-public-data")
  }
}

object BigQueryConnection {
  def createSparkSession(): SparkSession = {
    val projectId = AppConfig.get("GOOGLE_CLOUD_PROJECT")
    val credentialsFile = AppConfig.get("GOOGLE_APPLICATION_CREDENTIALS")
    
    SparkSession.builder()
      .appName("BigQueryToPostgres")
      .config("spark.master", "local[*]")
      // Add memory configurations
      .config("spark.driver.memory", "4g")
      .config("spark.executor.memory", "4g")
      .config("spark.memory.offHeap.enabled", "true")
      .config("spark.memory.offHeap.size", "4g")
      // BigQuery configs
      .config("viewsEnabled", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", credentialsFile)
      .config("spark.sql.extensions", "com.google.cloud.spark.bigquery.BigQuerySparkSessionExtension")
      .config("materializationDataset", AppConfig.get("BQ_MATERIALIZATION_DATASET"))
      .config("parentProject", projectId)
      .getOrCreate()
  }
}