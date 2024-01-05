package repositories

import com.google.api.core.ApiFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.*
import com.typesafe.scalalogging.LazyLogging
import models.Item
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import utils.asScala
import io.circe
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
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

  def save(projectId: String, item: Item): Future[Item] =
    db.collection(projectId)
      .add(item)
      .asScala
      .map(documentReference => documentReference.getId)
      .map(itemId => Item(itemId, item))

  def updateField_detectedObjectSerialized(
      projectId: String,
      documentId: String,
      detectedObjectSerialized: String
  ): Future[WriteResult] =
    db.collection(projectId)
      .document(documentId)
      .update(Item.paramName_detectedObjectsSerialized, detectedObjectSerialized)
      .asScala

  def findByBucketId(projectId: String, bucketId: String): Future[Option[Item]] =
    db.collection(projectId)
      .whereEqualTo(Item.paramName_bucketId, bucketId)
      .get()
      .asScala
      .map(querySnapshot => querySnapshot.getDocuments.asScala.toSeq)
      .map(seqQuerySnapshot => seqQuerySnapshot.map(toItem).headOption)

  def findAll(projectId: String): Future[Seq[Item]] =
    db.collection(projectId)
      .get()
      .asScala
      .map(querySnapshot => querySnapshot.getDocuments.asScala.toSeq)
      .map(seqQuerySnapshot => seqQuerySnapshot.map(toItem))

  def findById(projectId: String, itemId: String): Future[Option[Item]] =
    db.collection(projectId)
      .document(itemId)
      .get()
      .asScala
      .map(toItem)
      .map(Some(_))

  def delete(projectId: String, itemId: String): Future[String] =
    db.collection(projectId)
      .document(itemId)
      .delete()
      .asScala
      .map(_ => itemId)

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

  private def toItem: DocumentSnapshot => Item =
    documentSnapshot =>
      val fileItem: Item = documentSnapshot.toObject(classOf[Item])
      Item.apply(documentSnapshot.getId, fileItem)
