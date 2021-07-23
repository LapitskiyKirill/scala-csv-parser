package util

import entity.DriveInfo

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.immutable.List
import scala.io.Source

object Reader {
  def readFile(fileName: String): List[Option[DriveInfo]] = {
    val source = Source.fromFile(fileName)
    val lines: List[Option[DriveInfo]] = source.getLines.drop(1).map(line => readLine(line)).toList
    source.close()
    lines
  }

  private def readLine(line: String): Option[DriveInfo] = {
    try {
      val iterator: Iterator[String] = line.split(",").iterator
      val driveInfo = Some(
        DriveInfo(
          deleteQuotes(iterator.next()).toInt,
          parseTime(deleteQuotes(iterator.next())),
          parseTime(deleteQuotes(iterator.next())),
          deleteQuotes(iterator.next()).toInt,
          deleteQuotes(iterator.next()),
          deleteQuotes(iterator.next()).toInt,
          deleteQuotes(iterator.next()),
          deleteQuotes(iterator.next()),
          deleteQuotes(iterator.next())
        )
      )
      if (validateDriveInfo(driveInfo.value)) {
        driveInfo
      } else
        Option.empty
    } catch {
      case e: Exception => Option.empty
    }
  }

  private def validateDriveInfo(driveInfo: DriveInfo): Boolean = {
    driveInfo match {
      case DriveInfo(_, startDate, endDate, _, _, _, _, bikeNumber, memberType) =>
        if (!bikeNumber.matches("W[0-9]{5}") || startDate.isAfter(endDate) || !List("Member", "Casual").contains(memberType))
          return false
    }
    true
  }

  private def deleteQuotes(value: String): String = {
    value.replaceAll("\"", "")
  }

  private def parseTime(dateTime: String): LocalDateTime = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime.parse(dateTime, formatter);
  }
}