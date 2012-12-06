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
import play.api.i18n.Messages

import models._

object Guestbook extends Controller {

  lazy val database = Database.forDataSource(DB.getDataSource())

  val messageForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> text(minLength=1, maxLength=254),
      "body" -> text(minLength=1, maxLength=1000)
    )(GuestbookItem)(GuestbookItem.unapply)
  )

  def guestbook = Action {
    database.withSession{
      val messages = (for (m <- GuestbookItems) yield m).list
      Ok(views.html.guestbook.index(messageForm, messages))
    }
  }

  def addMessage = Action { implicit request =>
    messageForm.bindFromRequest.fold(
      errors => BadRequest(views.html.guestbook.index(errors, Nil)),
      message => {
        database.withSession{
          GuestbookItems.insert(message)
        }
        Redirect(routes.Guestbook.guestbook())
      }
    )
  }
}

