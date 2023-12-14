package repositories.kafka.model

sealed trait Message() 

case class ImageRecognitionMessage(bucketId: String, imageByteArray : Array[Byte]) extends Message
