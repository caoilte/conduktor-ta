package cta.domain

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._

// I originally included the timestamp as well, but it was tricky for my e2e tests so I took it out.
case class RecordMetadata(partition: Int, offset: Long, key: String)

object RecordMetadata {
  given Decoder[RecordMetadata] = deriveDecoder
  given Encoder[RecordMetadata] = deriveEncoder
}