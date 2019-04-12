package AkkaHTTP

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.util.{Failure, Success}


object Client {
  println("Started Client")

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val getAkkaWebsite = HttpRequest(uri = "https://akka.io")

    val responseFuture: Future[HttpResponse] = Http()
      .singleRequest(getAkkaWebsite)

    responseFuture
      .onComplete{
        case Success(value) => println(s"value = ${value}")
        case Failure(exception) => sys.error("Something wong")
      }

    val postOrder = HttpRequest(
      method = HttpMethods.POST,
      uri = "http://localhost:8080/create-order",
      entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "data")
    )
  }

}
