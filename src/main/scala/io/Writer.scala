package io

import entity.Report

import java.io.PrintWriter

object Writer {
  def write(info: List[Report]): Boolean = {
    info.foreach(report =>
      new PrintWriter(report.filename) {
        write(report.report)
        close()
      })
    true
  }


}