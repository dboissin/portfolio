import play.api.GlobalSettings
import play.api.Application
import play.api.Play
import play.api.Logger
import play.api.db.DB
import play.api.Play.current
import models._
import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession

object Global extends GlobalSettings {

  lazy val db = Database.forDataSource(DB.getDataSource())

  override def onStart(app: Application) {
    if (Play.isDev) {
      db.withSession {
        val ddl = Persons.ddl ++ Photos.ddl ++ Galleries.ddl ++ Categories.ddl ++ CategoriesToPhotos.ddl ++ GalleriesToPhotos.ddl ++ PersonsToPhotos.ddl
        Logger.debug(ddl.createStatements.reduceLeft(_ + _ + "\n"))
        //ddl.create
      }
    }
  }

}

