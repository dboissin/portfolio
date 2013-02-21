package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n.Messages
import scala.slick.driver.BasicDriver.simple._
import Database.threadLocalSession

import models.{Persons => Pers}
import models.Person
import models.InactiveUser

object Persons extends Controller with DBSession {

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

  def form(id: Option[Long]) = DBAction { request =>
      id.flatMap(personId => (for{p <- Pers if (p.id === personId)} yield p).list.headOption)
      .map(person => Ok(views.html.person.form(personForm.fill(person))))
      .getOrElse(Ok(views.html.person.form(personForm)))
  }

  def submit = DBAction { implicit request =>
    personForm.bindFromRequest.fold(
      errors => BadRequest(views.html.person.form(errors)),
      person => {
        Logger.debug(person.toString)
          person.id match {
            case Some(id) =>
              (for (p <- Pers if p.id === id) yield p.name ~ p.email)
              .update(person.name, person.email)
            case _ => Pers.insert(person)
          }
        Ok(views.html.person.summary(person))
      }
    )
  }
}

