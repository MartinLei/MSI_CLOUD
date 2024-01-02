package repositories

import com.google.api.core.ApiFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.*
import com.typesafe.scalalogging.LazyLogging
import models.Item
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

  def save(projectId: String, fileItem: Item): Future[Int] =
    logger.info(s"Upload metadate to firestore.  [projectId: '$projectId']")
    db.collection(projectId)
      .add(fileItem)
      .asScala
      .map(documentReference => documentReference.getId.toInt)

  def findAll(projectId: String): Future[Seq[Item]] =
    db.collection(projectId)
      .get()
      .asScala
      .map(querySnapshot => querySnapshot.getDocuments.asScala.toSeq)
      .map(seqQuerySnapshot => seqQuerySnapshot.map(toFileItem))

  def findById(projectId: String, itemId: String): Future[Option[Item]] =
    db.collection(projectId)
      .document(itemId)
      .get()
      .asScala
      .map(queryDocumentSnapshot => Some(queryDocumentSnapshot.toObject(classOf[Item])))

  def search(projectId: String, fileName: String): Future[Seq[Item]] =
    db.collection(projectId)
      .where(Filter.or(Filter.equalTo("itemName", fileName), Filter.equalTo("fileName", fileName)))
      .get()
      .asScala
      .map(querySnapshot => querySnapshot.getDocuments.asScala.toSeq)
      .map(seqQuerySnapshot => seqQuerySnapshot.map(toFileItem))

  def delete(projectId: String, itemId: String): Future[String] =
    db.collection(projectId)
      .document(itemId)
      .delete()
      .asScala
      .map(w => itemId)

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

  def toFileItem: QueryDocumentSnapshot => Item =
    queryDocumentSnapshot =>
      val fileItem: Item = queryDocumentSnapshot.toObject(classOf[Item])
      Item.apply(queryDocumentSnapshot.getId, fileItem)
