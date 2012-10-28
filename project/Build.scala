import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "portfolio"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      jdbc,
      "com.typesafe" % "slick_2.10.0-M7" % "0.11.1",
      "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
    )

}

