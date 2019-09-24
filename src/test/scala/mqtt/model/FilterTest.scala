package mqtt.model

class FilterTest() extends TopicFilterTest[TopicFilter](TopicFilter.apply) {
  test("The filter '#' is valid.") {
    assert(TopicFilter("#").isDefined)
  }
  
  test("The filter 'sport/tennis/#' is valid.") {
    assert(TopicFilter("sport/tennis/#").isDefined)
  }
  
  test("The filter 'sport/tennis#' is not valid.") {
    assert(TopicFilter("sport/tennis#").isEmpty)
  }
  
  test("The filter 'sport/#/ranking' is not valid.") {
    assert(TopicFilter("sport/#/ranking").isEmpty)
  }
  
  test("The filter 'sport/#/ranking/#' is not valid.") {
    assert(TopicFilter("sport/#/ranking/#").isEmpty)
  }
  
  test("The filter '+' is valid.") {
    assert(TopicFilter("+").isDefined)
  }
  
  test("The filter '+/tennis/#' is valid.") {
    assert(TopicFilter("+/tennis/#").isDefined)
  }
  
  test("The filter 'sport+' is not valid.") {
    assert(TopicFilter("sport+").isEmpty)
  }
  
  test("The filter 'sport/+/player1' is valid.") {
    assert(TopicFilter("sport/+/player1").isDefined)
  }
}
