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
import scala.jdk.CollectionConverters.*
import scala.language.postfixOps

class GoogleFireStoreRepository @Inject() (configuration: Configuration)(using ec: ExecutionContext)
    extends LazyLogging:

  private val projectId: String = configuration.get[String]("google.firestore.projectId")
  private val collectionId: String = configuration.get[String]("google.firestore.collectionId")
  private val credentialsFilePath: String = configuration.get[String]("google.firestore.credentialsFilePath")

  private val credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFilePath))

  private val db = FirestoreOptions
    .newBuilder()
    .setProjectId(projectId)
    .setCredentials(credentials)
    .build
    .getService

  def save(fileItem: FileItem): Future[Int] =
    db.collection(collectionId).add(fileItem).asScala
      .map(documentReference=> documentReference.getId.toInt)


  def findAll: Future[Seq[FileItem]] =
     db.collection(collectionId).get().asScala
      .map(querySnapshot => querySnapshot.getDocuments.asScala.toSeq)
      .map(seqQuerySnapshot => seqQuerySnapshot
        .map(toFileItem)
      )

  def findById(documentId: String): Future[Option[FileItem]] =
    db.collection(collectionId).document(documentId).get().asScala
      .map(queryDocumentSnapshot => Some(queryDocumentSnapshot.toObject(classOf[FileItem])))


  def toFileItem: QueryDocumentSnapshot => FileItem =
    queryDocumentSnapshot => {
      val fileItem : FileItem = queryDocumentSnapshot.toObject(classOf[FileItem])
      FileItem.apply(queryDocumentSnapshot.getId, fileItem)
    }