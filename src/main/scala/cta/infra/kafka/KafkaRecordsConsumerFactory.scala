package cta.infra.kafka

import cats.effect.IO
import cta.domain.{Records, RecordsConsumer, RecordsConsumerProvider}

class KafkaRecordsConsumerFactory extends RecordsConsumerProvider {

  override def consumerFor(topicName: String)(callback: RecordsConsumer => IO[Records]): IO[Records] = {
    KafkaRecordsConsumer.resource.use { consumer =>
      callback(consumer)
    }
  }
}
