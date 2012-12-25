package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession
import play.api.db.DB
import play.api.Play.current

import models.{ Gallery, Galleries => Gals }

object Galleries extends Controller {

  lazy val database = Database.forDataSource(DB.getDataSource())

  val galleryForm = Form(
    mapping(
      "gallery.id" -> optional(longNumber),
      "gallery.name" -> nonEmptyText,
      "gallery.description" -> optional(text)
    )(Gallery)(Gallery.unapply _)
  )

  def index = Action {
    val galleries = Fake.galleries
    Ok(views.html.galleries.index(galleries))
  }

  def gallery(id: Long) = Action {
    Fake.findGallery(id).map{ gallery =>
      val photos = Fake.listPhotos(id)
      Ok(views.html.galleries.gallery(gallery, photos))
    }.getOrElse(NotFound)
  }

  def addGallery = Action { implicit request =>
    galleryForm.bindFromRequest.fold(
      errors => {
        Logger.debug(errors.toString)
        BadRequest
      },
      gallery => {
        database.withSession {
          Gals.insert(gallery)
          Ok
        }
      }
    )
  }

}

