package person

import cats.effect.IO
import fs2.{Stream, StreamApp}
import org.http4s.client.blaze.Http1Client
import org.http4s.server.blaze.BlazeBuilder
import person.client.TwitterHttpClient
import person.config.Config
import person.database.Database
import person.repository.{PersonRepository, TwitterInfoQueries}
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
      client <- Http1Client.stream[IO]()
      exitCode <- BlazeBuilder[IO]
        .bindHttp(config.serverConfig.port, config.serverConfig.host)
        .mountService(
          new PersonService(
            new PersonRepository(transactor),
            new TwitterHttpClient(client, config.twitterConfig)).service,
          "/"
        )
        .serve
    } yield exitCode
  }
}
