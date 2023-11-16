package service

import com.typesafe.scalalogging.LazyLogging
import org.tensorflow.ndarray.{FloatNdArray, Shape}
import org.tensorflow.op.Ops
import org.tensorflow.op.core.{Constant, Placeholder, Reshape}
import org.tensorflow.op.image.{DecodeJpeg, EncodeJpeg}
import org.tensorflow.op.io.ReadFile
import org.tensorflow.types.{TFloat32, TString, TUint8}
import org.tensorflow.{Graph, Result, SavedModelBundle, Session, Tensor}

import java.util
import java.util.{HashMap, Map}
import scala.collection.immutable.TreeMap
import scala.jdk.CollectionConverters.*
import scala.util.Using

class FasterRcnn extends LazyLogging {

  val imagePath = "imageRecognitionApp/conf/tenserflow_model/image2.jpg"
  val modelPath = "imageRecognitionApp/conf/tenserflow_model/"
  lazy val model: SavedModelBundle = SavedModelBundle.load(modelPath, "serve")

  val cocoLabels: TreeMap[Int, String] = TreeMap(
    0 -> "person",
    1 -> "bicycle",
    2 -> "car",
    3 -> "motorcycle",
    4 -> "airplane" // TODO
  )

  def analyseOLD() = {

    val g = model.graph()
    val s = model.session()

    val tf = Ops.create(g)
    val fileName = tf.constant(imagePath)
    val readFile = tf.io.readFile(fileName)
    val runner = s.runner
    val options = DecodeJpeg.channels(3L)
    val decodeImage = tf.image.decodeJpeg(readFile.contents, options)
    //fetch image from file//fetch image from file
    val imageShape = runner.fetch(decodeImage).run.get(0).shape
    //reshape the tensor to 4D for input to model//reshape the tensor to 4D for input to model
    val reshape = tf.reshape(decodeImage, tf.array(1, imageShape.get(0), imageShape.get(1), imageShape.get(2)))

    val reshapeTensor = s.runner.fetch(reshape).run.get(0).asInstanceOf[TUint8]
    val feedDict: util.Map[String, Tensor] = new util.HashMap[String, Tensor]
    //The given SavedModel SignatureDef input//The given SavedModel SignatureDef input
    feedDict.put("input_tensor", reshapeTensor)
    //The given SavedModel MetaGraphDef key//The given SavedModel MetaGraphDef key
    val outputTensorMap: Result = model.function("serving_default").call(feedDict)
    //detection_classes, detectionBoxes etc. are model output names//detection_classes, detectionBoxes etc. are model output names

    val detectionClasses = outputTensorMap.get("detection_classes").get.asInstanceOf[TFloat32]
    val detectionBoxes = outputTensorMap.get("detection_boxes").get.asInstanceOf[TFloat32]
    val rawDetectionBoxes = outputTensorMap.get("raw_detection_boxes").get.asInstanceOf[TFloat32]
    val numDetections = outputTensorMap.get("num_detections").get.asInstanceOf[TFloat32]
    val detectionScores = outputTensorMap.get("detection_scores").get.asInstanceOf[TFloat32]
    val rawDetectionScores = outputTensorMap.get("raw_detection_scores").get.asInstanceOf[TFloat32]
    val detectionAnchorIndices = outputTensorMap.get("detection_anchor_indices").get.asInstanceOf[TFloat32]
    val detectionMulticlassScores = outputTensorMap.get("detection_multiclass_scores").get.asInstanceOf[TFloat32]
    val numDetects = numDetections.getFloat(0).toInt

    detectionClasses.scalars.forEachIndexed((coords, ss) => {
      var aa = ss.getFloat(0L, 1L)
      val bb = aa.toInt
      System.out.println("Value " + bb + " found at " + coords + " label: ")

    })

    s.close()
    g.close()
    //      try {
    //        // Load and preprocess the image
    //        val imageTensor = preprocessImage(imagePath)
    //
    //        // Prepare input and run inference
    //        val inputs = Array[Nothing](imageTensor)
    //        val outputs = model.session.runner.feed("image_tensor", imageTensor) // Replace with input tensor name from your
    //          .fetch("detection_classes") // Replace with output tensor name for class
    //          .fetch("detection_boxes") // Replace with output tensor name for boxes
    //          .run
    //
    //        // Close resources
    //        imageTensor.close
    //        classes.close
    //        boxes.close
    //      } catch {
    //        case e: Exception =>
    //          e.printStackTrace()
    //      } finally if (model != null) model.close()
    //
    //
    //      logger.info("Found Objects:")
    //      for (i <- 0L to detectionClasses.size()) {
    //        logger.info("Index: " + detectionClasses.get(i))
    //      }
    //    }

    //  // Method to preprocess the image (resize, normalize, etc.)
    //  private def preprocessImage(imagePath: String): Nothing = {
    //    // Implement image preprocessing logic
    //    // Load the image and convert it into a Tensor
    //    // Resize, normalize pixel values, etc.
    //    null
  }

