error id: file://<WORKSPACE>/SparkScala/introduction/src/main/scala/com/intro/ReadCSV.scala:[50..56) in Input.VirtualFile("file://<WORKSPACE>/SparkScala/introduction/src/main/scala/com/intro/ReadCSV.scala", "import org.apache.spark.sql.SparkSession

class 

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
}")
file://<WORKSPACE>/file:<WORKSPACE>/SparkScala/introduction/src/main/scala/com/intro/ReadCSV.scala
file://<WORKSPACE>/SparkScala/introduction/src/main/scala/com/intro/ReadCSV.scala:5: error: expected identifier; obtained object
object ReadCSV {
^
#### Short summary: 

expected identifier; obtained object