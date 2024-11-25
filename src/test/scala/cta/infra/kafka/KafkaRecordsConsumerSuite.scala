package cta.infra.kafka

import cta.domain.{Record, RecordMetadata, Records}
import munit.CatsEffectSuite
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.TopicPartition

import scala.jdk.CollectionConverters.*
import java.time.Duration
import java.util

class KafkaRecordsConsumerSuite extends CatsEffectSuite {
  val testValue: String = "test"
  val testValue2: String = "test2"

  class StubbedConsumer(consumerRecords: List[ConsumerRecord[String, String]]) extends KafkaConsumer[String, String](KafkaRecordsConsumer.properties, null, null) {
    override def assign(partitions: util.Collection[TopicPartition]): Unit = {}

    override def seek(partition: TopicPartition, offset: Long): Unit = {}

    override def poll(timeout: Duration): ConsumerRecords[String, String] = ConsumerRecords(
      Map(TopicPartition("topic", 0) -> consumerRecords.asJava).asJava
    )
  }

  test("consume returns a single entry if that is the maximum requested even if there are more") {
    val records = List(
      ConsumerRecord("topic", 0, 0, "key", testValue),
      ConsumerRecord("topic", 0, 1, "key", testValue2)
    )
    val consumer: KafkaRecordsConsumer = KafkaRecordsConsumer(new StubbedConsumer(records))
    val res = consumer.consume("topic", Some(0), Some(1))
    assertIO(res, Records(List(
      Record(RecordMetadata(0, 0, "key"), testValue)
    )))
  }
}