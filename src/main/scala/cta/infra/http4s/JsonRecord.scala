package cta.infra.http4s

import cta.domain.RecordMetadata
import io.circe.Json
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*

case class JsonRecord(metadata: RecordMetadata, value: Json)

object JsonRecord {
  given Decoder[JsonRecord] = deriveDecoder
  given Encoder[JsonRecord] = deriveEncoder
}