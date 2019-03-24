package person.service

import cats.effect.IO
import org.http4s.{HttpService, MediaType, Uri}
import org.http4s.dsl.Http4sDsl
import person.repository.PersonRepository
import io.circe.generic.auto._
import io.circe.syntax._
import fs2.Stream
import org.http4s.headers.{Location, `Content-Type`}
import person.model.model.{Gender, Person, PersonNotFoundError}
import org.http4s.circe._
import io.circe.{Decoder, Encoder}

/**
  * @author Ayush Mittal
  */
class PersonService(repository: PersonRepository) extends Http4sDsl[IO] {

  private implicit val encodeGender: Encoder[Gender] =
    Encoder.encodeString.contramap[Gender](_.value)

  private implicit val decodeGender: Decoder[Gender] =
    Decoder.decodeString.map[Gender](Gender.unsafeFromString(_))

  val service = HttpService[IO] {
    case GET -> Root / "person" =>
      Ok(Stream("[") ++ repository.getPeople
           .map(_.asJson.noSpaces)
           .intersperse(",") ++ Stream("]"),
         `Content-Type`(MediaType.`application/json`))

    case GET -> Root / "person" / LongVar(id) =>
      for {
        getResult <- repository.getPerson(id)
        response <- personResult(getResult)
      } yield response

    case req @ POST -> Root / "person" =>
      for {
        person <- req.decodeJson[Person]
        createdPerson <- repository.createPerson(person)
        response <- Created(
          createdPerson.asJson,
          Location(Uri.unsafeFromString(s"/person/${createdPerson.id.get}")))
      } yield response

  }

  private def personResult(result: Either[PersonNotFoundError.type, Person]) =
    result match {
      case Left(PersonNotFoundError) => NotFound()
      case Right(person)             => Ok(person.asJson)
    }
}
