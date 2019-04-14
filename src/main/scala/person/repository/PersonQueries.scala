package person.repository

import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.meta.Meta
import person.model.model.{Gender, Person, PersonRequest}

/**
  * @author Ayush Mittal
  */
object PersonQueries {

  private implicit val importanceMeta: Meta[Gender] =
    Meta[String].xmap(Gender.unsafeFromString, _.value)

  def deletePerson(id: Long): ConnectionIO[Int] =
    sql"DELETE from person where id = ${id}".update.run

  def updatePerson(id: Long, personRequest: PersonRequest): ConnectionIO[Int] =
    sql"UPDATE person set first_name = ${personRequest.firstName}, last_name = ${personRequest.lastName}, gender = ${personRequest.gender} where id = ${id}".update.run

  def addPerson(person: PersonRequest): ConnectionIO[Long] =
    sql"INSERT INTO person (first_name, last_name, gender) values (${person.firstName}, ${person.lastName}, ${person.gender})".update
      .withUniqueGeneratedKeys[Long]("id")

  def findPerson(id: Long): ConnectionIO[Option[Person]] =
    sql"SELECT id, first_name, last_name, gender from person where id = $id"
      .query[Person]
      .option

}
