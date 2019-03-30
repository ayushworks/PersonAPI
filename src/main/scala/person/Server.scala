package person

import cats.effect.IO
import fs2.{Stream, StreamApp}
import org.http4s.server.blaze.BlazeBuilder
import person.config.Config
import person.database.Database
import person.repository.PersonRepository
import person.service.PersonService
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Ayush Mittal
  */
object Server extends StreamApp[IO] {
  override def stream(
      args: List[String],
      requestShutdown: IO[Unit]): Stream[IO, StreamApp.ExitCode] = {
    for {
      config <- Stream.eval(Config.load())
      transactor <- fs2.Stream.eval(Database.transactor(config.dbConfig))
      _ <- fs2.Stream.eval(Database.init(transactor))
      exitCode <- BlazeBuilder[IO]
        .bindHttp(config.serverConfig.port, config.serverConfig.host)
        .mountService(
          new PersonService(new PersonRepository(transactor)).service,
          "/")
        .serve
    } yield exitCode
  }
}
