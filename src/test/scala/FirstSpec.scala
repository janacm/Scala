import org.scalatest.FlatSpec

class FirstSpec extends FlatSpec {

  "A string ab" should "be permuted" in {
    assert(Recursions.permute("ab") == Set("ab", "ba"))
    assert(Recursions.permute("ab") == Set("ba", "ab")) //order shouldn't matter
  }

  "A string abc" should "be permuted" in {
    assert(Recursions.permute("abc") == Set(
      "abc",
      "acb",
      "cab",
      "cba",
      "bca",
      "bac",
    ))
  }

  "A string xyz" should "be permuted" in {
    assert(Recursions.permute("xyz") == Set(
      "xyz",
      "xzy",
      "zxy",
      "zyx",
      "yzx",
      "yxz",
    ))
  }


}
