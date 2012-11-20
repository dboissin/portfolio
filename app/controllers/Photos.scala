package controllers

import play.api._
import play.api.mvc._
import Play.current
import play.api.libs.concurrent._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.libs.EventSource

import akka.actor._
import akka.routing._

import java.io.File

import actors._
import models.{ Photo, Photos => Ps }

object Photos extends Controller {

  val rootImages = Play.configuration.getString("root.images").getOrElse("")

  def index = Action {
    Ok
  }

  def photo(id: Long) = Action {
    Fake.findPhoto(id).map( photo =>
      Ok(views.html.photos.photo(photo))
    ).getOrElse(NotFound)
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
   Ok(views.html.photos.create(Nil, Nil, Nil))
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

