import util.{Reader, ReportGenerator, Writer}
import com.typesafe.config.ConfigFactory

object Main {

  def execute(): Unit = {
    val config = ConfigFactory.load
    val sourceFilename = config.getString("filename.source")
    val bikeFilename = config.getString("filename.bike")
    val generalFilename = config.getString("filename.general")
    val usageFilename = config.getString("filename.usage")
    val path = config.getString("filename.path")
    val reader = Reader
    val reportGenerator = new ReportGenerator(path, bikeFilename, generalFilename, usageFilename)
    val writer = Writer
    val list = reader.readFile(path + sourceFilename)
    writer.write(reportGenerator.generateReports("2010-09-20 12:26:08", "2010-10-26 12:26:08", list))
  }

  def main(args: Array[String]): Unit = {
    val start = System.nanoTime
    execute()
    val end = System.nanoTime
    print((end - start) / 1000000000)
  }
}
