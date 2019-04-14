package service

import cats.effect.IO
import org.http4s.circe._
import org.http4s.dsl.io._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import person.model.model.{
  Male,
  Person,
  PersonRequest,
  PersonTwitterInfo,
  PersonData
}
import person.repository.{PersonRepository, TwitterInfoQueries}
import person.service.PersonService
import io.circe.Json
import io.circe.literal._
import org.http4s.dsl.io.{POST, uri}
import org.http4s.{Request, Response, Status}
import person.client.TwitterHttpClient

/**
  * @author Ayush Mittal
  */
class PersonServiceSpec extends WordSpec with MockFactory with Matchers {

  private val repository = stub[PersonRepository]

  private val mockClient = stub[TwitterHttpClient]

  private val service =
    new PersonService(repository, mockClient).service

  "PersonService" should {
    "create a person" in {
      val id = 1
      val personReq = PersonRequest("Peter", "Parker", Male, "peter")
      val personData = PersonData(id, personReq)

      (repository.create _)
        .when(personReq)
        .returns(IO.pure(personData))

      val createJson = json"""
        {
        "firstName" : ${personReq.firstName},
        "lastName" : ${personReq.lastName},
        "gender" : ${personReq.gender.value},
        "screenName" : ${personReq.screenName}
        }"""

      val response = serve(
        Request[IO](POST, uri("/person")).withBody(createJson).unsafeRunSync())
      response.status shouldBe Status.Created
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": ${id},
          "firstName": ${personReq.firstName},
          "lastName": ${personReq.lastName},
          "gender" : ${personReq.gender.value},
          "screenName" : ${personReq.screenName}
        }"""
    }
  }

  private def serve(request: Request[IO]): Response[IO] = {
    service.orNotFound(request).unsafeRunSync()
  }
}
