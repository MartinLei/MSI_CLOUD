package repositories

import com.google.api.core.ApiFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.*
import com.typesafe.scalalogging.LazyLogging
import models.FileItem
import play.api.Configuration
import utils.asScala

import java.io.FileInputStream
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class GoogleFireStoreRepository @Inject() (configuration: Configuration)(using ec: ExecutionContext)
    extends LazyLogging:

  private val projectId: String = configuration.get[String]("google.firestore.projectId")
  private val collectionId: String = configuration.get[String]("google.firestore.collectionId")
  private val credentialsFilePath: String = configuration.get[String]("google.firestore.credentialsFilePath")

  private val credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFilePath))

  private val db = FirestoreOptions
    .newBuilder()
    .setProjectId(projectId + "kk")
    .setCredentials(credentials)
    .build
    .getService

  def save(fileItem: FileItem): Future[DocumentReference] =
    val apiFuture: ApiFuture[DocumentReference] = db.collection(collectionId).add(fileItem)
    apiFuture.asScala
