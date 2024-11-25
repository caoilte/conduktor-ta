package cta.infra.http4s

import cats.effect.IO
import cta.domain.RecordsConsumerProvider
import io.circe.{Encoder, Printer}
import org.http4s.circe.jsonEncoderWithPrinterOf
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.*
import org.http4s.{EntityEncoder, HttpRoutes, Response}

class CtaRoutes[T](avroRecordsConsumerProvider: RecordsConsumerProvider) {

  implicit def circeEntityEncoder[A: Encoder]: EntityEncoder[IO, A] =
    jsonEncoderWithPrinterOf[IO, A](Printer.spaces2)

  object MaybeCountParam extends OptionalQueryParamDecoderMatcher[Int]("count")

  val routes: HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl.*
    HttpRoutes.of[IO] {
      case GET -> Root / "topic" / topicName / IntVar(offset) :? MaybeCountParam(count) =>
        handleRequest(topicName, Some(offset), count)
      case GET -> Root / "topic" / topicName :? MaybeCountParam(count) =>
        handleRequest(topicName, None, count)
    }
  }

  def handleRequest(topicName: String, offset: Option[Int], count: Option[Int]): IO[Response[IO]] = {
    (for {
      records <- avroRecordsConsumerProvider.consumerFor(topicName) { consumer =>
        consumer.consume(topicName, offset, count)
      }
      // switch for nicer outcome, but shows score wrong...
//      jsonRecords <- JsonRecordAdapter.adapt(records)
      resp <- Ok(records) // Ok(jsonRecords)
    } yield resp
      ).recoverWith {
      case e: Throwable => {
        e.printStackTrace()
        InternalServerError("Artist not found")
      }
    }
  }
}
