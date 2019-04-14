package person.repository

import cats.effect.IO
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor
import person.model.model._
import doobie.implicits._
import cats.implicits._
import doobie.util.meta.Meta
import PersonQueries._
import TwitterInfoQueries._

/**
  * @author Ayush Mittal
  */
class PersonRepository(transactor: Transactor[IO]) {

  def create(person: PersonRequest): IO[PersonData] = {
    val composedIO = for {
      userId <- addPerson(person)
      _ <- addTwitterInfo(person, userId)
    } yield userId
    composedIO.transact(transactor).map { id =>
      PersonData(id, person)
    }
  }

  def find(id: Long): IO[Either[NotFoundError.type, PersonData]] = {
    val composedIO = for {
      person <- findPerson(id)
      twitterInfo <- person.traverseFilter[ConnectionIO, PersonTwitterInfo](p =>
        findTwitterInfo(p.id.get))
    } yield (person, twitterInfo)

    composedIO.transact(transactor).map {
      case (Some(person), Some(twitterInfo)) =>
        Right(PersonData.fromPerson(person, twitterInfo))
      case _ => Left(NotFoundError)
    }
  }

  def update(id: Long, personRequest: PersonRequest) = {
    val composedIO = for {
      updatePersonCount <- updatePerson(id, personRequest)
      updatedTwitterInfoCount <- updateTwitterInfo(id, personRequest)
    } yield (updatePersonCount, updatedTwitterInfoCount)

    composedIO.transact(transactor).map {
      case (0, _) => Left(NotFoundError)
      case (_, 0) => Left(NotFoundError)
      case _      => Right(PersonData(id, personRequest))
    }
  }

  def delete(id: Long): IO[Either[NotFoundError.type, Unit]] = {
    val composedIO = for {
      deletedTwitterInfoCount <- deleteTwitterInfo(id)
      deletedPersonCount <- deletePerson(id)
    } yield (deletedPersonCount, deletedTwitterInfoCount)

    composedIO.transact(transactor).map {
      case (0, _) => Left(NotFoundError)
      case (_, 0) => Left(NotFoundError)
      case _      => Right(())
    }
  }

}
