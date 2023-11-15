package service

import akka.util.Timeout
import models.{FileItem, FileItemDto, FileItemsDto}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Helpers.await
import play.api.test.Injecting
import repositories.{GoogleBucketRepository, GoogleFireStoreRepository}

import scala.concurrent.Future
import scala.concurrent.duration.*

class FileListServiceTest extends PlaySpec with GuiceOneAppPerTest with Injecting with MockitoSugar:
  given defaultAwaitTimeout: Timeout = 2.seconds

  val googleBucketRepositoryMock: GoogleBucketRepository = mock[GoogleBucketRepository]
  val googleFireStoreRepositoryMock: GoogleFireStoreRepository = mock[GoogleFireStoreRepository]
  val sut = new FileListService(googleBucketRepositoryMock, googleFireStoreRepositoryMock)

  "getAllItemMetadata" should {
    "find all items" in {
      // setup
      val file1 = FileItem("1", "itemName1", "fileName1", "contentType", "bucketItemId")
      val file2 = FileItem("2", "itemName2", "fileName2", "contentType", "bucketItemId")
      when(googleFireStoreRepositoryMock.findAll).thenReturn(Future.successful(Seq(file1, file2)))

      // execute
      val result = await(sut.getAllItemMetadata)

      // verify
      var expected = FileItemsDto(Seq(FileItemDto.from(file1), FileItemDto.from(file2)))
      result shouldBe expected
    }
  }

  "search" should {

    "find file with same itemName or fileName" in {
      // setup
      val file1 = FileItem("1", "itemName1", "fileName1", "contentType", "bucketItemId")
      when(googleFireStoreRepositoryMock.search(any())).thenReturn(Future.successful(Seq(file1)))

      // execute
      val result = await(sut.search("itemName1"))

      // verify
      var expected = FileItemsDto(Seq(FileItemDto.from(file1)))
      result shouldBe expected
    }
  }
