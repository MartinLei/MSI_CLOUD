package repositories

import com.google.api.core.ApiFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.*
import com.google.cloud.secretmanager.v1.{SecretManagerServiceClient, SecretVersionName}
import com.typesafe.scalalogging.LazyLogging
import models.FileItem
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import utils.asScala

import java.io.{ByteArrayInputStream, FileInputStream}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.jdk.CollectionConverters.*
import scala.language.postfixOps
import scala.util.{Failure, Success}

class GoogleFireStoreRepository @Inject()(
                                           configuration: Configuration,
                                           lifecycle: ApplicationLifecycle)(using ec: ExecutionContext)
  extends LazyLogging:
  
  lifecycle.addStopHook(() => {
    val promise = Promise[Unit]()
    try {
      db.close()
      promise.success(())
    } catch {
      case e: Throwable =>
        promise.failure(e)
    }
    promise.future
  })

  private val projectId: String = configuration.get[String]("google.firestore.projectId")
  private val collectionId: String = configuration.get[String]("google.firestore.collectionId")
  private val secretId: String = configuration.get[String]("google.firestore.secretId")
  private val versionId: String = configuration.get[String]("google.firestore.versionId")

  private val secretVersionName = SecretVersionName.of(projectId, secretId, versionId)
  private val client = SecretManagerServiceClient.create()
  private val response = client.accessSecretVersion(secretVersionName)
  private val payloadBytes = response.getPayload.getData.toByteArray
  private val credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(payloadBytes))

  private val db = FirestoreOptions
    .newBuilder()
    .setProjectId(projectId)
    .setCredentials(credentials)
    .build
    .getService

  def save(fileItem: FileItem): Future[Int] =
    db.collection(collectionId)
      .add(fileItem)
      .asScala
      .map(documentReference => documentReference.getId.toInt)

  def findAll: Future[Seq[FileItem]] =
    db.collection(collectionId)
      .get()
      .asScala
      .map(querySnapshot => querySnapshot.getDocuments.asScala.toSeq)
      .map(seqQuerySnapshot => seqQuerySnapshot.map(toFileItem))

  def findById(documentId: String): Future[Option[FileItem]] =
    db.collection(collectionId)
      .document(documentId)
      .get()
      .asScala
      .map(queryDocumentSnapshot => Some(queryDocumentSnapshot.toObject(classOf[FileItem])))

  def search(name: String): Future[Seq[FileItem]] =
    db.collection(collectionId)
      .where(Filter.or(Filter.equalTo("itemName", name), Filter.equalTo("fielName", name)))
      .get()
      .asScala
      .map(querySnapshot => querySnapshot.getDocuments.asScala.toSeq)
      .map(seqQuerySnapshot => seqQuerySnapshot.map(toFileItem))

  def delete(documentId: String): Future[String] =
    db.collection(collectionId)
      .document(documentId)
      .delete()
      .asScala
      .map(w => documentId)

  /** Only used for debugging purpose. For deleting all files in the bucket.
   */
  def deleteAll(): Unit =
    val batch = db.batch()
    val documents = db.collection(collectionId).get().get().asScala
    documents
      .foreach(document => batch.delete(document.getReference))

    if documents.isEmpty then
      logger.info(s"Firestore is empty")
      return

    batch.commit()
    logger.info(s"Delete all ${documents.size} entries in fileStore of collection $collectionId")

  def toFileItem: QueryDocumentSnapshot => FileItem =
    queryDocumentSnapshot =>
      val fileItem: FileItem = queryDocumentSnapshot.toObject(classOf[FileItem])
      FileItem.apply(queryDocumentSnapshot.getId, fileItem)
