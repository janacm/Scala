import java.io.{BufferedOutputStream, IOException, OutputStream, PrintWriter}
import java.nio.file.{Files, Path, Paths}
import java.util.stream


/**
  * 1) Creates 3 folders (if not exists):
  *  i) ./input - Contains the files to be sent to the client
  *  ii) ./splitFiles - Contains the slices of the original file that has now been split
  *  ii) ./output - contains the file which has been merged back together
  */
object FileProcessing extends App{

  println("File processing started")
  createDirs()

  //  Creates folders needed for splitting and merging
  def createDirs(): Unit = {
    val inputPath     = Paths.get("./input")
    val splitFilesPath= Paths.get("./splitFiles")
    val outputPath    = Paths.get("./output")

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
  * TODO: This code belongs in the client
  * Takes all files in the toMergeFolder and combines them into one large file in the mergedFolder
  *
  * 1) Appends all of the files named input/file.txt.split# to output/file.txt
  * 2) Clears ./splitFiles directory using deleteIfExists, to avoid throwing exceptions
  * */
  def mergeFiles(): Unit = {
    val mergedFile: Path = Paths.get("output/t1.txt") // TODO: add file name generically
    val mergedFileStream: OutputStream = new BufferedOutputStream(Files.newOutputStream(mergedFile))
    val dirStream = Files.newDirectoryStream(Paths.get("splitFiles"))

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
    */
  private def splitFiles(): Unit = {


  }

  /**
    * returns num of split files
    * @return
    */
  def getNumOfSplitFiles(): Int = {
    val splitFilesPath= Paths.get("./splitFiles")
    val fileList: stream.Stream[Path] = Files.list(splitFilesPath)
    var total = 0
    fileList.limit(5).forEach((item) => {
      println(item)
      total+=1
    })
    total
  }
}
