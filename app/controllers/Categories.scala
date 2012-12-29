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

import models.{ Category, Categories => Cats }

object Categories extends Controller {

  lazy val database = Database.forDataSource(DB.getDataSource())

  val categoryForm = Form(
    mapping(
      "category.id" -> optional(longNumber),
      "category.name" -> nonEmptyText
    )
    ({ (i, n) => Category(i, n, n.replaceAll(" ", "_")) })
    ({ c: Category => Some((c.id, c.name)) })
  )

  def addCategory = Action { implicit request =>
    categoryForm.bindFromRequest.fold(
      errors => {
        Logger.debug(errors.toString)
        BadRequest
      },
      category => {
        database.withSession {
          Cats.insert(category)
          Ok
        }
      }
    )
  }

}

