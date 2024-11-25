package cta.infra.kafka

import cats.effect.{IO, Resource}
import cta.domain.{Record, RecordMetadata, Records, RecordsConsumer}
import org.apache.kafka.clients.consumer.{Consumer, ConsumerConfig, ConsumerRecord, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer

import java.time.Duration
import java.util.Properties
import java.{lang, util}
import scala.jdk.CollectionConverters.*

class KafkaRecordsConsumer(consumer: Consumer[String, String]) extends RecordsConsumer {
  private val partitionCount = 3

  override def consume(topicName: String, offsetIndex: Option[Int], count: Option[Int]): IO[Records] = {
    val partitionIndexes: Seq[Int] = (0 until partitionCount - 1)
    val partitions = (0 until partitionCount - 1).map(partitionIndex => new TopicPartition(topicName, partitionIndex))
    val partitionsJavaList = partitions.asJava
    for {
      _ <- IO.blocking {
        consumer.assign(partitions.asJava)
        partitions.foreach(partition => consumer.seek(partition, offsetIndex.getOrElse(0)))
      }
      response <- pollUntilCount(List.empty, count)
    } yield Records(response)
  }

  private def poll(): IO[List[Record]] = IO.blocking {
    val records = consumer.poll(Duration.ofMillis(1000)).asScala.toList
    records.map((r: ConsumerRecord[String, String]) => {
      Record(RecordMetadata(r.partition(), r.offset(), r.key()), r.value())
    })
  }

  private def pollUntilCount(records: List[Record], count: Option[Int]): IO[List[Record]] = {
    count match {
      case Some(c) if records.size >= c => IO.pure(records.slice(0, c))
      case _ => {
        // flatMap trampolined for stack safety
        poll().flatMap(newRecords => {
          if (count.isEmpty && newRecords.isEmpty) IO.pure(records)
          else pollUntilCount(records ++ newRecords, count)
        })
      }
    }
  }
}

object KafkaRecordsConsumer {
  val properties:Properties = {
    val props = Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    props
  }
  
  def resource: Resource[IO, KafkaRecordsConsumer] = {
    Resource
      .fromAutoCloseable {
        IO.delay {
          KafkaConsumer[String, String](properties)
        }
      }
      .map(KafkaRecordsConsumer(_))
  }
}