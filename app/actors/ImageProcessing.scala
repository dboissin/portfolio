package actors

import akka.actor._
import akka.routing._
import akka.pattern.ask

import play.api.libs.concurrent._
import play.api.Play.current
import play.api.Logger

import java.io.File
import scalax.file._
import scalax.file.PathMatcher._
import utils.ImageUtils
import models.Photo
import models.Person

trait ImportEvent
case class ImportSuccess(path: String) extends ImportEvent
case class ImportError(path: String, cause: String) extends ImportEvent

case class ImportDirectory(path: String, categoriesIds: List[Long], galleriesIds: List[Long])
case class ImportImage(img:File, ownerRootDir: String, categoriesIds: List[Long], galleriesIds: List[Long])

class ImportImageActor extends Actor {

  def receive = {
    case ImportImage(img, ownerRootDirectory, categories, galleries) =>
      val thumb = new File(img.getParent() + File.separator + "thumb_"+ img.getName())
      val normal = new File(img.getParent() + File.separator + "n_"+ img.getName())
      ImageUtils.resize(img, normal, 930)
      ImageUtils.resize(img, thumb, 250)
      val faces = ImageUtils.jsonFaceDetection(normal)
      val meta = ImageUtils.extractMetadata(img)
      val relativePath = img.getAbsolutePath.substring(ownerRootDirectory.length)
      val res = Photo.create(relativePath, meta, faces.toString, categories, galleries)
      sender ! (if (res > 0) {
        ImportSuccess(relativePath)
      } else {
        ImportError(relativePath, "Error when persist photo.")
      })
  }

}

class ImportImagesActor extends Actor {
  val router = Akka.system.actorOf(Props[ImportImageActor].withRouter(RoundRobinRouter(4)))

  def receive = {
    case ImportDirectory(dir, categories, galleries) =>
      val path = Path.fromString(dir)
      Person.userHome() match {
        case Some(ownerRoot) if (path.isDirectory) =>
          path.descendants(IsFile).foreach( p =>
            p.fileOption.foreach(img =>
              router ! ImportImage(img, ownerRoot, categories, galleries)
            )
          )
        case None => sender ! ImportError(dir, "User home doesn't found.")
        case _ => sender ! ImportError(dir, "Path isn't a directory.")
      }

    case event: ImportSuccess =>
      sender ! event

    case ImportError(path, message) =>
      Logger.error(path + " : " + message)
  }
}

