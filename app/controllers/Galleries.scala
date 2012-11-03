package controllers

import play.api._
import play.api.mvc._
import models.{ Gallery, Galleries => Gals }

object Galleries extends Controller {

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
}

