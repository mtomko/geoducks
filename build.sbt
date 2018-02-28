import libraries._

lazy val commonSettings = inThisBuild(
  List(
    organization := "org.marktomko",
    scalaVersion := "2.12.4",
    version := "0.0.1-SNAPSHOT"
  ))


lazy val assemblySettings = Seq(
  assemblyJarName in assembly := "bin/geoducks.jar",
  assemblyMergeStrategy in assembly := {
    case "logback.xml" => MergeStrategy.first
    case "logback-test.xml" => MergeStrategy.discard
    case "sbt/sbt.autoplugins" => MergeStrategy.discard
    case x =>
      val old = (assemblyMergeStrategy in assembly).value
      old(x)
  }
)

lazy val geoducks = project
  .in(file("."))
  .settings(commonSettings: _*)
  .aggregate(core, bench)

lazy val core = project
  .in(file("core"))
  .settings(name := "geoducks-core")
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      catsCore,
      catsEffect,
      fs2Core,
      fs2Io,
      scalaCheck % Test,
      scalaTest  % Test))

lazy val bench = project
  .in(file("bench"))
  .settings(name := "geoducks-bench")
  .settings(commonSettings: _*)
  .settings(assemblySettings: _*)
  .dependsOn(core)
  .settings(
    libraryDependencies ++= Seq(
      catsCore,
      catsEffect,
      fs2Core,
      fs2Io,
      scalaCheck % Test,
      scalaTest  % Test))

