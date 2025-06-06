package com.migratedata

import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object MigrateToPostgres {

  def convertComplexTypes(df: DataFrame)(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._
    
    // Get all columns and their data types
    val fields = df.schema.fields
    
    // Start with the original DataFrame
    var resultDF = df
    
    // Convert each complex type column to JSON
    fields.foreach { field =>
      field.dataType match {
        case _: ArrayType | _: StructType | _: MapType =>
          resultDF = resultDF.withColumn(
            s"${field.name}_json", 
            to_json(col(field.name))
          )
        case _ => // Keep simple types as-is
      }
    }

    // Select all original simple type columns and JSON converted columns
    val selectExpr = fields.map { field =>
      field.dataType match {
        case _: ArrayType | _: StructType | _: MapType =>
          col(s"${field.name}_json")
        case _ =>
          col(field.name)
      }
    }

    resultDF.select(selectExpr: _*)
  }

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = BigQueryConnection.createSparkSession()

    // List of tables from GitHub public dataset
    val tables = List("commits",
                      "contents",
                      "files",
                      "languages",
                      "licenses",
                      "sample_commits",
                      "sample_contents",
                      "sample_files",
                      "sample_repos")

    tables.foreach { tableName =>
      println(s"Processing table: $tableName")
      try {
        // Read from BigQuery public dataset
        val bq = new BigQueryConnection(spark)
        val df = bq.readGithubDataset(tableName).repartition(10).cache()
        
        // Print schema and sample data
        println(s"\nSchema for table: $tableName")
        df.printSchema()
        println(s"\nSample data for table: $tableName")
        df.show(5)
        
        // Convert complex types to JSON
        val jsonDF = convertComplexTypes(df)(spark).coalesce(5)

        // Write to PostgreSQL
        println(s"Writing to PostgreSQL table: $tableName")
        jsonDF.write
          .format("jdbc")
          .option("url", AppConfig.get("POSTGRES_URL"))
          .option("dbtable", tableName)
          .option("user", AppConfig.get("POSTGRES_USER"))
          .option("password", AppConfig.get("POSTGRES_PASSWORD"))
          .mode(SaveMode.Append)
          .save()

        println(s"Successfully migrated table: $tableName")
        // Clear cache
        df.unpersist()
      } catch {
        case e: Exception =>
          println(s"Error processing table $tableName: ${e.getMessage}")
      }
    }
    spark.stop()
  }
}