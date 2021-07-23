package util

import entity.DriveInfo

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.immutable.List
import scala.io.Source

class Reader {
  def readFile(fileName: String): List[DriveInfo] = {
    val source = Source.fromFile(fileName)
    val lines: List[DriveInfo] =
      (for (line <- source.getLines.drop(1))
        yield {
          readLine(line) match {
            case Some(value) => value
            case None => new DriveInfo(Int.MinValue, LocalDateTime.MIN, LocalDateTime.MIN, Int.MinValue, "", Int.MinValue, "", "", "")
          }
        }).toList
    source.close()
    lines
  }

  private def readLine(line: String): Option[DriveInfo] = {
    try {
      val iterator: Iterator[String] = line.split(",").iterator
      val driveInfo = Some(
        new DriveInfo(
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
        throw new Exception("Data is not valid")
    } catch {
      case e: Exception => None
    }
  }

  private def validateDriveInfo(driveInfo: DriveInfo): Boolean = {
    if (!driveInfo.getBikeNumber.matches("W[0-9]{5}")) return false
    if (driveInfo.getStartDate.isAfter(driveInfo.getEndDate)) return false
    if (!List("Member", "Casual").contains(driveInfo.getMemberType)) return false
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