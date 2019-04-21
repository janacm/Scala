package JanacLibraries

import java.io.{BufferedOutputStream, IOException, OutputStream, PrintWriter}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.util.stream


/**
  * 1) Creates 3 folders (if not exists):
  *  i) ./input - Contains the files to be sent to the client
  *  ii) ./splitFiles - Contains the slices of the original file that has now been split
  *  ii) ./output - contains the file which has been merged back together
  */
class FileProcessing extends {

  println("File processing started")

  val inputPath: Path =       Paths.get("./input")
  val splitFilesPath: Path =  Paths.get("./server_splitFiles")
  val outputPath: Path =      Paths.get("./output")

  //  Creates folders needed for splitting and merging
  def createDirs(): Unit = {

    if (Files.notExists(inputPath)) Files.createDirectory(inputPath)
    if (Files.notExists(splitFilesPath)) Files.createDirectory(splitFilesPath)
    if (Files.notExists(outputPath)) Files.createDirectory(outputPath)
    println("Created dirs")

  }

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit): Unit ={
    val p = new PrintWriter(f)
    try {op(p)}
    finally {p.close()}
  }

  /*
  * Takes all files in the toMergeFolder and combines them into one large file in the mergedFolder
  *
  * 1) Appends all of the files named input/file.txt.split# to output/file.txt
  * 2) Clears ./splitFiles directory using deleteIfExists, to avoid throwing exceptions
  *
  * This code is called by the Client
  * */
  def mergeFiles(): Unit = {
    val mergedFile: Path = Paths.get("output/t1.txt") // TODO: add file name generically
    val mergedFileStream: OutputStream = new BufferedOutputStream(Files.newOutputStream(mergedFile))
    val dirStream = Files.newDirectoryStream(splitFilesPath)

    try {
      dirStream.forEach(file => {
        val it = Files.readAllLines(file).iterator()
        var currentSplitFile = ""
        while (it.hasNext){
          currentSplitFile += it.next()
        }
        val splitAsBytes = currentSplitFile.getBytes()

        mergedFileStream.write(splitAsBytes, 0, splitAsBytes.length)
      })
    } catch {
      case ioe: IOException => println(s"IOException thrown: $ioe")
      case e: Exception => println(s"Non-IOException thrown: $e")
    } finally {
      dirStream.close()
      mergedFileStream.close()
    }
  }


  /**
    * Splits the file found in the ./inputFolder and puts them into the ./toMergeFolder
    * Assumption: File is small enough to be read into memory
    *
    * 1) Clears ./splitFiles directory
    * 2) Reads the file from ./input/t1.txt
    * 3) For every 100k lines, it outputs into a new file
    * 4) Each new file has a Path: ./splitFiles/t1.txt.splitN where N = split number
    * 5) Split number increments until last file is reached
    *
    * TODO: Future Improvements such as:
    * - Make split size and file names configurable.
    * - Allow for non-text files to split.
    * - Use streams instead of reading whole file into memory
    */
  def splitFiles(): Unit = {
    clearSplitFiles()

    val fileToSplit = Paths.get("./input/t1.txt")
    if (Files.exists(fileToSplit)){
      val itr = Files.readAllLines(fileToSplit).iterator()
      var currentLine = 0
      var currentSliceNumber = 0
      var fileSliceOutputPath = splitFilesPath.resolve(s"t1.txt.split$currentSliceNumber")
      var out: OutputStream = new BufferedOutputStream( Files.newOutputStream(fileSliceOutputPath) )
      // output lines to a new partial file
      try {
        while (itr.hasNext){
          out.write(itr.next().getBytes() ++ "\n".getBytes())
          currentLine += 1
          if (currentLine % 100000 == 0) {
            out.close()
            // start new file slice
            currentSliceNumber += 1
            fileSliceOutputPath = splitFilesPath.resolve(s"t1.txt.split$currentSliceNumber")
            out = new BufferedOutputStream( Files.newOutputStream(fileSliceOutputPath) )
          }
        }
      } catch {
        case e: Throwable => println(s"Exception thrown during split file: $e")
      } finally {
        if (out != null) out.close()
      }
      println("Finished splitting files")
    } else {
      println(s"Cannot split non-existent file: $fileToSplit")
    }
  }


  //    Clear existing split files directory
  def clearSplitFiles(): Unit = {
    Files.walkFileTree(splitFilesPath, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }
    })
    println("Cleared split files folder")
  }

  /**
    * returns num of split files
    * @return
    */
  def getNumOfSplitFiles(): Int = {
    val fileList: stream.Stream[Path] = Files.list(splitFilesPath)
    var total = 0
    fileList.limit(5).forEach(item => {
      println(item)
      total+=1
    })
    total
  }
}
