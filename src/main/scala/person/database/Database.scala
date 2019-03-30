package person.database

import cats.effect.IO
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import person.config.DBConfig

/**
  * @author Ayush Mittal
  */
object Database {

  def transactor(config: DBConfig): IO[HikariTransactor[IO]] =
    HikariTransactor.newHikariTransactor[IO](config.driver,
                                             config.url,
                                             config.user,
                                             config.password)

  def init(transactor: HikariTransactor[IO]): IO[Unit] =
    transactor.configure { dataSource =>
      IO {
        val flyway = Flyway.configure().dataSource(dataSource).load()
        flyway.migrate()
        ()
      }
    }
}
