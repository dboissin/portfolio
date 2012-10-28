package models

import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession

object GalleriesToPhotos extends Table[(Long, Long)]("galleries_to_photos") {
  def galleryId = column[Long]("gallery_id")
  def photoId = column[Long]("photo_id")
  def * = galleryId ~ photoId
  def pk = primaryKey("pk_galleries_to_photos", (galleryId, photoId))
  def galleryFK = foreignKey("gallery_fk", galleryId, Galleries)(gallery => gallery.id)
  def photoFK = foreignKey("photo_fk", photoId, Photos)(photo => photo.id)
}

