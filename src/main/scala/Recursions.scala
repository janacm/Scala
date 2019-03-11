object Recursions {
  def permute(str: String): Set[Any] = {
//    permuteIteratively(str).split(",").map(_.trim).toSet
    permuteScala(str).mkString("\n").split("\n").toSet
  }

  def permuteScala(str: String): Set[Any] = {
    str.permutations.mkString("\n").split("\n").toSet
  }

  def permuteIteratively(str: String): String = {
    var sb: String = ""
    if (str.length == 2) {
      sb = permuteTwo(str).head + "," + permuteTwo(str).tail.head
    } else {
      for (i <- 0 to str.length -1){
        val addedChar = str.charAt(i)
        val stringWithCharRemoved = str.replace(String.valueOf(str.charAt(i)), "")
        sb +=  addedChar + permuteTwo(stringWithCharRemoved).head + ","
        sb +=  addedChar + permuteTwo(stringWithCharRemoved).tail.head + ","
      }
    }
    sb
  }

  def permuteTwo(str: String) : Set[String] = {
    Set(str.head + str.tail , str.tail + str.head)
  }
}
