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

import models.{Persons => Pers}
import models.Person
import models.InactiveUser

object Persons extends Controller {

  lazy val database = Database.forDataSource(DB.getDataSource())

  val personForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText,
      "email" -> optional(email)
    )
    ({(i, n, e) => Person(i, n, InactiveUser, e, None)})
    ({p:Person => Some((p.id, p.name, p.email))})
    verifying(Messages("error.email.exist"), {
      _ match {
        case p => !p.email.isDefined || !Person.isUsedEmail(p.id, p.email.get)
      }
    })
  )

  def form(id: Option[Long]) = Action {
    database.withSession{
      id.flatMap(personId => (for{p <- Pers if (p.id === personId)} yield p).list.headOption)
      .map(person => Ok(views.html.person.form(personForm.fill(person))))
      .getOrElse(Ok(views.html.person.form(personForm)))
    }
  }

  def submit = Action { implicit request =>
    personForm.bindFromRequest.fold(
      errors => BadRequest(views.html.person.form(errors)),
      person => {
        Logger.debug(person.toString)
        database.withSession{ //Transaction{
          person.id match {
            case Some(id) =>
              (for (p <- Pers if p.id === id) yield p.name ~ p.email)
              .update(person.name, person.email)
            case _ => Pers.insert(person)
          }
        }
        Ok(views.html.person.summary(person))
      }
    )
  }
}

