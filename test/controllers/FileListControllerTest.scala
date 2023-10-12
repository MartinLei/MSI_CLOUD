package controllers

import models.{FileItem, FileItemsDto}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.*
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import play.api.test.*
import play.api.test.Helpers.*
import service.FileListService
class FileListControllerTest extends PlaySpec with GuiceOneAppPerTest with Injecting with MockitoSugar:
  val fileListServiceMock: FileListService = mock[FileListService]
  val controller = new FileListController(fileListServiceMock, stubControllerComponents())


  "/files GET" should {

    "get all given files" in {
      // setup
      val expectedFiles = FileItemsDto(List(FileItem(1, "TestElement", "daten")))
      when(fileListServiceMock.getAllItemMetadata()).thenReturn(expectedFiles)

      // execute
      val result = controller.getAll.apply(FakeRequest(GET, "/files"))
  
      // verify
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(expectedFiles)
    }
    
  }
