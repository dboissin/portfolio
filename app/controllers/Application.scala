package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def story = Action {
    Ok(views.html.story())
  }

  def wedding = Action {
    Ok(views.html.wedding())
  }

  def location = Action {
    Ok(views.html.location())
  }

  def guestbook = Action {
    Ok(views.html.guestbook())
  }

  def contact = Action {
    Ok(views.html.contact())
  }

}

