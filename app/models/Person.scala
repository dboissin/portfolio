package models

import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession
import play.api.db.DB
import play.api.Play.current

class UserStatus(status: String)
object UserStatus {
  def apply(status: String) = status match {
    case "ActiveUser" => ActiveUser
    case "DeletedUser" => DeletedUser
    case _ => InactiveUser
  }
}
case object InactiveUser extends UserStatus(toString)
case object ActiveUser extends UserStatus(toString)
case object DeletedUser extends UserStatus(toString)

case class Person(
  id: Option[Long],
  name: String,
  status: UserStatus,
  email: Option[String],
  password: Option[String]
)

object Person {

  lazy val database = Database.forDataSource(DB.getDataSource())

  def isUsedEmail(id: Option[Long], email: String):Boolean = database.withSession {
    // TODO improve this query
      val l = (for (p <- Persons if (p.email === email)) yield p.id ~ p.email).list
      id.map(idx => l.filterNot(_._1 == idx))
      .getOrElse(l)
      .size > 0
  }

}

object Persons extends Table[Person]("persons") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def status = column[String]("status")
  def email = column[Option[String]]("email")
  def password = column[Option[String]]("password")
  def * = id.? ~ name ~ status ~ email ~ password <> (
    { (i, n, s, e, p) => Person(i, n, UserStatus(s), e, p) },
    { p:Person => Some((p.id, p.name, p.status.toString, p.email, p.password))}
  )
  def idx = index("idx_email", email, unique = true)
  def photos = PersonsToPhotos.filter(_.personId == id).flatMap(_.photoFK) // photos dans lequels person est présent

  def forInsert = name ~ status ~ email ~ password <> (
    { (n, s, e, p) => Person(None, n, UserStatus(s), e, p) },
    { p:Person => Some((p.name, p.status.toString, p.email, p.password))}
  )
  def insert(person: Person) = forInsert.insert(person)
}

