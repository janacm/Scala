package AkkaHTTP

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.server.ContentNegotiator.Alternative.ContentType
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


object Client {
  println("Started Client")

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher


    val getFileRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = s"http://localhost:8080/getFile",
      entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "data")
    )

    val getFile: Future[HttpResponse] = Http().singleRequest(getFileRequest)
    var numOfFileSplits = 0
    getFile.onComplete {
      case Success(value: HttpResponse) =>
//        numOfFileSplits = value.toString().toInt
//        println(s"valueeeeeee = ${value.toString().toInt}")
        println(s"valueeeeeee = ${value.toString()}")
        println(s"Got number of file splits: $numOfFileSplits")

      case Failure(exception) => sys.error("Something wrong during initial getFile request")
    }



//    Defined as nested function to allow access to implicit vals
    def sendAsyncRequests(numOfFileSplits: Int): Unit ={
      for (i <- 0 to numOfFileSplits ) {
        val getFileRequest = HttpRequest(uri = s"http://localhost:8080/query?sliceNum=$i")
        val getFile: Future[HttpResponse] = Http().singleRequest(getFileRequest)
        getFile
          .onComplete{
            case Success(value) => println(s"get query: $value")
            case Failure(exception) => sys.error("Something wong")
          }
      }
    }



  }

}
