package cta.infra.http4s


import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._

case class JsonRecords(ctRoot: List[JsonRecord])

object JsonRecords {
  given Decoder[JsonRecords] = deriveDecoder
  given Encoder[JsonRecords] = deriveEncoder
}