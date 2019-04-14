import java.io.File

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Framing, Source}
import akka.util.ByteString
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn

object WebServer {
  implicit val system: ActorSystem = ActorSystem("janacSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  var orders: List[Item] = Nil

  //    Domain model
  final case class Item(name: String, id: Long)
  final case class Order(items: List[Item])

  //    Formats for un/marshalling
  implicit val itemFormat: RootJsonFormat[Item] = jsonFormat2(Item)
  implicit val orderFormat: RootJsonFormat[Order] = jsonFormat1(Order)

  //    Fake async db query api
  def fetchItem(itemId: Long): Future[Option[Item]] = Future {
    orders.find(o => o.id == itemId)
  }

  def saveOrder(order: Order): Future[Done] = {
    orders = order match {
      case Order(items) => items ::: orders //concatenate lists
      case _            => orders
    }
    Future { Done }
  }

  def main (args: Array[String]): Unit ={

    val route=
      path("query") {
        get {
          parameters('sliceNumber.as[String]) {
            sliceNumber =>
              complete(s"sliceNumber: $sliceNumber ")
          }
        }
      } ~
        get {
          pathPrefix("item" /  LongNumber){ id =>
            val maybeItem: Future[Option[Item]] = fetchItem(id)

            onSuccess(maybeItem){
              case Some(item) => complete(item)
              case None       => complete(StatusCodes.NotFound)
            }
          }
        } ~
        post {
          path("create-order"){
            entity(as[Order]) { order =>
              val saved: Future[Done] = saveOrder(order)
              onComplete(saved) { done =>
                complete("order created")
              }
            }
          }
        } ~
        fileUpload("csv1"){
          case (fileinfo: FileInfo, byteSource: Source[ByteString, Any]) =>
            println(s"JANAC metadata = $fileinfo")
            println(s"JANAC byteSource = $byteSource")

            val value = byteSource.via(Framing.delimiter(ByteString("\n"), 1024))
            val value2 = value.mapConcat(_.utf8String.split(",").toVector)
            val value3 = value2.map(_.toInt)
            val sumF: Future[Int] = value3.runFold(0) { (acc, n) => acc + n }
            onSuccess(sumF) { sum=> complete(s"Sum: $sum")}

          //          case (metadata, byteSource) =>
          //            val sumF: Future[Int] =
          //            // sum the numbers as they arrive so we can accept any file size
          //              byteSource.via(Framing.delimiter(ByteString("\n"), 1024))
          //                .mapConcat(_.utf8String.split(",").toVector)
          //                .map(_.toInt)
          //                .runFold(0) { (acc, n) => acc + n }
          //            onSuccess(sumF) { sum=> complete(s"Sum: $sum")}

          case _ =>
            complete("yo")
        } ~
        storeUploadedFile(
          "csv",
          (fileInfo: FileInfo) => File.createTempFile("songee", ".tmp")
        ){
          case (fileInfo2, file: File) =>
            //                do sth
            //              file.delete()
            println(s"file.toString = ${file.toString}")
            println(s"file.getClass = ${file.getClass}")
            println("janac was here")
            complete(StatusCodes.OK)
          case _ =>
            complete("yo")

        }

    case class Video(file: File, title: String, author: String)
    object db {
      def create(video: Video): Future[Unit] = Future.successful(Unit)
    }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Started your first serber boiiiiiiii\n Press enter to stop")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}