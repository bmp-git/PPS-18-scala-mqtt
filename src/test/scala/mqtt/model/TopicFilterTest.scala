package mqtt.model

import org.scalatest.FunSuite

import scala.reflect.ClassTag

class TopicFilterTest[T](build: String => Option[T])(implicit m: ClassTag[T]) extends FunSuite {
  val name: String = m.runtimeClass.getSimpleName
  
  test(s"An empty $name is invalid") {
    assert(build("").isEmpty)
  }
  
  def assertObjects(assertion: (T, T) => Boolean): (Option[T], Option[T]) => Unit = (topic1, topic2) => {
    (for {
      t1 <- topic1
      t2 <- topic2
    } yield (t1, t2)).fold(fail)(r => assert(assertion(r._1, r._2)))
  }
  
  def assertEqual: (Option[T], Option[T]) => Unit = assertObjects((t1, t2) => t1 == t2)
  
  def assertNotEqual: (Option[T], Option[T]) => Unit = assertObjects((t1, t2) => t1 != t2)
  
  
  test(s"Two $name with same name are equal.") {
    assertEqual(build("sport/swimming"), build("sport/swimming"))
  }
  
  test(s"$name are case sensitive.") {
    assertNotEqual(build("sport/tennis"), build("sport/Tennis"))
  }
  
  test(s"A $name with a space is valid.") {
    assert(build("sport/ten nis").isDefined)
  }
  
  test(s"A $name with only a / is valid.") {
    assert(build("/").isDefined)
  }
  
  test(s"A $name with two / is valid.") {
    assert(build("//").isDefined)
  }
  
  test(s"A $name with two / is different from a topic with only a /.") {
    assertNotEqual(build("/"), build("//"))
  }
  
  test(s"A leading / creates a distinct $name.") {
    assertNotEqual(build("sport"), build("/sport"))
  }
  
  test(s"A trailing / creates a distinct $name.") {
    assertNotEqual(build("sport"), build("sport/"))
  }
  
  test(s"A $name with a null character is invalid.") {
    assert(build("sport/ten\0nis").isEmpty)
  }
}
