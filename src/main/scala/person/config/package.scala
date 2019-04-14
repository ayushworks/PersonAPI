package person

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import pureconfig.error.ConfigReaderException

/**
  * @author Ayush Mittal
  */
package object config {

  case class ServerConfig(host: String, port: Int)

  case class DBConfig(driver: String,
                      url: String,
                      user: String,
                      password: String)

  case class TwitterConfig(consumerKey: String,
                           consumerSecret: String,
                           accessKey: String,
                           accessSecret: String)

  case class Config(serverConfig: ServerConfig,
                    dbConfig: DBConfig,
                    twitterConfig: TwitterConfig)

  object Config {

    import pureconfig._
    import pureconfig.generic.auto._
    def load(configFile: String = "application.conf"): IO[Config] =
      IO {
        pureconfig.loadConfig[Config](ConfigFactory.load(configFile))
      } flatMap {
        case Left(e) =>
          IO.raiseError[Config](new ConfigReaderException[Config](e))
        case Right(config) => IO.pure[Config](config)
      }

  }
}
