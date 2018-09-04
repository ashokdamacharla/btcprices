package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
  * Created by Ashok on 2018/09/01.
  *
  * Swagger documentation for this application APIs.
  * 
  */
class ApiDocsController @Inject()(cc: ControllerComponents, configuration: Configuration) extends AbstractController(cc) {

  //Redirecting to swagger.json to view the documentation.
  def redirectToDocs = Action {
    val basePath = configuration.underlying.getString("base.api.uri")
    Redirect(
      url = "/assets/lib/swagger-ui/index.html",
      queryString = Map("url" -> Seq(s"$basePath/swagger.json"))
    )
  }

}
