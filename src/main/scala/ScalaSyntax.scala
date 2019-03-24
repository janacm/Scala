object ScalaSyntax {

  def main (args: Array[String]): Unit ={
    var list: List[String] = List.fill(3)("dawgz")
  }

  def listFilter(): Unit ={

    case class PhoneExt(name: String, ext: Int)
    val pList: List[PhoneExt] = List(PhoneExt("jan", 1), PhoneExt("pradriga", 2))
    System.out.println(pList.filter {case PhoneExt(_, extension) => extension >1})

  }

}
