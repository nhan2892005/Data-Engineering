error id: file://<WORKSPACE>/SparkScala/introduction/src/main/scala/com/intro/ReadCSV.scala:builder.
file://<WORKSPACE>/SparkScala/introduction/src/main/scala/com/intro/ReadCSV.scala
empty definition using pc, found symbol in pc: builder.
semanticdb not found
empty definition using fallback
non-local guesses:
	 -org/apache/spark/sql/SparkSession.builder.
	 -org/apache/spark/sql/SparkSession.builder#
	 -org/apache/spark/sql/SparkSession.builder().
	 -SparkSession.builder.
	 -SparkSession.builder#
	 -SparkSession.builder().
	 -scala/Predef.SparkSession.builder.
	 -scala/Predef.SparkSession.builder#
	 -scala/Predef.SparkSession.builder().
offset: 150
uri: file://<WORKSPACE>/SparkScala/introduction/src/main/scala/com/intro/ReadCSV.scala
text:
```scala
package 
import org.apache.spark.sql.{SparkSession, DataFrame}

object ReadCSV {
    def createSparkSession(): SparkSession = {
        SparkSession.b@@uilder()
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
```


#### Short summary: 

empty definition using pc, found symbol in pc: builder.