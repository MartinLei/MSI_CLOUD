package repositories

import akka.actor.Status.Success
import com.google.api.core.ApiFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.*
import com.google.firebase.FirebaseOptions
import models.FileItem
import play.api.Configuration

import java.util.concurrent.{ExecutionException, Executor}
import java.io.FileInputStream
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Try}

class GoogleFireStoreRepository @Inject() (configuration: Configuration)(using ec: ExecutionContext):

  private val projectId: String = configuration.get[String]("google.firestore.projectId")
  private val collectionId: String = configuration.get[String]("google.firestore.collectionId")
  private val credentialsFilePath: String = configuration.get[String]("google.bucket.credentialsFilePath")

  private val credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFilePath))

  private val firestoreOptions = FirestoreOptions.getDefaultInstance.toBuilder
    .setProjectId(projectId)
    .setCredentials(credentials)
    .build

  private val db = firestoreOptions.getService

//  def test =
//    val firestoreOptions = FirestoreOptions.getDefaultInstance.toBuilder
//      .setProjectId(projectId)
//      .setCredentials(GoogleCredentials.getApplicationDefault)
//      .build
//
//    val db = firestoreOptions.getService
//
//    db.collection("cities").document("new-city-id").set(data)
//    val future = db.collection("cities").document("LA").set(city)
//    val addedDocRef = db.collection("cities").add(data)
//
//    System.out.println("Added document with ID: " + addedDocRef.get.getId)

  def save(fileItem: FileItem): Future[DocumentReference] =
    val apiFuture: ApiFuture[DocumentReference] = db.collection(collectionId).add(fileItem)
    val documentReference = apiFuture.get()
    Future(documentReference)
    //apiFuture.asScala
