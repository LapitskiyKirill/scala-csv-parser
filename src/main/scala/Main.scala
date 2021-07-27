import com.typesafe.config.ConfigFactory
import entity.{DateRange, DriveInfo, Report}
import io.{ParameterizedReader, Writer}
import mapper.DriveMapper
import util.Reporter
import validator.DriveValidator

import scala.collection.immutable.Seq
import scala.collection.parallel.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

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
    val reader = new ParameterizedReader[DriveInfo](DriveValidator, DriveMapper)
    val reporter = new Reporter(path, bikeFilename, generalFilename, usageFilename)
    val reportsMonad = processFile(reader, reporter, sourceFilenamesListWithPath)
    Await.ready(reportsMonad, 40000 millis)
    reportsMonad.map(reports => Writer.write(reports))
    Thread.sleep(1000)
  }

  def main(args: Array[String]): Unit = {
    val start = System.nanoTime
    execute()
    val end = System.nanoTime
    print((end - start) / 1000000)
  }

  def processFile(reader: ParameterizedReader[DriveInfo], reporter: Reporter, sourceFilenamesListWithPath: List[String]): Future[List[Report]] = {
    val result: Future[Seq[List[Option[DriveInfo]]]] = Future.sequence(
      sourceFilenamesListWithPath.par.map(
        sourceFilenameListWithPath => {
          Future {
            reader.readFile(sourceFilenameListWithPath)
          }
        }
      ).seq
    )
    result.map(_.flatten.toList).map(
      records => reporter.generateReports(DateRange("2010-09-20 12:26:08", "2010-10-26 12:26:08"), records)
    )
  }
}