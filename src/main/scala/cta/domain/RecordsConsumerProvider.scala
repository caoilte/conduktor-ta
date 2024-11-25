package cta.domain

import cats.effect.IO

// Could be done as Factory or Pool
trait RecordsConsumerProvider {
  def consumerFor(topicName: String)(callback: RecordsConsumer => IO[Records]):IO[Records]
}
