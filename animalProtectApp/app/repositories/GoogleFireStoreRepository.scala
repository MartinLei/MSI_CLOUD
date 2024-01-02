package repositories

import com.google.api.core.ApiFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.*
import com.typesafe.scalalogging.LazyLogging
import models.FileItem
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import utils.asScala

import java.io.FileInputStream
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.jdk.CollectionConverters.*
import scala.language.postfixOps
import scala.util.{Failure, Success}

class GoogleFireStoreRepository @Inject() (configuration: Configuration, lifecycle: ApplicationLifecycle)(using
    ec: ExecutionContext
) extends LazyLogging:

  lifecycle.addStopHook { () =>
    val promise = Promise[Unit]()
    try
      db.close()
      promise.success(())
    catch
      case e: Throwable =>
        promise.failure(e)
    promise.future
  }

  private val googleProjectId: String = configuration.get[String]("google.firestore.projectId")
  private val googleCredentialsFilePath: String = configuration.get[String]("google.firestore.credentialsFilePath")

  private val credentials = GoogleCredentials.fromStream(new FileInputStream(googleCredentialsFilePath))

  private val db = FirestoreOptions
    .newBuilder()
    .setProjectId(googleProjectId)
    .setCredentials(credentials)
    .build
    .getService

  def save(projectId: String, fileItem: FileItem): Future[Int] =
    logger.info(s"Upload metadate to firestore.  [projectId: '$projectId']")
    db.collection(projectId)
      .add(fileItem)
      .asScala
      .map(documentReference => documentReference.getId.toInt)

  def findAll(projectId: String): Future[Seq[FileItem]] =
    db.collection(projectId)
      .get()
      .asScala
      .map(querySnapshot => querySnapshot.getDocuments.asScala.toSeq)
      .map(seqQuerySnapshot => seqQuerySnapshot.map(toFileItem))

  def findById(projectId: String, documentId: String): Future[Option[FileItem]] =
    db.collection(projectId)
      .document(documentId)
      .get()
      .asScala
      .map(queryDocumentSnapshot => Some(queryDocumentSnapshot.toObject(classOf[FileItem])))

  def search(projectId: String, fileName: String): Future[Seq[FileItem]] =
    db.collection(projectId)
      .where(Filter.or(Filter.equalTo("itemName", fileName), Filter.equalTo("fileName", fileName)))
      .get()
      .asScala
      .map(querySnapshot => querySnapshot.getDocuments.asScala.toSeq)
      .map(seqQuerySnapshot => seqQuerySnapshot.map(toFileItem))

  def delete(projectId: String, documentId: String): Future[String] =
    db.collection(projectId)
      .document(documentId)
      .delete()
      .asScala
      .map(w => documentId)

  /** Only used for debugging purpose. For deleting all files in the bucket.
    */
  def deleteAll(projectId: String): Unit =
    val batch = db.batch()
    val documents = db.collection(projectId).get().get().asScala
    documents
      .foreach(document => batch.delete(document.getReference))

    if documents.isEmpty then
      logger.info(s"Firestore is empty")
      return

    batch.commit()
    logger.info(s"Delete all ${documents.size} entries in fileStore of collection $projectId")

  def toFileItem: QueryDocumentSnapshot => FileItem =
    queryDocumentSnapshot =>
      val fileItem: FileItem = queryDocumentSnapshot.toObject(classOf[FileItem])
      FileItem.apply(queryDocumentSnapshot.getId, fileItem)
