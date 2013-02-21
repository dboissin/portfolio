package controllers

import play.api._
import play.api.mvc._
import Play.current
import play.api.libs.concurrent._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.libs.EventSource
import scala.slick.driver.BasicDriver.simple._
import Database.threadLocalSession

import akka.actor._
import akka.routing._

import java.io.File
import scalax.file.Path
import scalax.file.PathMatcher._
import scala.util.control.Exception

import actors._
import models.{ Photo, Photos => Ps }
import models.{Galleries => Gals }
import models.{Categories => Cats}

object Photos extends Controller with DBSession {

  val rootImages = Play.configuration.getString("root.images").getOrElse("")
  lazy val imagesPath = Path.fromString(rootImages)

  def index = Action {
    Ok
  }

  def photo(id: Long) = DBAction { request =>
    (for{ p <- Ps if (p.id === id)} yield p).list.headOption.map(photo =>
      Ok(views.html.photos.photo(photo))
    )
    .getOrElse(NotFound(views.html.errors.notFound()))
  }

  def image(size: String, path: String) = Action {
    // TODO check right
    // TODO manage three size
    val absPath = rootImages + (size match {
      case "t" =>
        val p = path.splitAt(path.lastIndexOf('/') + 1)
        p._1 + "thumb_" + p._2
      case "o" => path
      case "n" => path
      case _ => ""
    })
    Logger.debug(absPath)
    val img = new File(absPath)
    if (img.exists() && img.isFile()) {
      Ok.sendFile(img, true)
    } else {
      NotFound
    }
  }

  def create = DBAction { request =>
    val folders = imagesPath.descendants(IsDirectory).toList
    val galleries = (for (g <- Gals) yield g).list
    val categories = (for (c <- Cats) yield c).list
    Ok(views.html.photos.create(folders, galleries, categories))
  }

  def importImages(path: String, galleries: String, categories: String) = Action {
    // TODO check galleries and categories existance.
    val categoriesIds = categories.split(",").map(id =>
      Exception.catching(classOf[NumberFormatException]).opt(id.toLong)
    ).toList.flatten
    val galleriesIds = galleries.split(",").map(id =>
      Exception.catching(classOf[NumberFormatException]).opt(id.toLong)
    ).toList.flatten
    val out = Concurrent.unicast[JsValue] { channel =>
      val env = Akka.system.actorOf(Props(new ImportImagesActor(channel)))
      env ! ImportDirectory(path, categoriesIds, galleriesIds)
    }
    Ok.stream(out &> EventSource()).as(play.api.http.ContentTypes.EVENT_STREAM)
  }

}

