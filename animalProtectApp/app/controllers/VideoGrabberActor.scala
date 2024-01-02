package controllers

import com.typesafe.scalalogging.LazyLogging
import controllers.VideoGrabberActor.{GrabNextFrame, RetryReconnect, Shutdown}
import org.apache.pekko.actor.*
import repositories.RTMPGrabber
import org.bytedeco.javacv.FFmpegFrameGrabber

import java.io.File
import java.util.Calendar
import java.io.File
import java.util.Calendar
import javax.imageio.ImageIO
import scala.language.postfixOps
import org.bytedeco.javacv.*

import concurrent.duration.DurationInt
import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration.{Duration, MILLISECONDS}

object VideoGrabberActor:
  def apply(projectId: String, streamUrl: String): Props = Props(new VideoGrabberActor(projectId, streamUrl))

  case class Start()
  case class GrabNextFrame()

  case class Shutdown()
  case class RetryReconnect(retryAttempt: Int)

final class VideoGrabberActor(projectId: String, streamUrl: String) extends Actor with LazyLogging:
  implicit val ec: ExecutionContextExecutor = context.dispatcher

  val grabber: FFmpegFrameGrabber = FFmpegFrameGrabber.createDefault(streamUrl)
  FFmpegFrameGrabber.tryLoad()

  var index: Int = 0 // TODO remove
  var shutdown : Boolean = false
  def receive: Receive = {
    case GrabNextFrame() =>
      logger.info(s"Grab next frame. [projectId: '$projectId', url: '$streamUrl']")
      run(streamUrl, 0)
    case Shutdown() =>
      logger.info(s"Stop grabbing for given projectId. [projectId: '$projectId', url: '$streamUrl']")
      grabber.stop()
      shutdown = true
      context.stop(self)
    case RetryReconnect(retryAttempt) =>
      logger.info(s"Retry reconnect grabber. " +
        s"[projectId: '$projectId', url: '$streamUrl', retryAttempt: '$retryAttempt']")
      if retryAttempt == 5 then
        logger.info(s"Could not reconnect after $retryAttempt attempts -> stop. " +
          s"[projectId: '$projectId', url: '$streamUrl', retryAttempt: '$retryAttempt']")
      else run(streamUrl, retryAttempt + 1)
  }

  def run(rtmpStreamURL: String, retryAttempt: Int): Unit =
    if (shutdown) {
      logger.info(s"IgnoreMessage. [projectId: '$projectId', url: '$streamUrl']")
      return
    }

    val currentDir = new File(System.getProperty("user.dir") + "/output")
    val converter = new Java2DFrameConverter()

    try
      grabber.restart()

      grabFrame(grabber, currentDir, converter, retryAttempt)
    catch
      case e: Exception =>
        val waitSeconds: Int = Math.pow(2, retryAttempt).toInt * 10
        logger.info(s"Connection refused. wait $waitSeconds sec. [projectId: '$projectId', url: '$streamUrl']")
        val duration = Duration(waitSeconds, TimeUnit.SECONDS)
        context.system.scheduler.scheduleOnce(duration, self, RetryReconnect(retryAttempt))

  private def grabFrame(grabber: FFmpegFrameGrabber,
                        currentDir: File,
                        converter: Java2DFrameConverter,
                        retryAttempt: Int): Unit =
    logger.info(s"Skip frames ca. 10sec. [projectId: '$projectId', url: '$streamUrl']")
    for i <- 1 to 300 do
      grabber.grabImage()
      Thread.sleep(10)
    logger.info(s"Grab frame. [projectId: '$projectId', url: '$streamUrl']")
    val frame = grabber.grabImage()

    val image = converter.getBufferedImage(frame)
    if image != null then
      val filename = s"output_${index}_${Calendar.getInstance().getTime}.png"
      index = index + 1
      logger.info(s"Save image $filename. [projectId:'$projectId', url: '$streamUrl']")
      val outputFile = new File(currentDir, filename)
      ImageIO.write(image, "png", outputFile)
      context.system.scheduler.scheduleOnce(1.second, self, GrabNextFrame())
    else
      logger.info(s"No image received ${Calendar.getInstance().getTime} -> reconnect." +
        s" [projectId: '$projectId', url: '$streamUrl']")
      context.system.scheduler.scheduleOnce(1.second, self, RetryReconnect(retryAttempt))
