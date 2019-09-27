package mqtt.model

import org.scalatest.FunSuite

class FilterMatchTest extends FunSuite {
  val filters_match: Seq[(String, List[(String, Boolean)])] = Seq(
    "sport/tennis/player1/#" -> List(("sport/tennis/player1", true), ("sport/tennis/player1/ranking", true), ("sport/tennis/player1/score/wimbledon", true)),
    "sport/#" -> List(("sport", true)),
    "#" -> List(("//", true), ("game", true), ("sport/tennis/player", true), ("sport/tennis/player/", true)),
    "sport/tennis/+" -> List(("sport/tennis/player1", true), ("sport/tennis/player2", true), ("sport/tennis/player1/ranking", false)),
    "sport/+" -> List(("sport", false), ("sport/", true)),
    "+/+" -> List(("/swimming", true)),
    "/+" -> List(("/finance", true)),
    "+" -> List(("game", true), ("/game", false), ("$server", false)),
    "#" -> List(("$server", false), ("$", false), ("$/server", false), ("$/server/", false), ("/server/$", true), ("/$server/", true)),
    "+/monitor/Clients" -> List(("$SYS/monitor/Clients", false)),
    "$SYS/#" -> List(("$SYS/", true), ("$SYS/uptime", true)),
    "$SYS/monitor/+" -> List(("$SYS/monitor/Clients", true)),
    "sport/tennis" -> List(("sport/Tennis", false)),
    "sport/ten nis" -> List(("sport/ten nis", true)),
    "sport/tennis" -> List(("sport/tennis/", false)),
    "finance" -> List(("/finance", false)),
    "/" -> List(("/", true)),
    "sport/\\E.*\\Q" -> List(("sport/swimming", false)) //security regex test
  )
  
  testFiltersMatch()
  
  def testFiltersMatch(): Unit = {
    filters_match.foreach {
      case (filter, topics) =>
        topics.foreach {
          case (topic, shouldMatch) =>
            val not = if (shouldMatch) "" else " not"
            test(s"Filter '$filter' should$not match '$topic' topic.") {
              TopicFilter(filter).fold(fail)(f => assert(shouldMatch == f.matching(new Topic(topic))))
            }
        }
    }
  }
}
