package io

import entity.DriveInfo

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.immutable.List
import scala.io.Source
import scala.util.{Failure, Success, Try}

object Reader {
  def readFile(fileNames: List[String]): List[Option[DriveInfo]] = {
    val fileName = fileNames(0)
    val source = Source.fromFile(fileName)
    val lines = source.getLines.drop(1).map(readLine).toList
    source.close()
    lines
  }

  private def readLine(line: String): Option[DriveInfo] = {
    val parse = Try({
      val iterator = line.split(",").iterator
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
      if (!validateDriveInfo(driveInfo.value)) {
        throw new Exception
      }
      driveInfo
    })
    parse match {
      case Success(v) =>
        v
      case Failure(e) =>
        Option.empty
      //    try {
      //      val iterator = line.split(",").iterator
      //      val driveInfo = Some(
      //        DriveInfo(
      //          deleteQuotes(iterator.next()).toInt,
      //          parseTime(deleteQuotes(iterator.next())),
      //          parseTime(deleteQuotes(iterator.next())),
      //          deleteQuotes(iterator.next()).toInt,
      //          deleteQuotes(iterator.next()),
      //          deleteQuotes(iterator.next()).toInt,
      //          deleteQuotes(iterator.next()),
      //          deleteQuotes(iterator.next()),
      //          deleteQuotes(iterator.next())
      //        )
      //      )
      //      if (validateDriveInfo(driveInfo.value)) {
      //        driveInfo
      //      } else
      //        Option.empty
      //    } catch {
      //      case e: Exception => Option.empty
      //    }
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
