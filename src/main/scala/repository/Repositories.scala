package repository

import entity._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object StationRepository {

  def isStationNumberExists(stationNumber: Int): Future[Boolean] = {
    DataBase.db.run(Tables.stations.filter(_.stationNumber === stationNumber).exists.result)
  }

  def insert(station: Station): Future[Int] = {
    val trySave = Try {
      val exists = DataBase.db.run(Tables.stations.filter(_.stationNumber === station.stationNumber).exists.result)
      exists.map(res => {
        if (!res) {
          val insertStationQuery = Tables.stations += station
          DataBase.db.run(insertStationQuery)
        } else {
          Future(0)
        }
      }).flatten
    }
    trySave match {
      case Success(v) => v
      case Failure(_) => Future(0)
    }
  }

  def readAll(): Future[Seq[Station]] = {
    DataBase.db.run[Seq[Station]](Tables.stations.result)
  }
}

object DriveRepository {

  def readAll(): Future[Seq[Drive]] = {
    DataBase.db.run[Seq[Drive]](Tables.drives.result)
  }

  def insertAll(drives: List[Drive]): Future[Option[Int]] = {
    val insertDrivesQuery = Tables.drives ++= drives
    DataBase.db.run(insertDrivesQuery).map(c => c.map(b => b))
  }
}