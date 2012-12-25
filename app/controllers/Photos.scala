package controllers

import play.api._
import play.api.mvc._
import Play.current
import play.api.libs.concurrent._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.libs.EventSource
import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession
import play.api.db.DB
import play.api.Play.current

import akka.actor._
import akka.routing._

import java.io.File
import scalax.file.Path
import scalax.file.PathMatcher._

import actors._
import models.{ Photo, Photos => Ps }
import models.{Galleries => Gals }
import models.{Categories => Cats}

object Photos extends Controller {

  val rootImages = Play.configuration.getString("root.images").getOrElse("")
  lazy val imagesPath = Path.fromString(rootImages)
  lazy val database = Database.forDataSource(DB.getDataSource())

  def index = Action {
    Ok
  }

  def photo(id: Long) = Action {
    database.withSession {
      (for{ p <- Ps if (p.id === id)} yield p).list.headOption.map(photo =>
        Ok(views.html.photos.photo(photo))
      )
    }.getOrElse(NotFound(views.html.errors.notFound()))
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

  def create = Action {
    val folders = imagesPath.descendants(IsDirectory).toList
    database.withSession {
      val galleries = (for (g <- Gals) yield g).list
      val categories = (for (c <- Cats) yield c).list
      Ok(views.html.photos.create(folders, galleries, categories))
    }
  }

  def tii = Action {
    Ok(views.html.test())
  }

  def testImportImages = Action {
    val out = Concurrent.unicast[JsValue] { channel =>
      val env = Akka.system.actorOf(Props(new ImportImagesActor(channel)))
      env ! ImportDirectory("/Users/mumu/tmpportfolio/test01", Nil, Nil)
      }
    Ok.stream(out &> EventSource()).as(play.api.http.ContentTypes.EVENT_STREAM)
  }

}

