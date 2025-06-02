// build.sbt

name := "BigQueryToPostgresMigration"
version := "1.0"
scalaVersion := "2.12.15"

// Add Maven Central explicitly (usually it’s on by default)
resolvers += "Maven Central" at "https://repo1.maven.org/maven2"

ThisBuild / useCoursier := false

libraryDependencies ++= Seq(
  // Spark core & SQL for Scala 2.12 (must match the Spark version you installed locally)
  // Here we use Spark 3.3.2 (prebuilt for Hadoop 3.x)
  "org.apache.spark" %% "spark-core" % "3.3.2",
  "org.apache.spark" %% "spark-sql"  % "3.3.2",

  // Spark–BigQuery Connector (with dependencies shaded)
  // Version 0.31.0 is known to work with Spark 3.3.x / Scala 2.12
  "com.google.cloud.spark" %% "spark-bigquery-with-dependencies" % "0.31.0"
    // Exclude the bad avro-mapred:1.12.0:hadoop2 that the connector might pull in
    exclude("org.apache.avro", "avro-mapred"),

  // Provide a valid avro-mapred:hadoop2 artifact (Maven Central only has 1.10.2-hadoop2)
  "org.apache.avro" % "avro-mapred" % "1.10.2" classifier "hadoop2",

  // PostgreSQL JDBC driver
  "org.postgresql" % "postgresql" % "42.5.1",

  // Dotenv to load .env if you use it
  "io.github.cdimascio" % "java-dotenv" % "5.2.2"
)
