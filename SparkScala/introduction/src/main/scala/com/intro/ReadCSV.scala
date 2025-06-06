package com.intro
import org.apache.spark.sql.{SparkSession, DataFrame}

object ReadCSV {
    def createSparkSession(): SparkSession = {
        SparkSession.builder()
            .appName("Read CSV Example")
            .master("local[*]") // Use local mode for testing
            .getOrCreate()
    }

    def read_csv(spark: SparkSession, filePath: String): DataFrame = {
        spark.read
            .option("header", "true")
            .option("inferSchema", "true")
            .csv(filePath)
    }
}