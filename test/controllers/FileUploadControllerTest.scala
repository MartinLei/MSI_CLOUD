package controllers

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.api.test.*
import play.api.test.Helpers.*
import service.FileListService

class FileUploadControllerTest extends PlaySpec with GuiceOneAppPerTest with Injecting with MockitoSugar:
  val fileListServiceMock: FileListService = mock[FileListService]
  val sut = new FileUploadController(stubControllerComponents(), fileListServiceMock)

  /**
   * TODO not working!
   */
  "/upload POST" should {

    "upload file" in {

      // setup
      val expectedItemName = "itemName"
      val expectedFile = SingletonTemporaryFileCreator.create("tmp", ".txt")
      val files = Seq[FilePart[TemporaryFile]](FilePart("file", "UploadServiceSpec.scala", None, expectedFile))


      val file = FilePart("upload", "hello.txt", Option("text/plain"), expectedFile)
      val formData = MultipartFormData(
        dataParts = Map("" -> Seq("dummydata")),
        files = Seq(file),
        badParts = Seq())

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
