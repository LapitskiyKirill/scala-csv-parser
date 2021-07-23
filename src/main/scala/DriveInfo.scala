import java.time.LocalDateTime

class DriveInfo(
                 duration: Int,
                 startDate: LocalDateTime,
                 endDate: LocalDateTime,
                 startStationNumber: Int,
                 startStation: String,
                 endStationNumber: Int,
                 endStation: String,
                 bikeNumber: String,
                 memberType: String
               ) {


  override def equals(o: Any): Boolean = {
    o match {
      case other: DriveInfo =>
        this.duration.equals(other.getDuration) &&
          this.startDate.equals(other.getStartDate) &&
          this.endDate.equals(other.getEndDate) &&
          this.startStationNumber.equals(other.getStartStationNumber) &&
          this.startStation.equals(other.getStartStation) &&
          this.endStationNumber.equals(other.getEndStationNumber) &&
          this.endStation.equals(other.getEndStation) &&
          this.bikeNumber.equals(other.getBikeNumber) &&
          this.memberType.equals(other.getMemberType)
      case _ => false
    }
  }

  def getDuration: Int = {
    duration
  }

  def getStartDate: LocalDateTime = {
    startDate
  }

  def getEndDate: LocalDateTime = {
    endDate
  }

  def getStartStationNumber: Int = {
    startStationNumber
  }

  def getStartStation: String = {
    startStation
  }

  def getEndStationNumber: Int = {
    endStationNumber
  }

  def getEndStation: String = {
    endStation
  }

  def getBikeNumber: String = {
    bikeNumber
  }

  def getMemberType: String = {
    memberType
  }
}
