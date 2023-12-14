package utils

import java.io.ByteArrayOutputStream
import java.nio.file.Path
import javax.imageio.ImageIO

object ImageHelper:

  def readImageFromPath(imagePath: Path, contentType: String): Array[Byte] =
    val imageFile = imagePath.toFile
    val image = ImageIO.read(imageFile)
    val outputStream = new ByteArrayOutputStream()

    val imageType = getImageFormat(contentType)
    ImageIO.write(image, imageType, outputStream)

    outputStream.toByteArray

  private def getImageFormat(fullType: String): String =
    val parts = fullType.split("image/")
    if parts.length == 2 then parts(1) else "-"
