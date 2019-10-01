package mqtt.config

import fastparse.NoWhitespace._
import fastparse._

object UserConfigParser {
  private def username[_: P] = CharsWhile(_ != ':').! ~ ":"
  
  private def password[_: P] = (CharIn("0-9") | CharIn("a-f") | CharIn("A-F")).rep(min = 64, max = 64).!
  
  private def userConfigParser[_: P] = "\n".rep ~ (username ~ password.? ~ "\n".rep).rep ~ End
  
  /**
   * Parse the configuration file of users.
   *
   * @param fileContent content of the configuration file
   * @return Option(Map(username -> Option(sha256 password)))
   */
  def apply(fileContent: String): Option[Map[String, Option[String]]] =
    parse(fileContent, userConfigParser(_)) match {
      case Parsed.Success(value, _) => Option(value.collect({
        case (pName: String, Some(pass)) => pName -> Option(pass.toLowerCase)
        case (pName: String, None) => pName -> Option.empty
      }).toMap[String, Option[String]])
      case Parsed.Failure(label, index, extra) => {
        println(s"User config parsing failed on index: $index, label: $label, extra: $extra")
        Option.empty
      }
    }
}
