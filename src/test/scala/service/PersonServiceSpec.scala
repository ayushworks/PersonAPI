package service

import cats.effect.IO
import org.http4s.circe._
import org.http4s.dsl.io._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import person.model.model.{Male, Person}
import person.repository.PersonRepository
import person.service.PersonService
import io.circe.Json
import io.circe.literal._
import org.http4s.dsl.io.{POST, uri}
import org.http4s.{Request, Response, Status}

/**
  * @author Ayush Mittal
  */
class PersonServiceSpec extends WordSpec with MockFactory with Matchers {

  private val repository = stub[PersonRepository]

  private val service = new PersonService(repository).service

  "PersonService" should {
    "create a person" in {
      val id = 1
      val person = Person(None, "Peter", "Parker", Male)
      (repository.createPerson _)
        .when(person)
        .returns(IO.pure(person.copy(id = Some(id))))
      val createJson = json"""
        {
        "firstName" : ${person.firstName},
        "lastName" : ${person.lastName},
        "gender" : ${person.gender.value}
        }"""
      val response = serve(
        Request[IO](POST, uri("/person")).withBody(createJson).unsafeRunSync())
      response.status shouldBe Status.Created
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "firstName": ${person.firstName},
          "lastName": ${person.lastName},
          "gender" : ${person.gender.value}
        }"""
    }
  }

  private def serve(request: Request[IO]): Response[IO] = {
    service.orNotFound(request).unsafeRunSync()
  }
}
