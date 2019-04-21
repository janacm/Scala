package JanacLibraries

import java.nio.file.{Paths, StandardOpenOption}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString

import scala.concurrent.{ExecutionContextExecutor, Future}

object AkkaStreams extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val source1to100: Source[Int, NotUsed] = Source(1 to 100)
//  val done: Future[Done] = source1to100.runForeach(i => println(i))(materializer)


  val factorials: Source[BigInt, NotUsed] = source1to100.scan(BigInt(1))((acc, next) => acc * next)
  val result: Future[IOResult] = factorials.map(num => ByteString(s"$num\n"))
    .runWith(
      FileIO.toPath(
        Paths.get("client_splitFiles/factorials.txt"),
      )
    )

}
