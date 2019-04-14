package AkkaHTTP

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


object Client {
  println("Started Client")

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val getItem1 = HttpRequest(uri = "http://localhost:8080/item/1")
    val getQuery = HttpRequest(uri = "http://localhost:8080/query?sliceNum=1")

    for (i <- 0 to 10 ) {
      val responseFuture2: Future[HttpResponse] = Http().singleRequest(getItem1)
      responseFuture2
        .onComplete{
          case Success(value) => println("get item success")
          case Failure(exception) => sys.error("Something wong")
        }

      println("between")

      val responseFuture3: Future[HttpResponse] = Http().singleRequest(getQuery)
      responseFuture3
        .onComplete{
          case Success(value) => println("get query success")
          case Failure(exception) => sys.error("Something wong")
        }

    }




  }

}
