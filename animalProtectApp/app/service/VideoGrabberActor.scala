package service

import com.typesafe.scalalogging.LazyLogging
import org.apache.pekko.actor.*
import org.bytedeco.javacv.*
import repositories.kafka.KafkaProducerRepository
import service.VideoGrabberActor.{GrabNextFrame, RetryReconnect, Shutdown}
import utils.ImageResizer

import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.{Duration, DurationInt}
import scala.language.postfixOps

object VideoGrabberActor:
  def apply(fileListService: FileListService, projectId: String, streamUrl: String): Props = Props(
    new VideoGrabberActor(fileListService, projectId, streamUrl)
  )

  case class Start()
  case class GrabNextFrame()

  case class Shutdown()
  case class RetryReconnect(retryAttempt: Int)

final class VideoGrabberActor(fileListService: FileListService, projectId: String, streamUrl: String)
    extends Actor
    with LazyLogging:
  implicit val ec: ExecutionContextExecutor = context.dispatcher

  private val grabber: FFmpegFrameGrabber = FFmpegFrameGrabber.createDefault(streamUrl)
  FFmpegFrameGrabber.tryLoad()

  var index: Int = 0 // TODO remove
  var shutdown: Boolean = false
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
      logger.info(
        s"Retry reconnect grabber. " +
          s"[projectId: '$projectId', url: '$streamUrl', retryAttempt: '$retryAttempt']"
      )
      run(streamUrl, retryAttempt + 1)
  }

  def run(rtmpStreamURL: String, retryAttempt: Int): Unit =
    if shutdown then
      logger.info(s"IgnoreMessage. [projectId: '$projectId', url: '$streamUrl']")
      return

    var bufferedImage: BufferedImage = null
    try
      grabber.restart()

      bufferedImage = grabFrame(grabber, retryAttempt)
    catch
      case e: Exception =>
        val waitSeconds: Int = Math.pow(2, retryAttempt).toInt * 10
        logger.info(s"Connection refused. wait $waitSeconds sec. [projectId: '$projectId', url: '$streamUrl']")
        val duration = Duration(waitSeconds, TimeUnit.SECONDS)
        context.system.scheduler.scheduleOnce(duration, self, RetryReconnect(retryAttempt))
        return

    if bufferedImage == null then
      logger.info(s"No image received -> reconnect. [projectId: '$projectId', url: '$streamUrl']")
      context.system.scheduler.scheduleOnce(1.second, self, RetryReconnect(retryAttempt))
      return

    index = index + 1
    val filename = s"output_${index}_${Calendar.getInstance().getTime}.png"
    val reducedBufferedImage = ImageResizer.resize(bufferedImage)
    val tempFile = Files.createTempFile(filename, ".png")
    ImageIO.write(reducedBufferedImage, "png", tempFile.toFile)
    fileListService.addFileItem(projectId, tempFile)
    context.system.scheduler.scheduleOnce(1.second, self, GrabNextFrame())

  private def grabFrame(grabber: FFmpegFrameGrabber, retryAttempt: Int): BufferedImage =
    logger.info(s"Skip frames ca. 10sec. [projectId: '$projectId', url: '$streamUrl']")
    for i <- 1 to 300 do
      grabber.grabImage()
      Thread.sleep(10)
    logger.info(s"Grab frame. [projectId: '$projectId', url: '$streamUrl']")
    val frame = grabber.grabImage()

    val converter = new Java2DFrameConverter()
    converter.getBufferedImage(frame)
