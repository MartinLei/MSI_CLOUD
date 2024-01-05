package utils

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO
import java.nio.file.{Files, Path}

object ImageResizer:
  private val maxSize = 800

  def resize(inputPath: Path): Path =
    try
      val originalImage: BufferedImage = ImageIO.read(inputPath.toFile)
      resize(originalImage)
    catch
      case e: IOException =>
        throw new IOException(s"Error resizing image: ${e.getMessage}")

  def resize(originalImage: BufferedImage): Path =
    try

      val resizedImage: BufferedImage = resizeImageWithAspect(originalImage, maxSize)

      // Create a temporary file to store the resized image
      val tempFilePath: Path = Files.createTempFile("resized_image_", ".jpg")

      // Write the resized image to the temporary file
      ImageIO.write(resizedImage, "png", tempFilePath.toFile)
      tempFilePath
    catch
      case e: IOException =>
        throw new IOException(s"Error resizing image: ${e.getMessage}")

  def resizeImageWithAspect(image: BufferedImage, maxSize: Int): BufferedImage =
    val width = image.getWidth
    val height = image.getHeight

    val newWidth: Int = if width >= height then maxSize else (width.toDouble / height.toDouble * maxSize).toInt
    val newHeight: Int = if height >= width then maxSize else (height.toDouble / width.toDouble * maxSize).toInt

    val tmp: Image = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)
    val resizedImage: BufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)

    val g2d = resizedImage.createGraphics()
    g2d.drawImage(tmp, 0, 0, null)
    g2d.dispose()

    resizedImage
