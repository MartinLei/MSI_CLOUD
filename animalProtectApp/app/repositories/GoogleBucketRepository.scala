package repositories

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.{BlobId, BlobInfo, Storage, StorageOptions}
import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import java.io.FileInputStream
import java.nio.file.Path
import scala.concurrent.Promise
import scala.jdk.CollectionConverters.*

class GoogleBucketRepository @Inject() (configuration: Configuration, lifecycle: ApplicationLifecycle)
    extends LazyLogging:

  lifecycle.addStopHook { () =>
    val promise = Promise[Unit]()
    try
      storage.close()
      promise.success(())
    catch
      case e: Throwable =>
        promise.failure(e)
    promise.future
  }

  private val googleProjectId: String = configuration.get[String]("google.bucket.projectId")
  private val googleBucketName: String = configuration.get[String]("google.bucket.bucketName")
  private val googleCredentialsFilePath: String = configuration.get[String]("google.bucket.credentialsFilePath")

  private val credentials = GoogleCredentials.fromStream(new FileInputStream(googleCredentialsFilePath))

  private val storage = StorageOptions
    .newBuilder()
    .setProjectId(googleProjectId)
    .setCredentials(credentials)
    .build()
    .getService

  private def getBlobId(projectId: String, imageId: String): BlobId =
    val fullIdentifier = projectId + "/" + imageId
    BlobId.of(googleBucketName, fullIdentifier)

  def upload(projectId: String, imageId: String, path: Path): Unit =

    val blobId = getBlobId(projectId, imageId)
    val blobInfo = BlobInfo.newBuilder(blobId).build()

    // Optional: set a generation-match precondition to avoid potential race
    // conditions and data corruptions. The request returns a 412 error if the
    // preconditions are not met.
    val precondition = Option(storage.get(googleBucketName, imageId)) match
      case Some(_) =>
        // If the destination already exists in your bucket, instead set a generation-match
        // precondition. This will cause the request to fail if the existing object's generation
        // changes before the request runs.
        Storage.BlobWriteOption.generationMatch(storage.get(googleBucketName, imageId).getGeneration)
      case None =>
        // For a target object that does not yet exist, set the DoesNotExist precondition.
        // This will cause the request to fail if the object is created before the request runs.
        Storage.BlobWriteOption.doesNotExist()

    storage.createFrom(blobInfo, path, precondition)
    logger.info(s"Upload file to bucket.  [projectId: '$projectId', imageId: '$imageId']")

  def download(projectId: String, imageId: String): Array[Byte] =
    val blobId = getBlobId(projectId, imageId)
    storage.readAllBytes(blobId)

  def delete(projectId: String, imageId: String): Boolean =
    val blobId = getBlobId(projectId, imageId)
    storage.delete(blobId)

  /** Only used for debugging purpose. For deleting all files in the bucket.
    */
  def deleteAll(projectId: String): Unit =
    val batch = storage.batch
    val blobs = storage
      .list(googleBucketName, Storage.BlobListOption.prefix(projectId))
      .iterateAll
      .asScala
    
    if blobs.isEmpty then
      logger.info(s"Bucket is empty")
      return

    for blob <- blobs do batch.delete(blob.getBlobId)
    batch.submit()
    logger.info(s"Delete all files in bucket with prefix name $projectId")
