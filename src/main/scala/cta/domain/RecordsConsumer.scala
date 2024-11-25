package cta.domain

import cats.effect.IO

trait RecordsConsumer {
  def consume(topicName: String, offsetIndex: Option[Int], count:Option[Int]): IO[Records]
}
