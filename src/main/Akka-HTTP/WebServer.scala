import java.io.File

import akka.http.scaladsl.server._

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, Multipart, StatusCodes, Uri}
import akka.http.scaladsl.model.HttpMethods._

import scala.concurrent.duration._
import akka.http.scaladsl.model.Multipart.BodyPart
import akka.http.scaladsl.model.headers.LinkParams.title
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Framing}
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
      }

    val highLevelDirectiveRoute =
      extractRequestContext{ ctx =>
        implicit val materializer = ctx.materializer

        fileUpload("csv"){
          case (metadata, byteSource) =>

            val sumF: Future[Int] =
              // sum the numbers as they arrive so we can accept any file size
              byteSource.via(Framing.delimiter(ByteString("\n"), 1024))
                .mapConcat(_.utf8String.split(",").toVector)
                .map(_.toInt)
                .runFold(0) { (acc, n) => acc + n }
            onSuccess(sumF) { sum=> complete(s"Sum: $sum")}

          case _ =>
            complete("yo")
        }
      }


//    val uploadVideo =
//      path("video") {
//        entity(as[Multipart.FormData]) { formData =>
//
//          // collect all parts of the multipart as it arrives into a map
//          val allPartsF: Future[Map[String, Any]] = formData.parts.mapAsync[(String, Any)](1) {
//
//            case b: BodyPart if b.name == "file" =>
//              // stream into a file as the chunks of it arrives and return a future
//              // file to where it got stored
//              val file = File.createTempFile("upload", "tmp")
//              b.entity.dataBytes.runWith(FileIO.toPath(file.toPath)).map(_ =>
//                (b.name -> file))
//
//            case b: BodyPart =>
//              // collect form field values
//              b.toStrict(2.seconds).map(strict =>
//                (b.name -> strict.entity.data.utf8String))
//
//          }.runFold(Map.empty[String, Any])((map, tuple) => map + tuple)
//
//          val done = allPartsF.map { allParts =>
//            // You would have some better validation/unmarshalling here
//            db.create(Video(
//              file = allParts("file").asInstanceOf[File],
//              title = allParts("title").asInstanceOf[String],
//              author = allParts("author").asInstanceOf[String]))
//          }
//
//          // when processing have finished create a response for the user
//          onSuccess(allPartsF) { allParts =>
//            complete {
//              "ok!"
//            }
//          }
//        }
//      }


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
