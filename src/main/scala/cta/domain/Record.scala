package cta.domain

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._

case class Record(metadata: RecordMetadata, value: String)

object Record {
  given Decoder[Record] = deriveDecoder
  given Encoder[Record] = deriveEncoder
}