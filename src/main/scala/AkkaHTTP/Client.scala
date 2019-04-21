package AkkaHTTP

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.ContentNegotiator.Alternative.ContentType
import akka.stream.ActorMaterializer
import akka.util.ByteString

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


object Client {
  println("Started Client")

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher


    //    Begin Parallel file transfer on file "t1.txt" contained in the ./input folder
    sendGetFileRequest()

    /**
      * Sends GET request to initiate parallel file transfer
      * Server splits the input file contained in ./input when this GET request is received
      * Server sends back the number of file slices/parts as a response to this GET
      *
      * Note: Defined as nested function to allow access to implicit vals above
      */
    def sendGetFileRequest(): Unit ={
      val getFileRequest = HttpRequest(
        method = HttpMethods.GET,
        uri = s"http://localhost:8080/getFile",
        entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "data")
      )

      val getFile: Future[HttpResponse] = Http().singleRequest(getFileRequest)
      var numOfFileSplits = 0
      getFile.onComplete{
        case Success(value: HttpResponse) => {
          //          value.entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
          //            numOfFileSplits = body.utf8String.toInt
          //            println(s"Got number of file splits: $numOfFileSplits")
          value.entity.dataBytes.runForeach(i => println(i))(materializer)

          sendAsyncRequests(numOfFileSplits)
        }

        case Failure(exception) => sys.error("Something wrong during initial getFile request")
      }

    }



    //    Defined as nested function to allow access to implicit vals
    def sendAsyncRequests(numOfFileSplits: Int): Unit ={
      for (i <- 0 until numOfFileSplits ) {
        val getFileRequest = HttpRequest(uri = s"http://localhost:8080/query?sliceNumber=$i")
        val getFile: Future[HttpResponse] = Http().singleRequest(getFileRequest)
        getFile.onComplete{
          case Success(value) =>
            value.entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
              println(s"get query: ${ body.utf8String}")
            }
          case Failure(exception) => sys.error("Something wong")
        }
      }
    }



  }

}
