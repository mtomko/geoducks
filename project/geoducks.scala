import sbt._

object versions {

  val cats       = "1.0.1"
  val catsEffect = "0.8"
  val fs2        = "0.10.2"
  val scalaCheck = "1.13.5"
  val scalaTest  = "3.0.5"

}

object libraries {
  lazy val catsCore   = "org.typelevel"  %% "cats-core"   % versions.cats
  lazy val catsEffect = "org.typelevel"  %% "cats-effect" % versions.catsEffect
  lazy val fs2Core    = "co.fs2"         %% "fs2-core"    % versions.fs2
  lazy val fs2Io      = "co.fs2"         %% "fs2-io"      % versions.fs2
  lazy val scalaCheck = "org.scalacheck" %% "scalacheck"  % versions.scalaCheck
  lazy val scalaTest  = "org.scalatest"  %% "scalatest"   % versions.scalaTest
}
