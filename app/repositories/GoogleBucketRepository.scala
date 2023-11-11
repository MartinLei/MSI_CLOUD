package repositories

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.{BlobId, BlobInfo, Storage, StorageOptions}
import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging
import play.api.Configuration

import java.io.FileInputStream
import java.nio.file.Path
import scala.jdk.CollectionConverters.*

class GoogleBucketRepository @Inject() (configuration: Configuration) extends LazyLogging:
  private val projectId: String = configuration.get[String]("google.bucket.projectId")
  private val bucketName: String = configuration.get[String]("google.bucket.bucketName")
  private val credentialsFilePath: String = configuration.get[String]("google.bucket.credentialsFilePath")

  private val credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFilePath))

  private val storage = StorageOptions
    .newBuilder()
    .setProjectId(projectId)
    .setCredentials(credentials)
    .build()
    .getService

  private val prefixFileName: String = "file_"
  def upload(filePath: Path, fileName: String): Unit =

    val blobId = BlobId.of(bucketName, prefixFileName + fileName)
    val blobInfo = BlobInfo.newBuilder(blobId).build()

    // Optional: set a generation-match precondition to avoid potential race
    // conditions and data corruptions. The request returns a 412 error if the
    // preconditions are not met.
    val precondition = Option(storage.get(bucketName, fileName)) match
      case Some(_) =>
        // If the destination already exists in your bucket, instead set a generation-match
        // precondition. This will cause the request to fail if the existing object's generation
        // changes before the request runs.
        Storage.BlobWriteOption.generationMatch(storage.get(bucketName, fileName).getGeneration)
      case None =>
        // For a target object that does not yet exist, set the DoesNotExist precondition.
        // This will cause the request to fail if the object is created before the request runs.
        Storage.BlobWriteOption.doesNotExist()

    storage.createFrom(blobInfo, filePath, precondition)
    logger.info(s"Upload file $filePath to bucket as $fileName")

  def download(bucketItemId: String): Array[Byte] =
    val blobId = BlobId.of(bucketName, prefixFileName + bucketItemId)
    storage.readAllBytes(blobId)

  def delete(fileName: String): Boolean =
    val blobId = BlobId.of(bucketName, prefixFileName + fileName)
    storage.delete(blobId)

  /** Only used for debugging purpose. For deleting all files in the bucket.
    */
  def deleteAll(): Unit =
    val batch = storage.batch
    val blobs = storage
      .list(bucketName, Storage.BlobListOption.currentDirectory, Storage.BlobListOption.prefix(prefixFileName))
      .iterateAll
      .asScala

    if blobs.isEmpty then
      logger.info(s"Bucket is empty")
      return

    for blob <- blobs do batch.delete(blob.getBlobId)
    batch.submit()
    logger.info(s"Delete all files in bucket with prefix name $prefixFileName")
