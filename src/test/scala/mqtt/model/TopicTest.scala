package mqtt.model


class TopicTest extends TopicFilterTest[Topic](Topic.apply) {

  test("A topic with a # wildcard is invalid.") {
    assert(Topic("sport/#").isEmpty)
  }
  
  test("A topic with a + wildcard is invalid.") {
    assert(Topic("+/tennis").isEmpty)
  }
  
  test("A topic can match a filter.") {
    assert(new Topic("sport/tennis/player").matching(new TopicFilter("sport/+/player")))
  }
}
