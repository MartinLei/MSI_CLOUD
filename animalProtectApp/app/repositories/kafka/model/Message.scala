package repositories.kafka.model

sealed trait Message():
  val bucketId: String

case class ImageRecognitionJobMessage(bucketId: String, imageByteArray: Array[Byte]) extends Message {}

case class ImageRecognitionResultMessage(bucketId: String, detectedObject: Array[DetectedObject]) extends Message {}

case class DetectedObject(bbox: Array[Double], `class`: String, score: Double)
