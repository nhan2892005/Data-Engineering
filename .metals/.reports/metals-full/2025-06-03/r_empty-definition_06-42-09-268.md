error id: file://<WORKSPACE>/SparkScala/introduction/build.sbt:`<none>`.
file://<WORKSPACE>/SparkScala/introduction/build.sbt
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 270
uri: file://<WORKSPACE>/SparkScala/introduction/build.sbt
text:
```scala
import Dependencies._

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.intro"
ThisBuild / organizationName := "intro"

lazy val root = (project in file("."))
  .settings(
    name := "introd@@uction",
    libraryDependencies += munit % Test
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.3.2",
  "org.apache.spark" %% "spark-sql"  % "3.3.2",
)
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.