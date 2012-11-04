package utils

import play.api.Logger
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Writes._

import scala.collection.JavaConversions._

import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import org.imgscalr.Scalr
import org.imgscalr.Scalr.Method

import com.drew.metadata.Metadata
import com.drew.imaging.ImageMetadataReader

import org.openimaj.image.ImageUtilities
import org.openimaj.image.processing.face.detection.DetectedFace
import org.openimaj.image.processing.face.detection.HaarCascadeDetector

object ImageUtils {

  implicit val detectedFaceWrites = (
    (__ \ "x").write[Int] ~
    (__ \ "y").write[Int] ~
    (__ \ "width").write[Int] ~
    (__ \ "height").write[Int]
  )({face: DetectedFace =>
    (face.getBounds.x.toInt, face.getBounds.y.toInt, face.getBounds.width.toInt, face.getBounds.height.toInt)
  })

  def faceDetection(image: File, minSize: Int = 80) = {
    val img = ImageUtilities.readF(image)
    (new HaarCascadeDetector(minSize)).detectFaces(img).toList
  }

  def jsonFaceDetection(image: File) = Json.toJson(faceDetection(image))

  def extractMetadata(image: File) = {
    val metadata = ImageMetadataReader.readMetadata(image)
    metadata.getDirectories().toList.flatMap(directory =>
      directory.getTags.toList.map(tag =>
        tag.toString
      )
    ).mkString("\n")
  }

  def resize(image: File, x: Int): BufferedImage = {
    Scalr.resize(ImageIO.read(image), Method.ULTRA_QUALITY, x)
  }

  def resize(image: File, out: File, x: Int): Boolean = {
    ImageIO.write(resize(image, x), getExtension(image), out)
  }

  def getExtension(file: File) = {
    file.getName.substring(file.getName.lastIndexOf('.') + 1)
  }

}

