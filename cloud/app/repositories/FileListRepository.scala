package repositories

import models.FileItem


class FileListRepository:

  /**
   * TODO use real db repository.
   */
  def findAll(): List[FileItem] = List(
      FileItem("Test1", "Datei"),
      FileItem("Test2", "Datei")
    )
