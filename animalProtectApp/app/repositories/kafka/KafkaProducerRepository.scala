package repositories.kafka

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.SendProducer
import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer
import play.api.inject.ApplicationLifecycle
import repositories.kafka.model.Message

import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}

class KafkaProducerRepository @Inject() (lifecycle: ApplicationLifecycle) extends LazyLogging:
  implicit val system: ActorSystem = ActorSystem("producer-sample")

  private val kafkaTopic = "image_recognition"
  private val imageRecognitionAppKey = "job"

  private val producerSettings =
    ProducerSettings(system, new StringSerializer, new StringSerializer)

  private val producer = SendProducer(producerSettings)

  lifecycle.addStopHook(() => producer.close())

  def sendToImageRecognitionApp(message: Message): Unit =
    logger.info("Send job to imageRecognitionApp")

    val record = generateRecord(imageRecognitionAppKey, message)
    val send: Future[RecordMetadata] = producer.send(record)

    // Blocking here for illustration only, you need to handle the future result
    Await.result(send, 2.seconds)

  private def generateRecord(key: String, message: Message): ProducerRecord[String, String] =
    val jsonMessage = message.asJson.noSpaces
    new ProducerRecord[String, String](kafkaTopic, key, jsonMessage)
