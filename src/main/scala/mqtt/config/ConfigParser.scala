package mqtt.config

import com.typesafe.scalalogging.LazyLogging
import fastparse.NoWhitespace._
import fastparse._
import mqtt.model.BrokerConfig

import scala.reflect.ClassTag

object ConfigParser extends LazyLogging {
  private def parameter[_: P](name: String) = !"#" ~ name.! ~ " "
  
  private def parseComment[_: P] = "#" ~ CharsWhile(_ != '\n').?
  
  private def port[_: P] = P(CharIn("0-9").rep(1).!.map(_.toInt).filter(_ < 65536))
  
  private def address[_: P] = CharIn("\\--z").rep(1, max = 2048).!
  
  private def boolean[_: P] = P("true").!.map(_ => true) | P("false").!.map(_ => false)
  
  private def parameters[_: P] =
    (parameter("port") ~ port.?) |
      (parameter("bind_address") ~ address.?) |
      (parameter("allow_anonymous") ~ boolean.?)
  
  private def configParser[_: P] = "\n".rep ~ ((parseComment | (parameters ~ parseComment.?)) ~ "\n".rep).rep ~ End
  
  private def configMap(fileContent: String): Option[Map[String, Option[Any]]] =
    parse(fileContent, configParser(_)) match {
      case Parsed.Success(value, _) => Option(value.collect({
        case (pName: String, pParam: Option[Any]) => pName -> pParam
      }).toMap[String, Option[Any]])
      case Parsed.Failure(label, index, extra) => {
        logger.error(s"Config parsing failed on index: $index, label: $label, extra: $extra")
        Option.empty
      }
    }
  
  private def setParam[T: ClassTag](key: String, f: (BrokerConfig, T) => BrokerConfig)(implicit data: Map[String, Option[Any]]): ConfigBuilder =
    ConfigBuilder(config => {
      data.get(key) match {
        case Some(Some(value: T)) => Option(f(config, value))
        case None => Option(config)
        case Some(None) => {
          logger.error(s"parameter '$key' need a value.")
          Option.empty
        }
      }
    })
  
  private def setPort(implicit data: Map[String, Option[Any]]): ConfigBuilder =
    setParam[Int]("port", {
      case (config, port) => config.copy(port = port)
    })
  
  private def setBindAddress(implicit data: Map[String, Option[Any]]): ConfigBuilder =
    setParam[String]("bind_address", {
      case (config, address) => config.copy(bindAddress = Option(address))
    })
  
  private def setAllowAnonymous(implicit data: Map[String, Option[Any]]): ConfigBuilder =
    setParam[Boolean]("allow_anonymous", {
      case (config, allow) => config.copy(allowAnonymous = allow)
    })
  
  private def configBuilder(implicit data: Map[String, Option[Any]]): ConfigBuilder = for {
    _ <- setPort
    _ <- setBindAddress
    s <- setAllowAnonymous
  } yield s
  
  /**
   * Parse the configuration file.
   *
   * @param fileContent content of the configuration file
   * @return the broker configuration
   */
  def apply(fileContent: String): Option[BrokerConfig] =
    configMap(fileContent).flatMap(data => configBuilder(data).run(BrokerConfig()))
}
