package controllers

import javax.inject._

import play.api._
import play.api.libs.ws.WSClient
import play.api.mvc._
import bots.botToken

import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents
                               , ws: WSClient)
                              (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  lazy val url = s"https://api.telegram.org/bot$botToken/getMe"

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action.async { implicit request: Request[AnyContent] =>
    ws.url(url)
      .get() // as this returns a Future the function is now async
      .map(_.json) // the the body as json
      .map(_.toString())
      .map(str => Ok(views.html.index(str))) // forward the result to the index page
  }
}
