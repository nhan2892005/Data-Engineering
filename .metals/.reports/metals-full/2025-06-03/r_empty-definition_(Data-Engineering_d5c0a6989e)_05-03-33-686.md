error id: file://<WORKSPACE>/SparkScala/introduction/src/main/scala/com/intro/ReadCSV.scala:`<none>`.
file://<WORKSPACE>/SparkScala/introduction/src/main/scala/com/intro/ReadCSV.scala
empty definition using pc, found symbol in pc: `<none>`.
semanticdb not found
empty definition using fallback
non-local guesses:

offset: 657
uri: file://<WORKSPACE>/SparkScala/introduction/src/main/scala/com/intro/ReadCSV.scala
text:
```scala
import org.apache.spark.sql.SparkSession

object ReadCSV {
    def createSparkSession(): SparkSession = {
        SparkSession.builder()
            .appName("Read CSV Example")
            .master("local[*]") // Use local mode for testing
            .getOrCreate()
    }

    def read_csv(spark: SparkSession, filePath: String): Unit = {
        val df = spark.read
            .option("header", "true")
            .option("inferSchema", "true")
            .csv(filePath)

        df.show()
    }

    def main(args: Array[String]): Unit = {
        val spark = createSparkSession()

        // Specify the path to your CSV file
        val filePath = "@@path/to/your/file.csv" // Change this to your actual file path
        read_csv(spark, filePath)

        // Stop the Spark session
        spark.stop()
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.