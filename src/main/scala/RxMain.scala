import com.typesafe.scalalogging.LazyLogging
import mqtt.config.{ConfigParser, UserConfigParser}
import mqtt.model.BrokerConfig
import mqtt.server._

import scala.io.Source
import scala.util.Try

/**
 * Entry point of the mqtt broker.
 */
object RxMain extends App with LazyLogging {
  private val SETTINGS_PATH = "settings.conf"
  private val USERS_SETTINGS_PATH = "users.conf"
  
  private def readFile(file: String): Option[String] = {
    Try {
      val bufferedSource = Source.fromFile(file)
      val data = bufferedSource.mkString("\n")
      bufferedSource.close
      data
    }.toOption match {
      case None =>
        logger.warn(s"Error while opening $file")
        None
      case Some(a) => Option(a)
    }
  }
  
  val brokerConfig = readFile(SETTINGS_PATH) match {
    case Some(data) => ConfigParser(data) match {
      case Some(brokerConfig) => brokerConfig
      case None => BrokerConfig()
    }
    case None => BrokerConfig()
  }
  
  val usersConfig: Map[String, Option[String]] = readFile(USERS_SETTINGS_PATH) match {
    case Some(data) => UserConfigParser(data) match {
      case Some(usersConfig) => usersConfig
      case None => Map()
    }
    case None => Map()
  }
  
  logger.info(s"Loaded ${usersConfig.size} user configurations.")
  logger.info(s"Using $brokerConfig configuration.")
  val stopper = MqttBroker(brokerConfig, usersConfig).run()
  scala.io.StdIn.readLine()
  stopper.stop()
  logger.debug("Bye")
}


