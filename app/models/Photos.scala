package models

import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession

case class Photo(id: Option[Long], path: String, name: String, description: Option[String], meta: Option[String])

object Photos extends Table[Photo]("photos") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def path = column[String]("path")
  def name = column[String]("name")
  def description = column[Option[String]]("description")
  def meta = column[Option[String]]("meta", O.DBType("text"))
  def * = id.? ~ path ~ name ~ description ~ meta <> (Photo, Photo.unapply _)
  def galleries = GalleriesToPhotos.filter(_.photoId === id).flatMap(_.galleryFK)
  def persons = PersonsToPhotos.filter(_.photoId == id).flatMap(_.personFK) // person in the photo
  def categories = CategoriesToPhotos.filter(_.photoId == id).flatMap(_.categoryFK) //catégories de la photo

  def forInsert = path ~ name ~ description ~ meta <> (
    {(p, n, d, m) => Photo(None, p, n, d, m)},
    { p:Photo => Some((p.path, p.name, p.description, p.meta))}
  )
  def insert(photo: Photo) = forInsert.insert(photo)
}

