package controllers

import models.{FileItem, FileItemDto, FileItemsDto}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.*
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.libs.json.Json
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.api.test.*
import play.api.test.Helpers.*
import repositories.GoogleBucketRepository
import service.FileListService

import scala.concurrent.Future
class FileListControllerTest extends PlaySpec with GuiceOneAppPerTest with Injecting with MockitoSugar:
  val fileListServiceMock: FileListService = mock[FileListService]
  val googleBucketRepositoryMock: GoogleBucketRepository = mock[GoogleBucketRepository]
  val sut = new FileListController(stubControllerComponents(), fileListServiceMock, googleBucketRepositoryMock)

  "/files GET" should {

    "get all given files" in {
      // setup
      val expectedFiles = FileItemsDto(List(FileItemDto("1", "itemName", "fileName", "contentType")))
      when(fileListServiceMock.getAllItemMetadata).thenReturn(Future.successful(expectedFiles))

      // execute
      val result = sut.getAllItemMetadata().apply(FakeRequest(GET, "/files"))

      // verify
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(expectedFiles)
    }
  }

  "/search GET" should {

    "get all given files with given name" in {
      // setup
      val expectedFiles = FileItemsDto(List(FileItemDto("1", "itemName", "fileName", "contentType")))
      when(fileListServiceMock.search("itemName")).thenReturn(Future.successful(expectedFiles))

      // execute
      val result = sut.search("itemName").apply(FakeRequest(GET, "/search"))

      // verify

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(expectedFiles)
    }
  }

  "/download/:id GET" should {

    "download given file" in {
      // setup
      val expectedFile = FileItem("1", "itemName", "fileName", "text/plain", "bucketItemId")
      when(fileListServiceMock.getItem("1")).thenReturn(Future.successful(Some(expectedFile)))
      val fileData = Array[Byte](1)
      when(googleBucketRepositoryMock.download(any())).thenReturn(fileData)
      // execute
      val result = sut.download("1").apply(FakeRequest(GET, "/download/1"))

      // verify
      status(result) mustBe OK
      contentType(result) mustBe Some(expectedFile.contentType)
      contentAsBytes(result) mustEqual fileData
      headers(result) mustBe Map("Content-Disposition" -> s"attachment; filename=${expectedFile.fileName}")
    }

    "404 if id is not found" in {
      // setup
      when(fileListServiceMock.getItem("1")).thenReturn(Future.successful(None))

      // execute
      val result = sut.download("1").apply(FakeRequest(GET, "/download/1"))

      // verify
      status(result) mustBe NOT_FOUND
    }
  }

  /** TODO not working!
    */
  "/upload POST" should {

    "upload file" in {

      // setup
      val expectedItemName = "itemName"
      val expectedFile = SingletonTemporaryFileCreator.create("tmp", ".txt")
      val files = Seq[FilePart[TemporaryFile]](FilePart("file", "UploadServiceSpec.scala", None, expectedFile))

      val file = FilePart("upload", "hello.txt", Option("text/plain"), expectedFile)
      val formData = MultipartFormData(dataParts = Map("" -> Seq("dummydata")), files = Seq(file), badParts = Seq())

      // execute
      val result = sut
        .upload(expectedItemName)
        .apply(
          FakeRequest(POST, "/upload")
            .withMultipartFormDataBody(formData)
        )

      // verify
      // status(result) mustBe OK
    }
  }
