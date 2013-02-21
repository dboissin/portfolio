package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import scala.slick.driver.BasicDriver.simple._
import Database.threadLocalSession

import models.{ Gallery, Galleries => Gals }

object Galleries extends Controller with DBSession {

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

  def addGallery = DBAction { implicit request =>
    galleryForm.bindFromRequest.fold(
      errors => {
        Logger.debug(errors.toString)
        BadRequest
      },
      gallery => {
        Gals.insert(gallery)
        Ok
      }
    )
  }

}

