package models

import play.api.db.DB
import play.api.Play.current
import play.api.Logger

import java.io.File
import scala.slick.driver.BasicDriver.simple._
import Database.threadLocalSession

case class Photo(id: Option[Long], path: String, name: String, description: Option[String], meta: Option[String], faces: Option[String])

object Photo {

  lazy val database = Database.forDataSource(DB.getDataSource())

  def create(path: String, meta: String, faces: String,
    associateCategories: List[Long], associateGalleries: List[Long]): Long =
    database.withTransaction {
      val photoId = Photos.insert(Photo(None, path, path.substring(path.lastIndexOf(
        File.separator) + 1), None, Some(meta), Some(faces)))
      CategoriesToPhotos.insertAll(
        associateCategories.map(categoryId => (categoryId, photoId)) : _*
      )
      GalleriesToPhotos.insertAll(
        associateGalleries.map(galleryId => (galleryId, photoId)) : _*
      )
      Logger.debug("create - photoId : " + photoId)
      photoId
  }

}


object Photos extends Table[Photo]("photos") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def path = column[String]("path")
  def name = column[String]("name")
  def description = column[Option[String]]("description")
  def meta = column[Option[String]]("meta", O.DBType("text"))
  def faces = column[Option[String]]("faces", O.DBType("text"))
  def * = id.? ~ path ~ name ~ description ~ meta ~ faces <> ( //Photo, Photo.unapply _)
    {(i, p, n, d, m, f) => Photo(i, p, n, d, m, f)},
    { p:Photo => Some((p.id, p.path, p.name, p.description, p.meta, p.faces))}
  )

  def galleries = GalleriesToPhotos.filter(_.photoId === id).flatMap(_.galleryFK)
  def persons = PersonsToPhotos.filter(_.photoId == id).flatMap(_.personFK) // person in the photo
  def categories = CategoriesToPhotos.filter(_.photoId == id).flatMap(_.categoryFK) //catégories de la photo

  def forInsert = path ~ name ~ description ~ meta ~ faces <> (
    {(p, n, d, m, f) => Photo(None, p, n, d, m, f)},
    { p:Photo => Some((p.path, p.name, p.description, p.meta, p.faces))}
  ) returning id
  def insert(photo: Photo) = forInsert.insert(photo)
}

