package controllers

import service.VideoGrabberActor.{GrabNextFrame, Shutdown}

import javax.inject.*
import org.apache.pekko.actor.*
import org.apache.pekko.util.Timeout
import play.api.mvc.*

import scala.concurrent.Future
import scala.concurrent.duration.*
import org.apache.pekko.pattern.ask
import repositories.kafka.KafkaProducerRepository
import service.{ItemService, VideoGrabberActor}

import scala.collection.mutable
@Singleton
class VideoGrabberController @Inject() (system: ActorSystem, cc: ControllerComponents, itemService: ItemService)
    extends AbstractController(cc):

  implicit val timeout: Timeout = 20.seconds
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  private val actors: mutable.Map[String, ActorRef] = mutable.Map.empty

  def startGrabbing(projectId: String, streamingUrl: String): Action[AnyContent] = Action.async {
    actors.get(projectId) match
      case Some(_) =>
        Future.successful(Conflict(s"A video grabber is already active for given project. [projectId: '$projectId']"))
      case None =>
        val videoGrabberActor = system.actorOf(
          VideoGrabberActor(itemService, projectId, streamingUrl),
          "actor_" + projectId
        )
        actors.put(projectId, videoGrabberActor)
        videoGrabberActor ! GrabNextFrame()
        Future.successful(Ok(s"Started video grabber. [projectId: '$projectId', streamingUrl: '$streamingUrl']"))
  }

  def stopGrabbing(projectId: String): Action[AnyContent] = Action.async {
    actors.get(projectId) match
      case Some(actorRef) =>
        actorRef ! Shutdown()
        actors.remove(projectId)
        Future.successful(Ok(s"Video grabber for given projectId was stopped. [projectId: '$projectId']"))
      case None =>
        Future.successful(NotFound(s"No video grabber for given projectId was found. [projectId: '$projectId']"))
  }
