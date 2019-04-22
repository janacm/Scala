package JanacLibraries

import java.io.{BufferedOutputStream, IOException, OutputStream, PrintWriter}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.util.stream


class FileProcessor extends {

  println("File processing started")

  val serverInputPath: Path =       Paths.get("./server_input")
  val server_splitFilesPath: Path =  Paths.get("./server_splitFiles")
  val client_splitFilesPath: Path =  Paths.get("./client_splitFiles")
  val clientOutputPath: Path =      Paths.get("./client_output")

  /**
    * 1) Creates 4 folders (if non-existant):
    *  i)   ./server_input - Contains the files to be sent to the client
    *  ii)  ./server_splitFiles - Contains slices of the file to be sent to the client
    *  iii) ./client_splitFiles - Contains slices received by the client
    *  iv)  ./client_output - contains the file which has been merged back together by the client
    */
  def createDirs(): Unit = {
    if (Files.notExists(serverInputPath)) Files.createDirectory(serverInputPath)
    if (Files.notExists(server_splitFilesPath)) Files.createDirectory(server_splitFilesPath)
    if (Files.notExists(client_splitFilesPath)) Files.createDirectory(client_splitFilesPath)
    if (Files.notExists(clientOutputPath)) Files.createDirectory(clientOutputPath)
    println("Created dirs")
  }

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit): Unit ={
    val p = new PrintWriter(f)
    try {op(p)}
    finally {p.close()}
  }

  /*
  * Takes all files from ./client_splitFiles with filename file.txt.split# and
  * combines them into one large file into ./clientOutput/file.txt
  *
  * Clears ./splitFiles directory using deleteIfExists, to avoid throwing exceptions
  * This code is triggered by the Client
  * */
  def mergeFiles(fileName: String): Unit = {
    println("File merging started")
    val mergedFile: Path = clientOutputPath.resolve(fileName)
    val mergedFileStream: OutputStream = new BufferedOutputStream(Files.newOutputStream(mergedFile))
    val dirStream = Files.newDirectoryStream(client_splitFilesPath)

    try {
      dirStream.forEach(file => {
        println(s"Current file: $file")
        val it = Files.readAllLines(file).iterator()
        var currentSplitFile = ""
        while (it.hasNext){
          val currentLine = it.next()
          currentSplitFile += (currentLine + "\n")
          println(s"Merging currentLine: $currentLine")
        }
        val splitAsBytes = currentSplitFile.getBytes()

        println("Writing to output...")
        mergedFileStream.write(splitAsBytes, 0, splitAsBytes.length)
      })
    } catch {
      case ioe: IOException => println(s"IOException thrown: $ioe")
      case e: Exception => println(s"Non-IOException thrown: $e")
    } finally {
      dirStream.close()
      mergedFileStream.close()
      println("Merging complete")
    }
  }


  /**
    * Splits the file found in the ./server_input and puts them into the ./toMergeFolder
    * Assumption: File is small enough to be read into memory
    *
    * 1) Clears ./splitFiles directory
    * 2) Reads the file from ./server_input/t1.txt
    * 3) For every 100k lines, it outputs into a new file
    * 4) Each new file has a Path: ./splitFiles/t1.txt.splitN where N = split number
    * 5) Split number increments until last file is reached
    *
    * TODO: Future Improvements such as:
    * - Make split size and file names configurable.
    * - Allow for non-text files to split.
    * - Use streams instead of reading whole file into memory
    */
  def splitFiles(filename: String): Unit = {
    clearDir(server_splitFilesPath)
    println(s"Splitting file: '$filename' in $serverInputPath")

    val fileToSplit = serverInputPath.resolve(filename)
    if (Files.exists(fileToSplit)){
      val itr = Files.readAllLines(fileToSplit).iterator()
      var currentLine = 0
      var currentSliceNumber = 0
      var fileSliceOutputPath = server_splitFilesPath.resolve(s"t1.txt.split$currentSliceNumber")
      var out: OutputStream = new BufferedOutputStream( Files.newOutputStream(fileSliceOutputPath) )
      // output lines to a new partial file
      try {
        while (itr.hasNext){
          out.write(itr.next().getBytes() ++ "\n".getBytes())
          currentLine += 1
          if (currentLine % 10000 == 0) {
            out.close()
            // start new file slice
            currentSliceNumber += 1
            fileSliceOutputPath = server_splitFilesPath.resolve(s"t1.txt.split$currentSliceNumber")
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
  def clearDir(p: Path): Unit = {
    Files.walkFileTree(p, new SimpleFileVisitor[Path] {
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
  def getNumOfSplitFiles: Int = {
    getNumOfFilesInPath(server_splitFilesPath)
  }

  def getNumOfFilesInPath(p: Path): Int = {
    val fileList: stream.Stream[Path] = Files.list(p)
    var total = 0
    fileList.forEach(item => {
      println(item)
      total+=1
    })
    total
  }
}
