import org.scalatest.FlatSpec

class IQsTest extends FlatSpec {

  "A list of 3 numbers" should "have a pair sum" in {
    assert(IQs.pairSum(Array(1,2,3)) == 5)
  }

  "A list of 3 numbers with max in middle " should "have a pair sum" in {
    assert(IQs.pairSum(Array(1,3,2)) == 5)
  }


  "A list of 3 numbers, reverse order" should "have a pair sum" in {
    assert(IQs.pairSum(Array(4,3,2)) == 7)
  }

  "A list of 5 numbers" should "have a pair sum" in {
    assert(IQs.pairSum(Array(12, 34, 10, 6, 40)) == 74)
  }

  "A list of 2 numbers, min first" should "have a pair sum" in {
    assert(IQs.pairSum(Array(12, 34)) == 46)
  }

  "A list of 2 numbers, max first" should "have a pair sum" in {
    assert(IQs.pairSum(Array(100, 34)) == 134)
  }

  "A list of numbers with duplicates" should "have a pair sum" in {
    assert(IQs.pairSum(Array(1,1,-2, 34)) == 35)
  }



}
