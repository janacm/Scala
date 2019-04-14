import FileProcessing._
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

  def main(args: Array[String]): Unit ={

    val route=
    //      Initiates parallel transfer, and file splitting.
      path("getFile") {
        get {
          splitFiles()
          val numOfSplits = getNumOfSplitFiles()
          complete(numOfSplits.toString)
        }
      } ~
        path("query") {
          get {
            parameters('sliceNumber.as[String]) {
              sliceNumber =>
                complete(s"sliceNumber: $sliceNumber ")
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