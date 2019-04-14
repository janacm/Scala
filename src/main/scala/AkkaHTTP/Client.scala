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

    val getFileRequest = HttpRequest(uri = "http://localhost:8080/query?sliceNum=1")
    val getFile: Future[HttpResponse] = Http().singleRequest(getFileRequest)

    getFile.onComplete{
      case Success(value) => println(s"get file value: $value")
      case Failure(exception) => sys.error("Something wrong during initial getFile request")
    }


    for (i <- 0 to 10 ) {
      val getQuery = HttpRequest(uri = s"http://localhost:8080/query?sliceNum=$i")
      val responseFuture3: Future[HttpResponse] = Http().singleRequest(getQuery)
      responseFuture3
        .onComplete{
          case Success(value) => println(s"get query: $value")
          case Failure(exception) => sys.error("Something wong")
        }

    }




  }

}
