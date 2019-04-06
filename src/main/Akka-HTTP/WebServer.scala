import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future
import scala.io.StdIn

object WebServer {
    implicit val system: ActorSystem = ActorSystem("janacSystem")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    var orders: List[Item] = Nil

//    Domain model
    final case class Item(name: String, id: Long)
    final case class Order(items: List[Item])

//    Formats for un/marshalling
    implicit val itemFormat = jsonFormat2(Item)
    implicit val orderFormat = jsonFormat1(Order)

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

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Started your first serber boiiiiiiii\n Press enter to stop")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
