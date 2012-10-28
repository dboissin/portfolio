package models

import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession

object CategoriesToPhotos extends Table[(Long, Long)]("categories_to_photos") {
  def categoryId = column[Long]("category_id")
  def photoId = column[Long]("photo_id")
  def * = categoryId ~ photoId
  def pk = primaryKey("pk_categories_to_photos", (categoryId, photoId))
  def categoryFK = foreignKey("category_fk", categoryId, Categories)(category => category.id)
  def photoFK = foreignKey("photo_fk", photoId, Photos)(photo => photo.id)
}

