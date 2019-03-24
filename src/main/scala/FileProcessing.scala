import java.io.IOException
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

/**
  * 1) Creates 3 folders (if not exists):
  *  i) ./input - Contains the files to be sent to the client
  *  ii) ./toMerge - Contains the slices of the original file that has now been split
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

  /*
  * Takes all files in the toMergeFolder and combines them into one large file in the mergedFolder
  *
  * 1) Creates a newFile-timestamp.txt in the ./output folder
  * 2) Appends all of the files inside the ./toMerge folder to the newFile-timestamp.txt
  * 3) Clears ./toMerge directory using deleteIfExists, to avoid throwing exceptions
  * */
  def mergeFiles(): Unit = {
    try {
      Files.write(Paths.get("myfile.txt"), "the text".getBytes(), StandardOpenOption.APPEND)
    } catch {
      case ioe: IOException => println(s"IOException thrown: $ioe")
      case e: Exception => println(s"Non-IOException thrown: $e")
    }
  }



}
