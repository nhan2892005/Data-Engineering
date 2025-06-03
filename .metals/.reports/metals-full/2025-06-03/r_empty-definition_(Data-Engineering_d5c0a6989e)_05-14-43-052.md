error id: file://<WORKSPACE>/SparkScala/introduction/src/main/scala/com/intro/Main.scala:`<none>`.
file://<WORKSPACE>/SparkScala/introduction/src/main/scala/com/intro/Main.scala
empty definition using pc, found symbol in pc: `<none>`.
semanticdb not found
empty definition using fallback
non-local guesses:

offset: 239
uri: file://<WORKSPACE>/SparkScala/introduction/src/main/scala/com/intro/Main.scala
text:
```scala
package com.intro
import org.apache.spark.sql.SparkSession

object Main {
    def main(args: Array[String]): Unit = {
        // Create a Spark session
        val spark = ReadCSV.createSparkSession()

        // Specify the path to your C@@SV file
        val filePath = "../data/AAPL.csv"
        
        // Read and display the CSV file
        ReadCSV.read_csv(spark, filePath)

        // Stop the Spark session
        spark.stop()
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.