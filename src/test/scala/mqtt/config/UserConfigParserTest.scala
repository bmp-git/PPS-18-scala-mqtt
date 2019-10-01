package mqtt.config

import org.scalatest.FunSuite

class UserConfigParserTest extends FunSuite {
  test("Parsean empty user configuration") {
    val res = UserConfigParser("")
    assert(res == Option(Map()))
  }
  
  test("Parse a single user configuration") {
    val res = UserConfigParser("asd:B133a0c0e9bee3be20163d2ad31d6248db292aa6dcb1ee087a2aa50e0fc75ae2")
    assert(res == Option(Seq[(String, Option[String])]("asd" -> Option("b133a0c0e9bee3be20163d2ad31d6248db292aa6dcb1ee087a2aa50e0fc75ae2")).toMap))
  }
  
  test("Parse a multiple user configuration") {
    val res = UserConfigParser("\n\nasd:b133a0c0e9bee3be20163d2ad31d6248db292aa6dcb1ee087a2aa50e0fc75ae2\nlol:")
    assert(res == Option(Seq[(String, Option[String])]("asd" -> Option("b133a0c0e9bee3be20163d2ad31d6248db292aa6dcb1ee087a2aa50e0fc75ae2"),
      "lol" -> Option.empty).toMap))
  }
  
  
  test("Parse a bad user configuration (password too short)") {
    val res = UserConfigParser("asd:b133a0c0e9bee3be20163d2ad31d6248db292aa6dcb1ee087a2aa50e0fc75ae")
    assert(res == Option.empty)
  }
  
  test("Parse a bad user configuration (password too long)") {
    val res = UserConfigParser("asd:b133a0c0e9bee3be20163d2ad31d6248db292aa6dcb1ee087a2aa50e0fc75aeee")
    assert(res == Option.empty)
  }
  
  test("Parse a bad user configuration (invalid password)") {
    val res = UserConfigParser("asd:g133a0c0e9bee3be20163d2ad31d6248db292aa6dcb1ee087a2aa50e0fc75aee")
    assert(res == Option.empty)
  }
  
  test("Parse a bad user configuration (username empty)") {
    val res = UserConfigParser(":g133a0c0e9bee3be20163d2ad31d6248db292aa6dcb1ee087a2aa50e0fc75aee")
    assert(res == Option.empty)
  }
  
  
}
