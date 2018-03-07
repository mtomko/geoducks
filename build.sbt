import libraries._

lazy val commonSettings = inThisBuild(
  List(
    organization := "org.marktomko",
    scalaVersion := "2.12.4",
    version := "0.0.1-SNAPSHOT"
  ))

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
      univocity,
      scalaCheck % Test,
      scalaTest  % Test))

lazy val bench = project
  .in(file("bench"))
  .settings(name := "geoducks-bench")
  .settings(commonSettings: _*)
  .dependsOn(core)
  .settings(
    assemblyJarName := "geoducks-bench-bin.jar",
    libraryDependencies ++= Seq(
      catsCore,
      catsEffect,
      fs2Core,
      fs2Io,
      monix,
      scalaCheck % Test,
      scalaTest  % Test))

