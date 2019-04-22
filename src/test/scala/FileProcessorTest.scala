import java.io.{BufferedOutputStream, OutputStream}
import java.nio.file.{Files, Path, Paths}

import JanacLibraries.FileProcessor
import org.scalatest.{BeforeAndAfter, FlatSpec}

class FileProcessorTest extends FlatSpec with BeforeAndAfter{
  val f = new FileProcessor()
  val testFileName: String = "test1.txt"

/*
 * Create files needed for tests:
 */
  before{
    f.createDirs()
    f.clearDir(f.client_splitFilesPath)
    f.clearDir(f.server_splitFilesPath)
    f.clearDir(f.clientOutputPath)

    val testFile1 = f.client_splitFilesPath.resolve(testFileName + ".split1")
    val testFile2 = f.client_splitFilesPath.resolve(testFileName + ".split2")
    val s1 = "test data in file1".getBytes()
    val s2 = "test data in file2".getBytes()
    val out1: OutputStream = new BufferedOutputStream(Files.newOutputStream(testFile1))
    val out2: OutputStream = new BufferedOutputStream(Files.newOutputStream(testFile2))

    try {
      out1.write(s1, 0, s1.length)
      out2.write(s2, 0, s2.length)
    } catch {
      case e: Throwable => println(s"Exception thrown during file writing: $e")
    } finally {
      out1.close()
      out2.close()
    }

  }

  "2 files in the client_splitFiles folder" should "be merged and stored as one file in " in {
    f.mergeFiles(testFileName)
    val mergedFile: Path = f.clientOutputPath.resolve(testFileName)
    val allLines= Files.readAllLines(mergedFile)
    val twoLineFile = allLines.get(0) + allLines.get(1)
    println(s"firstLine = ${twoLineFile}")
    assert(!allLines.isEmpty && twoLineFile.equals("test data in file1test data in file2"))
  }

  "get num of split files" should "return the number of files that have been split" in {
    val numOfPieces = f.getNumOfFilesInPath(f.client_splitFilesPath)
    assert(numOfPieces == 2)
  }

  "clear dir" should "delete all files in the given directory" in {
    f.clearDir(f.server_splitFilesPath)
    val numOfPieces = f.getNumOfSplitFiles
    assert(numOfPieces == 0)
  }

  "split files" should "split a large file in the input directory" in {

//    Make a large file over 10k lines
    val largeFile = f.serverInputPath.resolve(testFileName)
    val out: OutputStream = new BufferedOutputStream(Files.newOutputStream(largeFile))
    try
        for (i <- 0 to 10001)
          out.write((i.toString + "\n").getBytes())
    finally
      out.close()

    f.splitFiles(testFileName)

//    Should be split into two files
    val numOfPieces = f.getNumOfSplitFiles
    assert(numOfPieces == 2)
  }

  after {
    f.clearDir(f.client_splitFilesPath)
    f.clearDir(f.server_splitFilesPath)
    f.clearDir(f.clientOutputPath)
  }


}
