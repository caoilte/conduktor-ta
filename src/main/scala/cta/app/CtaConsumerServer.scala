package cta.app

import cats.effect.kernel.Resource
import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.*
import cta.infra.http4s.CtaRoutes
import cta.infra.kafka.KafkaRecordsConsumerFactory
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger

object CtaConsumerServer extends IOApp {

  private val routes = (new CtaRoutes(new KafkaRecordsConsumerFactory).routes).orNotFound
  private val httpApp = Logger.httpApp(true, true)(routes)

  private val host = ipv4"0.0.0.0"
  private val port = Port.fromInt(9090).get

  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      server <- EmberServerBuilder
        .default[IO]
        .withHost(host)
        .withPort(port)
        .withHttpApp(httpApp)
        .build
      _ <- Resource.eval(IO(println(s"👂 HTTP server listening on http://$host:$port")))
    } yield server).useForever
  }
}
