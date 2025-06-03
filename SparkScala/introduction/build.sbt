name := "SparkApp"
version := "0.1"
scalaVersion := "2.12.15"

// Dependency Spark (phiên bản tương thích Hadoop nếu cần)
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.3.2",
  "org.apache.spark" %% "spark-sql"  % "3.3.2"
)

// Cấu hình Assembly: 
// Loại bỏ Scala library để tránh trùng lặp; Spark runtime đã có Scala.
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

// Nếu có file log4j.properties hoặc resources nào, 
// đảm bảo sbt-assembly không xung đột.
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
