package AkkaHTTP

import JanacLibraries.FileProcessor
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


object Client {
  println("Started Client")
  val fileProcessor = new FileProcessor
  val filename= "t1.txt"

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
      println(s"Sent request: $getFile")
      var numOfFileSplits = 0 // result from GET
      getFile.onComplete{
        case Success(value: HttpResponse) =>
          value.entity.dataBytes.runForeach(i => {
            numOfFileSplits = i.utf8String.toInt
            println(s"Received number of split files from server: $numOfFileSplits")
            sendAsyncRequests(numOfFileSplits)
          })(materializer)


        case Failure(exception) => sys.error(s"Something wrong during initial getFile request: $exception")
      }

    }



    //    Defined as nested function to allow access to implicit vals
    def sendAsyncRequests(numOfFileSplits: Int): Unit = {
      println(s"Sending $numOfFileSplits async requests for file slices")
      var numOfRequestsCompleted = 0
      for (i <- 0 until numOfFileSplits ) {
        val getFileRequest = HttpRequest(uri = s"http://localhost:8080/query?sliceNumber=$i")
        println(s"Making request: $getFileRequest")
        val getFile: Future[HttpResponse] = Http().singleRequest(getFileRequest)
        getFile.onComplete{
          case Success(value) =>
            println("Request successful!")
            value.entity.dataBytes.runWith(
              FileIO.toPath(fileProcessor.client_splitFilesPath
                .resolve(s"$filename.split$i"))
            )
            numOfRequestsCompleted+=1
            if (numOfRequestsCompleted == numOfFileSplits){
              println("All file slices received! Proceeding to merge files...")
              fileProcessor.mergeFiles(filename)
            }
          case Failure(exception) => sys.error(s"Something wrong during async getFile requests: $exception")
        }
      }
    }



  }

}
