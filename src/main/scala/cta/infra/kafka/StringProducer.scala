package cta.infra.kafka

import cats.effect.{IO, Resource}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import java.util.Properties

class StringProducer(producer: KafkaProducer[String, String]) {
  def send(topic: String, key: String, value: String): IO[RecordMetadata] = {
    val record = new ProducerRecord[String, String](topic, key, value)
    IO.async_[RecordMetadata] { callback =>
      producer.send(record, (metadata: RecordMetadata, exception: Exception) => {
        if (exception == null) {
          callback(Right(metadata))
        } else {
          callback(Left(exception))
        }
      })
    }
  }
}

object StringProducer {
  val properties:Properties = {
    val props = Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    props
  }
  def resource: Resource[IO, StringProducer] =
    Resource
      .fromAutoCloseable {
        IO.delay {
          KafkaProducer[String, String](properties)
        }
      }
      .map(StringProducer(_))
}