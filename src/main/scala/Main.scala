import io.{ParameterizedReader, Reader, Writer}
import util.Reporter
import com.typesafe.config.ConfigFactory
import entity.{DateRange, DriveInfo}
import mapper.DriveMapper
import validator.DriveValidator

object Main {

  def execute(): Unit = {
    val config = ConfigFactory.load
    val path = config.getString("filename.path")
    val sourceFilenames = config.getString("filename.source")
    val sourceFilenamesList: List[String] = sourceFilenames.split(",").toList
    val sourceFilenamesListWithPath = sourceFilenamesList.map(path + _)
    val bikeFilename = config.getString("filename.bike")
    val generalFilename = config.getString("filename.general")
    val usageFilename = config.getString("filename.usage")
//    val reader = Reader
    val reader = new ParameterizedReader[DriveInfo](DriveValidator, DriveMapper)
    val reporter = new Reporter(path, bikeFilename, generalFilename, usageFilename)
    val writer = Writer
    val list = reader.readFile(sourceFilenamesListWithPath)
    writer.write(reporter.generateReports(DateRange("2010-09-20 12:26:08", "2010-10-26 12:26:08"), list))
  }

  def main(args: Array[String]): Unit = {
    val start = System.nanoTime
    execute()
    val end = System.nanoTime
    print((end - start) / 1000000)
  }
}
