import org.scalatest.FunSuite

class Test extends FunSuite {
  test("An empty Set should have size 0") {
    println("Test1")
    assert(Set.empty.isEmpty)
  }
}
