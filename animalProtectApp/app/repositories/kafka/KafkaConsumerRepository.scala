package repositories.kafka

import com.typesafe.scalalogging.LazyLogging
import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Flow, Keep, Sink}
import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging
import io.circe
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.inject.ApplicationLifecycle
import repositories.kafka.model.{ImageRecognitionJobMessage, ImageRecognitionResultMessage, Message}

import scala.concurrent.duration.Duration

class KafkaConsumerRepository @Inject() (lifecycle: ApplicationLifecycle) extends LazyLogging:
  implicit val system: ActorSystem = ActorSystem("image_recognition_done")
  private val consumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)

  private val mapToObject: Flow[ConsumerRecord[String, String], Message, NotUsed] =
    Flow[ConsumerRecord[String, String]]
      .map(message =>
        decode[Message](message.value()) match
          case Left(df: DecodingFailure) => throw new IllegalArgumentException(s"Error:${df.message}")
          case Left(pf: ParsingFailure)  => throw new IllegalArgumentException(s"Error:${pf.message}")
          case Right(value)              => value
      )

  private val saveToDB: Flow[Message, NotUsed, NotUsed] =
    Flow[Message]
      .map { message =>
        logger.info(s"Receive message with bucketId: ${message.bucketId}")

        val imageRecognitionResultMessage = message.asInstanceOf[ImageRecognitionResultMessage]
        logger.info(s"TODO save this ${imageRecognitionResultMessage}")
        NotUsed
      }

  private val (consumerControl, streamComplete) = Consumer
    .plainSource(consumerSettings, Subscriptions.topics("image_recognition_done"))
    .via(mapToObject)
    .via(saveToDB)
    .recover {
      case ex: RuntimeException =>
        logger.info(s"Caught exception: ${ex.getMessage}")
        "skipped"
    }
     .toMat(Sink.ignore)(Keep.both)
    .run()
  
  lifecycle.addStopHook { () =>
    logger.info("shutdown")
    consumerControl.shutdown()
  }
