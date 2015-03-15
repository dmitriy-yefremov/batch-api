package net.yefremov.play.sample

import play.api.libs.json.Json._
import play.api.mvc.{Action, Controller}

/**
 * Sample controller with a couple of JSON endpoints. It is used to demo the batch API.
 * @author Dmitriy Yefremov
 */
object FooBarControler extends Controller {

  def message(text: String) = Map("message" -> text)

  def foo = Action {
    val msg = message("Hi, this is Foo")
    Ok(toJson(msg))
  }

  def bar = Action {
    val msg = message("Hi, this is Bar")
    Ok(toJson(msg))
  }

}
