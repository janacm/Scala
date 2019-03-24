object Recursions {
  def permute(str: String): Set[Any] = {
//    permuteIteratively(str).split(",").map(_.trim).toSet
    permuteRecursively(str)
//    permuteScala(str).mkString("\n").split("\n").toSet
    Set("")
  }
  def permuteRecursively(input: String): Unit ={
    permuteRecursively("", input)
  }

  def permuteRecursively(prefix: String, word: String): Unit ={
    println(s"prefix: $prefix \n word: $word")
    println()
    if (word.isEmpty) println(prefix + word)
    else {
      for (i <- 0 to word.length -1){
        permuteRecursively(
          prefix + word.charAt(i),
          word.substring(0, i) + word.substring(i + 1, word.length)
        )
      }
    }
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

  def factorial(n: Int): Int = {
    var result = 0
    if (n==1)
      result = 1
    else {
      result = factorial(n-1) * n
      //      f(3) = f(3-1) * 3
      //      f(3) = f(2) * 3
      //      f(3) = (f(2-1) * 2) * 3
      //      f(3) = (f(1) * 2) * 3
      //      f(3) = (1* 2) *3
      //      f(3) = (2 * 3)
      //      f(3) = 6

    }
    result
  }


}
