package person.client

import cats.effect.IO
import org.http4s.client._
import fs2.Stream
import io.circe.Json
import org.http4s.Request
import jawnfs2._
import person.config.TwitterConfig

/**
  * @author Ayush Mittal
  */
class TwitterHttpClient(client: Client[IO], twitterConfig: TwitterConfig) {

  // jawn-fs2 needs to know what JSON AST you want
  implicit val f = io.circe.jawn.CirceSupportParser.facade

  /* These values are created by a Twitter developer web app.
   * OAuth signing is an effect due to generating a nonce for each `Request`.
   */
  def sign(twitterConfig: TwitterConfig)(req: Request[IO]): IO[Request[IO]] = {
    val consumer =
      oauth1.Consumer(twitterConfig.consumerKey, twitterConfig.consumerSecret)
    val token =
      oauth1.Token(twitterConfig.accessKey, twitterConfig.accessSecret)
    oauth1.signRequest(req,
                       consumer,
                       callback = None,
                       verifier = None,
                       token = Some(token))
  }

  /* Create a http client, sign the incoming `Request[F]`, stream the `Response[IO]`, and
   * `parseJsonStream` the `Response[F]`.
   * `sign` returns a `F`, so we need to `Stream.eval` it to use a for-comprehension.
   */
  def jsonStream(req: Request[IO]): Stream[IO, Json] =
    for {
      sr <- Stream.eval(sign(twitterConfig)(req))
      res <- client.streaming(sr)(resp => resp.body.chunks.parseJsonStream)
    } yield res
}
