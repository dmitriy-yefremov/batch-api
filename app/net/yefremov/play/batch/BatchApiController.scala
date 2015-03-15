package net.yefremov.play.batch

import scala.concurrent.Future
import scala.util.control.NonFatal

import play.api.Play
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Enumerator
import play.api.mvc._

/**
 * An example of a Facebook-style batch API implementation for Play. Only GET requests are supported.
 *
 * The controller handles batch requests, delegates processing to the corresponding individual handlers
 * and assembles the aggregated response.
 *
 * @author Dmitriy Yefremov.
 */
object BatchApiController extends Controller {

  def batchGet(): Action[AnyContent] = Action.async { implicit request =>
    val resultFutures = request.queryString.map { case (name, values) =>
      fetch(values.head).map(name -> _)
    }
    Future.sequence(resultFutures).map(combineResults)
  }

  private def fetch(path: String)(implicit request: RequestHeader): Future[Result] = {
    val fetchRequest = createFetchRequest(path)
    val handler = Play.current.global.onRouteRequest(fetchRequest)
    handler.map {
      case action: EssentialAction => proxyAction(action)(fetchRequest)
      case x => Future.failed(new IllegalArgumentException(s"Unexpected handler type"))
    } getOrElse {
      Future.failed(new IllegalArgumentException(s"No handler for path '$path'"))
    }
  }

  private def createFetchRequest(path: String)(implicit request: RequestHeader): RequestHeader = {
    request.copy(path = path, uri = path)
  }

  private def proxyAction(action: => EssentialAction)(implicit request: RequestHeader): Future[Result] = {
    // surround acton invocation with try and recover with a failed future in case there is an exception in the action
    try {
      val actionIteratee = action(request)
      actionIteratee.run
    } catch {
      case NonFatal(e) => Future.failed(e)
    }
  }

  private def combineResults(results: Iterable[(String, Result)]): Result = {

    def bytesEnumerator(s: String) = Enumerator(s.getBytes)
    def openBrace = bytesEnumerator("{")
    def closeBrace = bytesEnumerator("}")
    def comma = bytesEnumerator(",")
    def namedBlock(name: String) = bytesEnumerator(s""""$name":""")
    def isLast(index: Int) = index == results.size - 1

    if (results.isEmpty) {
      NoContent
    } else {
      val body = results.zipWithIndex.foldLeft(openBrace) { case (acc, ((name, result), index)) =>
        acc
          .andThen(namedBlock(name))
          .andThen(result.body)
          .andThen(
            if (isLast(index)) {
              closeBrace
            } else {
              comma
            }
          )
      }
      Result(ResponseHeader(OK), body)
    }

  }

}
