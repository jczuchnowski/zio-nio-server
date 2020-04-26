import zio._
import zio.clock._
import zio.console._

package object server {

  type Server = Has[Server.Service]

  object Server {

    trait Service {
      def start: IO[Exception, Unit]
    }

    def live(host: String, port: Int): ZLayer[Clock with Console, Nothing, Server] = ZLayer.fromFunction { services =>
      new Service {
        val start = NioServer.start(host, port).provide(services)
      }
    }

    def start: ZIO[Server, Exception, Unit] = ZIO.accessM(_.get.start)
  }
}
