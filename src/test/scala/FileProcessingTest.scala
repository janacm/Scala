import java.io.{BufferedOutputStream, OutputStream}
import java.nio.file.{Files, Paths}

import FileProcessing._
import org.scalatest.{BeforeAndAfter, FlatSpec}


class FileProcessingTest extends FlatSpec with BeforeAndAfter{

/*
 * Create files needed for tests:
 */
  before{
    createDirs()
    val testFile1 = Paths.get("splitFiles/t1.txt.split1")
    val testFile2 = Paths.get("splitFiles/t1.txt.split2")
    val s1 = "testFile1".getBytes()
    val s2 = "testFile2".getBytes()

    val out1: OutputStream = new BufferedOutputStream(
      Files.newOutputStream(testFile1))

    val out2: OutputStream = new BufferedOutputStream(
      Files.newOutputStream(testFile2))

    try {
      out1.write(s1, 0, s1.length)
      out2.write(s2, 0, s2.length)
    } catch {
      case _ => println("Exception thrown during file writing")
    } finally {
      out1.close()
      out2.close()
    }


  }

  "2 files in the splitFiles folder" should "be merged and stored as one file in " in {
    FileProcessing.mergeFiles()
    val mergedFile = Paths.get("output/t1.txt")
    val allLines= Files.readAllLines(mergedFile)
    assert(!allLines.isEmpty && allLines.get(0).equals("testFile1testFile2"))

  }

}
