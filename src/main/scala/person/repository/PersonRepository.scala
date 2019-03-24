package person.repository

import cats.effect.IO
import doobie.Meta
import doobie.util.transactor.Transactor
import fs2.Stream
import person.model.model.{Gender, Person, PersonNotFoundError}
import doobie.implicits._

/**
  * @author Ayush Mittal
  */
class PersonRepository(transactor: Transactor[IO]) {

  private implicit val importanceMeta: Meta[Gender] =
    Meta[String].xmap(Gender.unsafeFromString, _.value)

  def getPeople: Stream[IO, Person] =
    sql"SELECT id, first_name, last_name, gender from person"
      .query[Person]
      .stream
      .transact(transactor)

  def getPerson(id: Long): IO[Either[PersonNotFoundError.type, Person]] =
    sql"SELECT id, first_name, last_name, gender from person where id = $id"
      .query[Person]
      .option
      .transact(transactor)
      .map {
        case Some(person) => Right(person)
        case None         => Left(PersonNotFoundError)
      }

  def createPerson(person: Person): IO[Person] =
    sql"INSERT into person (first_name, last_name, gender) values (${person.firstName}, ${person.lastName}, ${person.gender})".update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map { id =>
        person.copy(id = Some(id))
      }

  def deletePerson(id: Long): IO[Either[PersonNotFoundError.type, Unit]] =
    sql"DELETE from person where id = $id".update.run.transact(transactor).map {
      affectedRows =>
        affectedRows match {
          case 0 => Left(PersonNotFoundError)
          case _ => Right(())
        }
    }

  def updatePerson(
      id: Long,
      person: Person): IO[Either[PersonNotFoundError.type, Person]] =
    sql"UPDATE person SET first_name = ${person.firstName}, last_name = ${person.lastName}, gender = ${person.gender} where id = $id".update.run
      .transact(transactor)
      .map { affectedRows =>
        affectedRows match {
          case 0 => Left(PersonNotFoundError)
          case _ => Right(person.copy(id = Some(id)))
        }
      }

}
