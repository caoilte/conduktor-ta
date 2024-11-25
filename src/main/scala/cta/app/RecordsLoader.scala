package cta.app

import cats.effect.{IO, IOApp}
import cats.implicits.*
import cta.infra.kafka.StringProducer

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import io.circe.*
import io.circe.parser.*

object RecordsLoader extends IOApp.Simple {

  def runMain(): IO[Unit] = {
    val peopleString = new String(Files.readAllBytes(Paths.get("random-people-data.json")), StandardCharsets.UTF_8)

    for {
      peopleJson:Json <- IO.fromEither(parse(peopleString))
      personArray:List[Json] <- IO.fromEither(peopleJson.hcursor.get[List[Json]]("ctRoot"))
      _ <- StringProducer.resource.use(producer => {
        personArray.map(personJson => {
          sendPerson(producer, personJson)
        }).sequence
      })
    } yield ()
  }

  private def sendPerson(producer: StringProducer, personJson: Json):IO[Unit] = {
    for {
      id <- IO.fromEither(personJson.hcursor.get[String]("_id"))
      _ <- producer.send("first_topic", id, Printer.spaces2.print(personJson))
    } yield ()
  }

  val run: IO[Unit] = runMain()
}
