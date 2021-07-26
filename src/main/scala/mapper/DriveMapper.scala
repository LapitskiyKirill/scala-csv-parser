package mapper

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DriveMapper extends Mapper {

  def map(iterator: Iterator[String]) = {
    (deleteQuotes(iterator.next()).toInt,
      parseTime(deleteQuotes(iterator.next())),
      parseTime(deleteQuotes(iterator.next())),
      deleteQuotes(iterator.next()).toInt,
      deleteQuotes(iterator.next()),
      deleteQuotes(iterator.next()).toInt,
      deleteQuotes(iterator.next()),
      deleteQuotes(iterator.next()),
      deleteQuotes(iterator.next())).productIterator
  }

  private def parseTime(dateTime: String): LocalDateTime = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime.parse(dateTime, formatter);
  }

  private def deleteQuotes(value: String): String = {
    value.replaceAll("\"", "")
  }
}
