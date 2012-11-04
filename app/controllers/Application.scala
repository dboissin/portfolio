package controllers

import play.api._
import play.api.mvc._

import utils._

import java.io.File

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def test = Action {
    val photo = new File(Photos.rootImages + Fake.findPhoto(319).get.path)
    val smallPhoto = File.createTempFile("blip", ".jpg")
    ImageUtils.resize(photo, smallPhoto, 930)
    Logger.debug(ImageUtils.extractMetadata(photo))
    Logger.debug(ImageUtils.jsonFaceDetection(smallPhoto).toString)
    Ok.sendFile(smallPhoto, true)
  }

}
