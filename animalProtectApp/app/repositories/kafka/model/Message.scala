package repositories.kafka.model

import models.Item

import scala.beans.BeanProperty

sealed trait Message():
  val bucketId: String

case class ImageRecognitionJobMessage(projectId: String, bucketId: String, imageByteArray: Array[Byte])
    extends Message {}

case class ImageRecognitionResultMessage(projectId: String, bucketId: String, detectedObject: List[DetectedObject])
    extends Message {}

case class DetectedObject(bbox: List[Double], `class`: String, score: Double)
