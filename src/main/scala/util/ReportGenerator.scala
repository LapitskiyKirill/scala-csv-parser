package util

import entity.{DriveInfo, Report}

trait ReportGenerator {
  def generate(list: List[Option[DriveInfo]]): Report
}
