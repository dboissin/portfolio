package models

import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession

case class Gallery(id: Option[Long], name: String, description: Option[String])

object Galleries extends Table[Gallery]("galleries") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def description = column[Option[String]]("description")
  def * = id.? ~ name ~ description <> (Gallery, Gallery.unapply _)
  def photos = GalleriesToPhotos.filter(_.galleryId === id).flatMap(_.photoFK)

  def forInsert = name ~ description <> (
    {(n, d) => Gallery(None, n, d)}, { p:Gallery => Some((p.name, p.description))}
  )
  def insert(gallery: Gallery) = forInsert.insert(gallery)
}

