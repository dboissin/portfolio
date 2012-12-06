package models

import play.api.db.DB
import play.api.Play.current
import play.api.Logger

import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession

case class GuestbookItem(id: Option[Long], title: String, body: String)

object GuestbookItems extends Table[GuestbookItem]("guestbook_items") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def body = column[String]("body", O.DBType("varchar(1000)"))
  def * = id.? ~ title ~ body <> (GuestbookItem, GuestbookItem.unapply _)

  def forInsert = title ~ body <> (
    {(t, b) => GuestbookItem(None, t, b)},
    { g:GuestbookItem => Some((g.title, g.body))}
  ) returning id
  def insert(guestbookItem: GuestbookItem) = forInsert.insert(guestbookItem)
}

