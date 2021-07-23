package util

import java.io.PrintWriter

class Writer {
  def write(info: Array[(String, String)]): Unit = {
    for (report <- info) {
      new PrintWriter(report._1) {
        write(report._2);
        close()
      }
    }
  }
}