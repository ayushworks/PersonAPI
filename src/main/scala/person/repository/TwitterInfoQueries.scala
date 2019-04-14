package person.repository

import doobie.free.connection.ConnectionIO
import doobie.implicits._
import person.model.model.{PersonRequest, PersonTwitterInfo}

/**
  * @author Ayush Mittal
  */
object TwitterInfoQueries {

  def deleteTwitterInfo(userId: Long): ConnectionIO[Int] =
    sql"DELETE from person_twitter_info where user_id = ${userId}".update.run

  def updateTwitterInfo(userId: Long,
                        personRequest: PersonRequest): ConnectionIO[Int] =
    sql"UPDATE person_twitter_info set screen_name = ${personRequest.screenName} where user_id = ${userId}".update.run

  def addTwitterInfo(person: PersonRequest, userId: Long): ConnectionIO[Long] =
    sql"INSERT into person_twitter_info(screen_name,user_id) values(${person.screenName}, ${userId})".update
      .withUniqueGeneratedKeys[Long]("id")

  def findTwitterInfo(userId: Long): ConnectionIO[Option[PersonTwitterInfo]] =
    sql"SELECT id,screen_name,user_id from person_twitter_info where user_id = $userId"
      .query[PersonTwitterInfo]
      .option
}
