package service

import models.{FileItem, FileItems}
import repositories.FileListRepository

import javax.inject.Inject

class FileListService @Inject()(val fileListRepository: FileListRepository) {

  def getAll() : FileItems = FileItems(fileListRepository.findAll());
}
