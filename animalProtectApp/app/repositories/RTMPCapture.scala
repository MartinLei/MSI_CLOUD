package repositories

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import org.bytedeco.opencv.opencv_videoio.VideoCapture
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.global.opencv_imgcodecs.*

import scala.concurrent.{ExecutionContextExecutor, Future}
import java.nio.file.{Files, Paths}
import org.bytedeco.opencv.global.opencv_core.*
import org.bytedeco.javacv.FFmpegFrameGrabber

import java.io.File
import javax.imageio.ImageIO
//import org.bytedeco.javacpp.avcodec
//import org.bytedeco.javacpp.opencv_core.Buffer
//import org.bytedeco.javacpp.opencv_core.IplImage
import org.bytedeco.javacv.CanvasFrame
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FrameGrabber
import org.bytedeco.javacv.*

object RTMPCapture {



  def main(args: Array[String]): Unit = {
//    val rtmpStreamURL = "rtmp://localhost/live"
//     val myThread = new MyThread(rtmpStreamURL)
//
//    myThread.run()
//
//    myThread.wait()
    old()
  }

    def old() = {
      // Replace this with your RTMP stream URL
      val rtmpStreamURL = "rtmp://localhost/live"
      
      val grabber = new FFmpegFrameGrabber(rtmpStreamURL)
      println("Init")
      var frame = new Frame()

      println("Start")
      grabber.start()
      println("Grab")
      frame = grabber.grabImage
      println("Stop")
      grabber.stop()
      println("release")
      grabber.release()

      val currentDir = new File(System.getProperty("user.dir"))
      println(s"Save Path ${currentDir.getAbsolutePath}")
      val outputFile = new File(currentDir, "output.png") // Save as output.png in the current directory

      println("convert")
      val converter = new Java2DFrameConverter()
      val image = converter.getBufferedImage(frame)
      println("save image")
      ImageIO.write(image, "png", outputFile)
    }
}

class MyThread(rtmpStreamURL: String) extends Runnable {

  def run(): Unit = {
    val grabber = new FFmpegFrameGrabber(rtmpStreamURL)
    println("Init")

    val currentDir = new File(System.getProperty("user.dir") + "/output")
    println(s"Save Path ${currentDir.getAbsolutePath}")
    val converter = new Java2DFrameConverter()

    try {
      println("Start")
      grabber.start()

      for (i <- 1 to 5) {
        println("Grab")
        val frame = grabber.grabKeyFrame()

        println("convert")
        val image = converter.getBufferedImage(frame)
        println("save image")
        val outputFile = new File(currentDir, s"output_${i}.png")
        ImageIO.write(image, "png", outputFile)

        Thread.sleep(10000)
      }

    }finally {
      println("Stop")
      grabber.stop()
      println("release")
      grabber.release()
    }
  }

}
