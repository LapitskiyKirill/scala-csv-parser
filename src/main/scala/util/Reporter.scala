package util

import entity.{DriveInfo, Report}

trait Reporter {
  def generate(list: List[Option[DriveInfo]]): Report
}
