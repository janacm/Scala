import java.io.{BufferedOutputStream, OutputStream}
import java.nio.file.{Files, Paths}

import JanacLibraries.FileProcessor
import org.scalatest.{BeforeAndAfter, FlatSpec}


class FileProcessorTest extends FlatSpec with BeforeAndAfter{
  val f = new FileProcessor()

/*
 * Create files needed for tests:
 */
  before{
    f.clearSplitFiles()
    f.createDirs()
    val testFile1 = Paths.get("server_splitFiles/t1.txt.split1")
    val testFile2 = Paths.get("server_splitFiles/t1.txt.split2")
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
    f.mergeFiles()
    val mergedFile = Paths.get("output/t1.txt")
    val allLines= Files.readAllLines(mergedFile)
    assert(!allLines.isEmpty && allLines.get(0).equals("testFile1testFile2"))

  }

  "get num of split files" should "return the number of files that have been split" in {
    val numOfPieces = f.getNumOfSplitFiles()
    assert(numOfPieces == 2)
  }

  "clear split files" should "delete all files in the splitFiles directory" in {
    f.clearSplitFiles()
    val numOfPieces = f.getNumOfSplitFiles()
    assert(numOfPieces == 0)
  }

  "split files" should "split a large file in the input directory" in {
    f.clearSplitFiles()
//    Make a large file over 100k lines
    val largeFile = Paths.get("./input/t1.txt")
    val out: OutputStream = new BufferedOutputStream(
      Files.newOutputStream(largeFile)
    )
    try {
      for ( i <- 0 to 100001 ) {
        out.write((i.toString()+"\n").getBytes())
      }
    } finally {
      out.close()
    }

    f.splitFiles()

//    Should be split into two files
    val numOfPieces = f.getNumOfSplitFiles()
    assert(numOfPieces == 2)
  }

  after {
    f.clearSplitFiles()
  }


}
