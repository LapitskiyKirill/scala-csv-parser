package entity

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{Rep, TableQuery, Tag}

import java.time.LocalDateTime

object Tables {
  val stations = TableQuery[StationTable]
  val drives = TableQuery[DriveTable]
}

object DataBase {
  val db = Database.forConfig("postgres")
}

class StationTable(tag: Tag) extends Table[Station](tag, "station") {
  def * = (stationNumber, stationName) <> (Station.tupled, Station.unapply)

  val stationNumber: Rep[Int] = column[Int]("station_number")
  val stationName: Rep[String] = column[String]("station_name")
}

class DriveTable(tag: Tag) extends Table[Drive](tag, "drive") {
  def * = (
    duration,
    startDate,
    endDate,
    startStationNumber,
    endStationNumber,
    bikeNumber,
    memberType) <> (Drive.tupled, Drive.unapply)

  val duration: Rep[Int] = column[Int]("duration")
  val startDate: Rep[LocalDateTime] = column[LocalDateTime]("start_date")
  val endDate: Rep[LocalDateTime] = column[LocalDateTime]("end_date")
  val startStationNumber: Rep[Int] = column[Int]("start_station_number")
  val endStationNumber: Rep[Int] = column[Int]("end_station_number")
  val bikeNumber: Rep[String] = column[String]("bike_number")
  val memberType: Rep[String] = column[String]("member_type")

  val startStation = foreignKey("drive_start_station_fk", startStationNumber, Tables.stations)(_.stationNumber)
  val endStation = foreignKey("drive_end_station_fk", endStationNumber, Tables.stations)(_.stationNumber)
}

