import java.nio.file.{Files, Paths}

import org.scalatest.{BeforeAndAfter, FlatSpec}
import FileProcessing._

class FileProcessingTest extends FlatSpec with BeforeAndAfter{

/*
 * Create files needed for tests:
 */
  before{
    createDirs()
    val testFile1 = Paths.get("input/t1.txt")
    val testFile2 = Paths.get("input/t2.txt")
    Files.createFile(testFile1)
    Files.createFile(testFile2)



  }

  "2 files in the toMergedFolder" should "be merged" in {
    assert(true==true)

  }

}
