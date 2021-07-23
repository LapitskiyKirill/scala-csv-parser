package util

import scala.collection.immutable.List
import scala.io.Source

class SimpleReader {
  def readFile(fileName: String): List[Iterator[String]] = {
    val source = Source.fromFile(fileName)
    val lines: List[Iterator[String]] =
      (for (line <- source.getLines.drop(1))
        yield {
          readLine(line) match {
            case Some(value) => value
            case None => Iterator.empty
          }
        }).toList
    source.close()
    lines
  }

  private def readLine(line: String): Option[Iterator[String]] = {
    try {
      Some(line.split(",").iterator)
    } catch {
      case e: Exception => None
    }
  }
}
