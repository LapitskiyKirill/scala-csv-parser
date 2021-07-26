package validator

import entity.DriveInfo

import scala.collection.immutable.List

object DriveValidator extends Validator {

  def validate(driveInfo: Any): Boolean = {
    driveInfo.asInstanceOf[DriveInfo] match {
      case DriveInfo(_, startDate, endDate, _, _, _, _, bikeNumber, memberType) =>
        if (!bikeNumber.matches("W[0-9]{5}") || startDate.isAfter(endDate) || !List("Member", "Casual").contains(memberType))
          return false
    }
    true
  }
}
