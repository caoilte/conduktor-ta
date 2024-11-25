package cta.app

import cats.effect.IO
import cta.infra.http4s.CtaRoutes
import cta.infra.kafka.KafkaRecordsConsumerFactory
import munit.CatsEffectSuite
import org.http4s.{Method, Request, Status}
import org.http4s.implicits.*
import snapshot4s.generated.*
import snapshot4s.munit.SnapshotAssertions

// Might need to run the following before these tests will pass
//
// kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic first_topic
// kafka-topics.sh --bootstrap-server localhost:9092 --topic first_topic --create --partitions 3 --replication-factor 1
// sbt "runMain cta.app.RecordsLoader"
//
// Of course... all bets are off if partition consumption order is non-deterministic... ¯\_(ツ)_/¯
class E2ESuite extends CatsEffectSuite with SnapshotAssertions {
  val routes = new CtaRoutes(new KafkaRecordsConsumerFactory).routes

  test("Can read a single record from offset 0") {
    val getPeople: Request[IO] = Request[IO](Method.GET, uri"/topic/first_topic/0?count=1")

    for {
      response <- routes.orNotFound(getPeople)
      _ = assertEquals(response.status, Status.Ok)
      responseString <- response.bodyText.compile.string
      _ = assertFileSnapshot(responseString, "cta/app/single-record-from-offset-0.json")
    } yield ()
  }

  test("Can read a last few records if given only final(ish) offset") {
    val getPeople: Request[IO] = Request[IO](Method.GET, uri"/topic/first_topic/166")

    for {
      response <- routes.orNotFound(getPeople)
      _ = assertEquals(response.status, Status.Ok)
      responseString <- response.bodyText.compile.string
      _ = assertFileSnapshot(responseString, "cta/app/final-records.json")
    } yield ()
  }
}
