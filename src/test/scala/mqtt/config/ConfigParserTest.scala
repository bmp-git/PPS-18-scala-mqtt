package mqtt.config

import mqtt.model.BrokerConfig
import org.scalatest.FunSuite

class ConfigParserTest extends FunSuite {
  test("An empty configuration file should be the default configuration") {
    assert(ConfigParser("") == Option(BrokerConfig()))
  }
  
  test("Parse a normal configuration file") {
    val input = "\n\n#comment\n\n\n\nport 1234#comment\n#comment\n#comment\n\nbind_address localhost#\nallow_anonymous false#"
    assert(ConfigParser(input) == Option(BrokerConfig(1234, Option("localhost"), allowAnonymous = false)))
  }
  
  test("Parse a bad configuration file (port too large)") {
    val input = "#comment\n\n\n\nport 456455#comment\n#comment\n#comment\n\nbind_address localhost#\nallow_anonymous false#"
    assert(ConfigParser(input) == Option.empty)
  }
  
  test("Parse a bad configuration file (port not a number)") {
    val input = "#comment\n\n\n\nport asd#comment\n#comment\n#comment\n\nbind_address localhost#\nallow_anonymous false#"
    assert(ConfigParser(input) == Option.empty)
  }
  
  test("Parse a bad configuration file (anonymous not boolean)") {
    val input = "#comment\n\n\n\nport 1234#comment\n#comment\n#comment\n\nbind_address localhost#\nallow_anonymous asd#"
    assert(ConfigParser(input) == Option.empty)
  }
  
  test("Parse a bad configuration file (address not valid)") {
    val input = "#comment\n\n\n\nport 1234#comment\n#comment\n#comment\n\nbind_address !!#\nallow_anonymous false#"
    assert(ConfigParser(input) == Option.empty)
  }
  
  test("Parse a bad configuration file (port not present)") {
    val input = "#comment\n\n\n\nport\n#comment\n#comment\n\nbind_address localhost#\nallow_anonymous false#"
    assert(ConfigParser(input) == Option.empty)
  }
}
