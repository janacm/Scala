import JanacLibraries.FileProcessing
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object WebServer {
  implicit val system: ActorSystem = ActorSystem("janacSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  var fileProcessor = new FileProcessing()

  def main(args: Array[String]): Unit ={

    val route=
    //      Initiates parallel transfer, and file splitting.
      path("getFile") {
        get {
          println("received GET: getFile ")
          fileProcessor.splitFiles()
          val numOfSplits = fileProcessor.getNumOfSplitFiles()
          complete(numOfSplits.toString)
        }
      } ~
        path("query") {
          get {
            parameters('sliceNumber.as[String]) { //returns a file slice as response
              sliceNumber =>
                getFromFile(s"./splitFiles/t1.txt.split$sliceNumber")
//                complete(s"sliceNumber: $sliceNumber ")
            }
          }
        }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Started Parallel File Transfer Server \n Press enter to stop")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}