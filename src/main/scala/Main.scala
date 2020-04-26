package app

import java.nio.channels.{ CancelledKeyException, SocketChannel => JSocketChannel }

import zio._
import zio.clock._
import zio.console._
import zio.duration._
import zio.nio.channels.AsynchronousServerSocketChannel
import zio.nio.core.channels._
import zio.nio.core.channels.SelectionKey.Operation
import zio.nio.core._
import zio.stream._

import scala.language.postfixOps
import zio.nio.channels.AsynchronousSocketChannel
import java.io.IOException
import java.util.concurrent.TimeUnit
import server.Server
import zio.zmx.Diagnostics

object Main extends App {

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val program = (Server.start.toManaged_ <&> Diagnostics.start).useForever
    
    val server      = Server.live("localhost", 9002)
    val diagnostics = Diagnostics.live(1111, Some("0.0.0.0"))

    program.provideCustomLayer(server ++ diagnostics)
      .foldM(e => putStrLn("Error: " + e.getMessage) *> ZIO.succeed(1), _ => ZIO.succeed(0))
  }
}
