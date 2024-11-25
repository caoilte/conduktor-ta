package cta.infra.http4s

import cats.effect.{IO, Ref}
import cta.domain.*
import munit.CatsEffectSuite
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.implicits.*
import org.http4s.{Method, Request, Status}

class CtaRoutesSuite extends CatsEffectSuite {

  val aRecordsResult = Records(List(Record(RecordMetadata(0,0,"key"),"""{"name" : "John Doe"}""")))

  case class ConsumerCallResults(topicName: String, offset: Option[Int], count: Option[Int])

  class RecordingRecordsConsumerProvider(ref: Ref[IO, Option[ConsumerCallResults]]) extends RecordsConsumerProvider {
    def consumerFor(topicName: String)(callback: RecordsConsumer => IO[Records]):IO[Records] = {
      callback((topicName: String, offset: Option[Int], count: Option[Int]) => {
        ref.set(Some(ConsumerCallResults(topicName, offset, count))).map(_ => aRecordsResult)
      })
    }
  }

  test("Service correctly handles request if all parameters passed and responds as expected") {
    for {
      consumerCallResults <- Ref.of[IO, Option[ConsumerCallResults]](None)
      routes = new CtaRoutes(RecordingRecordsConsumerProvider(consumerCallResults)).routes
      getPeople: Request[IO] = Request[IO](Method.GET, uri"/topic/first_topic/6?count=5")
      response <- routes.orNotFound(getPeople)
      _ = assertEquals(response.status, Status.Ok)
      _ = assertIO(response.as[Records], aRecordsResult)
      _ <- assertIO(consumerCallResults.get, Some(ConsumerCallResults("first_topic", Some(6), Some(5))))
    } yield ()
  }


  test("Service correctly handles request if count not passed and responds as expected") {
    for {
      consumerCallResults <- Ref.of[IO, Option[ConsumerCallResults]](None)
      routes = new CtaRoutes(RecordingRecordsConsumerProvider(consumerCallResults)).routes
      getPeople: Request[IO] = Request[IO](Method.GET, uri"/topic/first_topic/6")
      response <- routes.orNotFound(getPeople)
      _ = assertEquals(response.status, Status.Ok)
      _ = assertIO(response.as[Records], aRecordsResult)
      _ <- assertIO(consumerCallResults.get, Some(ConsumerCallResults("first_topic", Some(6), None)))
    } yield ()
  }

  test("Service correctly handles request if offset and count not passed and responds as expected") {
    for {
      consumerCallResults <- Ref.of[IO, Option[ConsumerCallResults]](None)
      routes = new CtaRoutes(RecordingRecordsConsumerProvider(consumerCallResults)).routes
      getPeople: Request[IO] = Request[IO](Method.GET, uri"/topic/first_topic")
      response <- routes.orNotFound(getPeople)
      _ = assertEquals(response.status, Status.Ok)
      _ = assertIO(response.as[Records], aRecordsResult)
      _ <- assertIO(consumerCallResults.get, Some(ConsumerCallResults("first_topic", None, None)))
    } yield ()
  }

  test("Service correctly handles request if just topic and count passed and responds as expected") {
    for {
      consumerCallResults <- Ref.of[IO, Option[ConsumerCallResults]](None)
      routes = new CtaRoutes(RecordingRecordsConsumerProvider(consumerCallResults)).routes
      getPeople: Request[IO] = Request[IO](Method.GET, uri"/topic/first_topic?count=5")
      response <- routes.orNotFound(getPeople)
      _ = assertEquals(response.status, Status.Ok)
      _ = assertIO(response.as[Records], aRecordsResult)
      _ <- assertIO(consumerCallResults.get, Some(ConsumerCallResults("first_topic", None, Some(5))))
    } yield ()
  }
}
