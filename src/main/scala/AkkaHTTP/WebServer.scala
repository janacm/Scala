package AkkaHTTP

import JanacLibraries.FileProcessor
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
  var fileProcessor = new FileProcessor()

  val filename: String = "t1.txt"

  def main(args: Array[String]): Unit ={

    val route=
    //      Initiates parallel transfer, and file splitting.
      path("getFile") {
        get {
          println("received GET: /getFile ")
          fileProcessor.splitFiles(filename)
          val numOfSplits = fileProcessor.getNumOfSplitFiles
          println(s"numOfSplits = $numOfSplits")
          complete(numOfSplits.toString)
        }
      } ~
        path("query") { //response: a file slice as specified by the query param
          get {
            parameters('sliceNumber.as[String]) {
              sliceNumber =>
                println(s"Received GET: /query with slice number: $sliceNumber")
                val fileSliceOutputPath = fileProcessor.server_splitFilesPath
                  .resolve(s"t1.txt.split$sliceNumber")
                getFromFile(fileSliceOutputPath.toString)
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