  private def transformImage(g: Graph, s: Session): Reshape[TUint8] = {
    val tf = Ops.create(g)
    val fileName = tf.constant(imagePath)
    val readFile = tf.io.readFile(fileName)
    val runner = s.runner
    val options = DecodeJpeg.channels(3L)
    val decodeImage = tf.image.decodeJpeg(readFile.contents, options)
    //fetch image from file
    val imageShape = runner.fetch(decodeImage).run.get(0).shape
    //reshape the tensor to 4D for input to model
    val reshape = tf.reshape(decodeImage, tf.array(1, imageShape.get(0), imageShape.get(1), imageShape.get(2)))
    reshape
  }

  def analyse(): Unit = {
    val g = model.graph()
    val s = model.session()

    val reshape = transformImage(g, s)
    val reshapeTensor = s.runner.fetch(reshape).run.get(0).asInstanceOf[TUint8]

    val feedDict: util.Map[String, Tensor] = new util.HashMap()
    //The given SavedModel SignatureDef input
    feedDict.put("input_tensor", reshapeTensor)
    //The given SavedModel MetaGraphDef key
    val outputTensorMap: Result = model.function("serving_default").call(feedDict)
    //detection_classes, detectionBoxes etc. are model output names
    val detectionClasses = outputTensorMap.get("detection_classes").get().asInstanceOf[TFloat32]
    val numDetections = outputTensorMap.get("num_detections").get().asInstanceOf[TFloat32]
    val detectionScores = outputTensorMap.get("detection_scores").get()
    try {
      //              try {
      val numDetects = numDetections.getFloat(0).asInstanceOf[Int]
      logger.info("Num:" + numDetects)
      val detectedClassLabels = detectionClasses.get(0).streamOfObjects()
        .map(a => a.toInt)
        .toList
//
//     val b =  detectedClassLabels
//       .map(a => a.toInt)
//       //.map(a => if cocoLabels.contains(a) then cocoLabels.get(a) else "aa")
//        .toList
//
      detectedClassLabels.forEach(a => logger.info("Index " +a))



      //                if (numDetects > 0) {
      //                  val boxArray = new util.ArrayList[FloatNdArray]()
      //                  //TODO tf.image.combinedNonMaxSuppression
      //                  for (n <- 0 until numDetects) {
      //                    //put probability and position in outputMap
      //                    val detectionScore = detectionScores.getFloat(0, n)
      //                    //only include those classes with detection score greater than 0.3f
      //                    if (detectionScore > 0.3f) boxArray.add(detectionBoxes.get(0, n))
      //                  }
      //              }
    } finally {
      if (detectionClasses != null) detectionClasses.close()
      if (detectionScores != null) detectionScores.close()
      if (numDetections != null) numDetections.close()
      if (reshapeTensor != null) reshapeTensor.close()
    }

    if (s != null) s.close()
    if (g != null) g.close()
    logger.info("ENDE")
  }


}
