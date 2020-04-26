import sbt._

object Dependencies {
  val zioVersion      = "1.0.0-RC18-2"
  lazy val zio        = "dev.zio" %% "zio"         % zioVersion
  lazy val zioNio     = "dev.zio" %% "zio-nio"     % "1.0.0-RC6"
  lazy val zioStreams = "dev.zio" %% "zio-streams" % zioVersion
  lazy val zioZmx     = "dev.zio" %% "zio-zmx"     % "0.0.0+12-2c3c923b+20200421-2328-SNAPSHOT"
}
