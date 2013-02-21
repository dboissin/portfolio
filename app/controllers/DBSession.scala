package controllers

import play.api._
import play.api.mvc._
import scala.slick.driver.BasicDriver.simple._
import Database.threadLocalSession
import play.api.db.DB
import play.api.Play.current

trait DBSession {

  lazy val database = Database.forDataSource(DB.getDataSource())

  def DBAction(f: => Request[AnyContent] => Result) = Action { request =>
    database.withSession {
      f(request)
    }
  }

  def DBTransaction(f: => Request[AnyContent] => Result) = Action { request =>
    database.withTransaction {
      f(request)
    }
  }

}

