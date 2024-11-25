package cta.infra.http4s

import cats.effect.IO
import cta.domain.{Record, Records}
import io.circe.*
import io.circe.parser.*
import cats.implicits.*

// Reads nicer if you use this, but doesn't format consistently with the input (eg score decimal places) - which felt more important!
object JsonRecordAdapter {
  def adapt(records: Records):IO[JsonRecords] = {
    records.ctRoot.map(adapt).sequence.map(records => JsonRecords(records))
  }

  private def adapt(record: Record):IO[JsonRecord] = {
    IO.fromEither(parse(record.value)).map { json =>
      JsonRecord(record.metadata, json)
    }
  }
}