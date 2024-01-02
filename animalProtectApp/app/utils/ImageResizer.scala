package utils

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import java.io.File
import java.nio.file.Path

object ImageResizer:
  def resize(originalImage: BufferedImage): BufferedImage =
    // Calculate the target size in bytes (e.g., 1MB)
    val targetSizeBytes: Long = 1 * 1024 * 1024 // 1MB

    // Get the original image dimensions
    val originalWidth: Int = originalImage.getWidth
    val originalHeight: Int = originalImage.getHeight

    // Compress the image until it fits within the target size
    var compressedImage: BufferedImage = originalImage
    val outputStream: ByteArrayOutputStream = new ByteArrayOutputStream()
    try
      // Reduce image size while maintaining aspect ratio until it fits within the target size
      while outputStream
          .size() > targetSizeBytes || compressedImage.getWidth > originalWidth / 2 || compressedImage.getHeight > originalHeight / 2
      do
        val newWidth: Int = Math.max(compressedImage.getWidth / 2, 1)
        val newHeight: Int = Math.max(compressedImage.getHeight / 2, 1)

        val resizedImage: BufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
        resizedImage.getGraphics.drawImage(
          compressedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH),
          0,
          0,
          null
        )

        compressedImage = resizedImage

        outputStream.reset()
        ImageIO.write(resizedImage, "jpg", outputStream)
    catch case e: Exception => e.printStackTrace()

    compressedImage

  def resize(path: Path): Path =
    val bufferedImage: BufferedImage = loadImage(path)
    val resizedBuffedImage = resize(bufferedImage)

    saveImage(resizedBuffedImage, path)
    path

  private def saveImage(image: BufferedImage, imagePath: Path): Unit =
    val imageFile = imagePath.toFile
    val imageName = imageFile.getName
    val imageExtension = imageName.substring(imageName.lastIndexOf('.') + 1)

    try ImageIO.write(image, imageExtension, imageFile)
    catch
      case e: Exception =>
        e.printStackTrace()
  private def loadImage(imagePath: Path): BufferedImage =
    try ImageIO.read(imagePath.toFile)
    catch
      case e: Exception =>
        e.printStackTrace()
        null
