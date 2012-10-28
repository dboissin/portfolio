package models

import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession

object PersonsToPhotos extends Table[(Long, Long, String)]("persons_to_photos") {
  def personId = column[Long]("person_id")
  def photoId = column[Long]("photo_id")
  def position = column[String]("position") // json : x, y, width, height
  def * = personId ~ photoId ~ position
  def pk = primaryKey("pk_persons_to_photos", (personId, photoId))
  def personFK = foreignKey("person_fk", personId, Persons)(person => person.id)
  def photoFK = foreignKey("photo_fk", photoId, Photos)(photo => photo.id)
}

