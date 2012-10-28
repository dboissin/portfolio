package models

import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession

case class Category(id: Option[Long], name: String, scope:String)

object Categories extends Table[Category]("categories") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def scope = column[String]("scope")
  def * = id.? ~ name ~ scope <> (Category, Category.unapply _)
  def photos = CategoriesToPhotos.filter(_.categoryId == id).flatMap(_.photoFK)

  def forInsert = name ~ scope <> (
    {(n, s) => Category(None, n, s)}, { c:Category => Some((c.name, c.scope))}
  )
  def insert(category: Category) = forInsert.insert(category)
}

