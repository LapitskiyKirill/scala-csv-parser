package entity

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{Rep, Tag}

import java.time.LocalDateTime
import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

case class Drive(
                  duration: Int,
                  startDate: LocalDateTime,
                  endDate: LocalDateTime,
                  startStation: Int,
                  endStation: Int,
                  bikeNumber: String,
                  memberType: String
                )

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


class DriveRepository(db: Database = Database.forConfig("postgres")) {

  def readAll(): Future[Seq[Drive]] = {
    db.run[Seq[Drive]](Tables.drives.result)
  }

  def insertAll(drives: List[Drive]): Future[Option[Int]] = {
    val trySave = Try {
      val insertDrivesQuery = Tables.drives ++= drives
      db.run(insertDrivesQuery).map(_.map(b => b))
    }
    trySave match {
      case Success(v) => v
      case Failure(_) => Future.successful(Option(0))
    }
  }
}