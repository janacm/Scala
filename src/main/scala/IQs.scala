object IQs {

  /*
   * Given an array of numbers, find the largest pair sum
   * {12, 34, 10, 6, 40} is 74.
   * {1,1,3,5} = 8
   * Solution:
   * Check for max1 and max2, where [ max2 < max1]
   * */
  def pairSum(arr: Array[Int]): Int = {
    if (arr.length == 1) {
      arr(0)
    } else {
      var first = Math.max(arr(0),arr(1))
      var second = Math.min(arr(0),arr(1))

      for (i <- 2 until arr.length ) {
        if (i > first) {
          second = first
          first = i
        }

        /* If arr[i] is in between first and second then update second  */
        else if (i > second && i != first)
          second = i
      }
      first + second
    }
  }
}

