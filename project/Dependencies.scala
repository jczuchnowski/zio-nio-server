import sbt._

object Dependencies {
  lazy val zio    = "dev.zio" %% "zio"     % "1.0.0-RC13"
  lazy val zioNio = "dev.zio" %% "zio-nio" % "0.2.1"
  lazy val zioStreams    = "dev.zio" %% "zio-streams"     % "1.0.0-RC13"
}
