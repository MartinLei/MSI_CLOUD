package service

import models.FileItems
import repositories.FileListRepository

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileListService @Inject() (val fileListRepository: FileListRepository):

  //def getAll(): FileItems = FileItems(fileListRepository.findAll());

  def getAll(): Future[FileItems] = {
    fileListRepository.findAllDB
      .map(a => FileItems(a))
  }
