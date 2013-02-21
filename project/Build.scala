import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "portfolio"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      jdbc,
      "com.typesafe.slick" %% "slick" % "1.0.0",
      "org.openimaj" % "core" % "1.0.5",
      "org.openimaj" % "faces" % "1.0.5",
      "org.imgscalr" % "imgscalr-lib" % "4.2",
      "com.drewnoakes" % "metadata-extractor" % "2.6.2",
      "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      resolvers += "OpenIMAJ repository" at "http://maven.openimaj.org"
    )

}

