import Dependencies._

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.migratedata"
ThisBuild / organizationName := "MigrateData"

lazy val root = (project in file("."))
  .settings(
    name := "MigrateData",
    libraryDependencies += munit % Test,
    libraryDependencies += "no.nrk.bigquery" %% "bigquery-core" % nrkNoBigQueryVersion
    libraryDependencies += "org.postgresql" % "postgresql" % "42.5.4"
  )
