package util

import java.io.PrintWriter

object Writer {
  def write(info: Array[(String, String)]): Unit = {
    info.foreach(f = report =>
      new PrintWriter(report._1) {
        write(report._2)
        close()
      })
  }
}