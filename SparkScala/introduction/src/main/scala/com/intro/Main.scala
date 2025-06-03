package com.intro

object Main {
  def main(args: Array[String]): Unit = {
    val spark = ReadCSV.createSparkSession()
    val filePath = "data/AAPL.csv"
    val df = ReadCSV.read_csv(spark, filePath)
    df.show() // Display the DataFrame content
    df.printSchema() // Print the schema of the DataFrame
  }
}
