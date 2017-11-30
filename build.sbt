import libraries._

lazy val geoducks = project
  .in(file("."))
  .settings(
    inThisBuild(
      List(
        organization := "org.marktomko",
        scalaVersion := "2.12.4",
        version := "0.1.0-SNAPSHOT"
      )),
    name := "geoducks",
    libraryDependencies ++= Seq(
      catsCore,
      catsEffect,
      fs2Core,
      fs2Io,
      scalaCheck % Test,
      scalaTest  % Test
    )
  )
