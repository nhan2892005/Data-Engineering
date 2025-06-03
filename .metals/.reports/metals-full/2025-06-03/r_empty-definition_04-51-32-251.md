error id: file://<WORKSPACE>/SparkScala/introduction/build.sbt:
file://<WORKSPACE>/SparkScala/introduction/build.sbt
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 343
uri: file://<WORKSPACE>/SparkScala/introduction/build.sbt
text:
```scala
val scala3Version = "3.7.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Introduction",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.3.2",
  "org.ap@@ache.spark" %% "spark-sql"  % "3.3.2",
)

```


#### Short summary: 

empty definition using pc, found symbol in pc: 