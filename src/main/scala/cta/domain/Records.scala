package cta.domain

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._

case class Records(ctRoot: List[Record])

object Records {
  given Decoder[Records] = deriveDecoder
  given Encoder[Records] = deriveEncoder
}