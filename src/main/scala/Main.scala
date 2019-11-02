package app

import zio._
import zio.clock._
import zio.console._
import zio.duration._
import zio.nio.channels.AsynchronousServerSocketChannel
import zio.nio._
import zio.stream._

import scala.language.postfixOps
import zio.nio.channels.AsynchronousSocketChannel

object Main extends App {

  def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    Server.server(9002)
      .mapMPar(10) { chan =>
        chan.read(24).flatMap { chunk =>
          putStrLn("Message: " + chunk.mkString)
        } *> sleep(2.seconds) *> putStrLn("Done")
        .unit
      }
      .runDrain
      .foldM(e => putStrLn("Error: " + e.getMessage) *> ZIO.succeed(1), _ => ZIO.succeed(0))
}

object Server {

  def start(port: Int): RIO[Console with Clock, Unit] =
    AsynchronousServerSocketChannel().use { server =>
      for {
        socketAddress <- SocketAddress.inetSocketAddress(port)
        _             <- server.bind(socketAddress)
        _             <- putStrLn("Listening on port " + port)

        _ <- server.accept.use { socketChannel =>
               for {
                 _     <- putStrLn("Connected.")
                 _     <- ZIO.sleep(5 seconds)
                 chunk <- socketChannel.read(24)
                 _     <- putStrLn("Message: " + chunk.mkString)
               } yield ()
             }.forever
      } yield ()
    }

    def start2[R](port: Int, numWorkers: Int)(f: AsynchronousSocketChannel => RIO[R, Unit]) = 
      for {
        server        <- AsynchronousServerSocketChannel()
        socketAddress <- SocketAddress.inetSocketAddress(port).toManaged_
        acceptPermit  <- Semaphore.make(1).toManaged_
        _             <- server.bind(socketAddress).toManaged_            
        workerFibers  <- ZManaged.collectAll {
          List.fill(numWorkers) {
            (for {
              permitAndRelease <- acceptPermit.withPermitManaged[Any, Nothing].withEarlyRelease
              (release, _) = permitAndRelease
              chan <- server.accept
              _ <- release.toManaged_
              _ <- f(chan).toManaged_
            } yield ()).fork
          }
      }
      } yield ()

    def server(port: Int): ZStream[Any, Throwable, AsynchronousSocketChannel] = {
      val server = 
        for {
          server        <- AsynchronousServerSocketChannel()
          socketAddress <- SocketAddress.inetSocketAddress(port).toManaged_
          _             <- server.bind(socketAddress).toManaged_
          acceptSemaphore <- Semaphore.make(1).toManaged_
        } yield (acceptSemaphore, server)

      ZStream.managed(server).flatMap { case (acceptSem, server) =>
        val fiber = 
          ZStream.managed(acceptSem.withPermitManaged *> server.accept)
            .mapM { chan =>
              chan.read(24).flatMap { chunk =>
                putStrLn("Message: " + chunk.mkString)
              } *> sleep(2.seconds) *> putStrLn("Done")
              .unit
            }
            .runDrain

        val fibers = ZManaged.collectAll(List.fill(10)(fiber.toManaged_.fork))

        ZStream.managed(fibers)
      }
    }
}
