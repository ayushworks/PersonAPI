package person.service

import cats.effect.IO
import org.http4s.{HttpService, MediaType, Method, Request, Uri}
import org.http4s.dsl.Http4sDsl
import person.repository.{PersonRepository}
import io.circe.generic.auto._
import io.circe.syntax._
import fs2.Stream
import org.http4s.headers.{Location, `Content-Type`}
import person.model.model._
import org.http4s.circe._
import io.circe.{Decoder, Encoder, Json}
import person.client.TwitterHttpClient

/**
  * @author Ayush Mittal
  */
class PersonService(repository: PersonRepository,
                    twitterHttpClient: TwitterHttpClient)
    extends Http4sDsl[IO] {

  private implicit val encodeGender: Encoder[Gender] =
    Encoder.encodeString.contramap[Gender](_.value)

  private implicit val decodeGender: Decoder[Gender] =
    Decoder.decodeString.map[Gender](Gender.unsafeFromString(_))

  val service = HttpService[IO] {

    case GET -> Root / "person" / LongVar(id) =>
      for {
        person <- repository.find(id)
        response <- personResult(person)
      } yield response

    case GET -> Root / "person" / "tweets" / LongVar(id) =>
      for {
        person <- repository.find(id)
        response <- Ok(Stream("[") ++ tweetResult(person)
                         .map(_.asJson.noSpaces)
                         .intersperse(",") ++ Stream("]"),
                       `Content-Type`(MediaType.`application/json`))
      } yield response

    case req @ POST -> Root / "person" =>
      for {
        person <- req.decodeJson[PersonRequest]
        createdPerson <- repository.create(person)
        response <- Created(
          PersonResponse.fromPersonData(createdPerson).asJson,
          Location(Uri.unsafeFromString(s"/person/${createdPerson.id}")))
      } yield response

    case DELETE -> Root / "person" / LongVar(id) =>
      for {
        deleteResult <- repository.delete(id)
        response <- personDeleteResult(deleteResult)
      } yield response

    case req @ PUT -> Root / "person" / LongVar(id) =>
      for {
        person <- req.decodeJson[PersonRequest]
        updatedPerson <- repository.update(id, person)
        response <- personResult(updatedPerson)
      } yield response
  }

  private def personResult(result: Either[NotFoundError.type, PersonData]) =
    result match {
      case Left(NotFoundError) => NotFound()
      case Right(person)       => Ok(PersonResponse.fromPersonData(person).asJson)
    }

  private def personDeleteResult(result: Either[NotFoundError.type, Unit]) =
    result match {
      case Left(NotFoundError) => NotFound()
      case Right(person)       => NoContent()
    }

  private def tweetResult(result: Either[NotFoundError.type, PersonData])
    : Stream[IO, PersonTweets] =
    result match {
      case Left(NotFoundError) => Stream.empty
      case Right(person)       => apiCall(person.personRequest.screenName)
    }

  private def apiCall(screenName: String): Stream[IO, PersonTweets] = {
    val req = Request[IO](
      Method.GET,
      Uri.unsafeFromString(
        s"https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=$screenName&count=10"))
    twitterHttpClient.jsonStream(req).map { json =>
      PersonTweets(screenName,
                   json.findAllByKey("text").map(j => j.asString.getOrElse("")))
    }
  }
}
