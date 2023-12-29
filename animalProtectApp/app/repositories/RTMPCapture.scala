package repositories

import org.bytedeco.javacv.FFmpegFrameGrabber

import java.io.File
import java.util.Calendar
import javax.imageio.ImageIO
import scala.language.postfixOps
import org.bytedeco.javacv.*

object RTMPCapture:

  def main(args: Array[String]): Unit =
    val rtmpStreamURL = "rtmp://localhost/live"
    val myThread = new MyThread(rtmpStreamURL)

    myThread.run()
    myThread.wait()

class MyThread(rtmpStreamURL: String) extends Runnable:

  def run(): Unit =

    println(s"Init ${Calendar.getInstance().getTime}")

    val currentDir = new File(System.getProperty("user.dir") + "/output")
    println(s"Save Path ${currentDir.getAbsolutePath}")
    val converter = new Java2DFrameConverter()

    var retry = 0
    while retry <= 5 do
      try
        val grabber = FFmpegFrameGrabber.createDefault(rtmpStreamURL)
        FFmpegFrameGrabber.tryLoad()
        println("Start")
        grabber.start()

        retry = 0
        grabFrame(grabber, currentDir, converter)

        println("Stop")
        grabber.stop()
      catch
        case e: Exception =>
          val waitSeconds: Int = Math.pow(2, retry).toInt * 10
          println(s"Connection refused. wait $waitSeconds sec.")
          Thread.sleep(1000 * waitSeconds)
          retry = retry + 1
          if retry == 5 then println("Could not reconnect stop.")
          else println("Retry to connect.. ")

  private def grabFrame(grabber: FFmpegFrameGrabber, currentDir: File, converter: Java2DFrameConverter): Unit =
    for i <- 1 to 5000 do
      println("skip frames ca. 10sec")
      for i <- 1 to 300 do
        grabber.grabImage()
        Thread.sleep(10)
      println("grab frame")
      val frame = grabber.grabImage()

      val image = converter.getBufferedImage(frame)
      if image != null then
        val filename = s"output_${i}_${Calendar.getInstance().getTime}.png"
        println(s"save image $filename")
        val outputFile = new File(currentDir, filename)
        ImageIO.write(image, "png", outputFile)
      else {
        println(s"no image received ${Calendar.getInstance().getTime} -> reconnect")
        grabber.restart()
      }
