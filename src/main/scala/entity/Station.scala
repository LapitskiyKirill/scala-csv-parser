package entity

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{Rep, Tag}

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

case class Station(
                    stationNumber: Int,
                    stationName: String,
                  )

class StationTable(tag: Tag) extends Table[Station](tag, "station") {
  def * = (stationNumber, stationName) <> (Station.tupled, Station.unapply)

  val stationNumber: Rep[Int] = column[Int]("station_number")
  val stationName: Rep[String] = column[String]("station_name")
}

class StationRepository(db: Database = Database.forConfig("postgres")) {

  def isStationNumberExists(stationNumber: Int): Future[Boolean] = {
    db.run(Tables.stations.filter(_.stationNumber === stationNumber).exists.result)
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
      case Failure(_) => Future.successful(0)
    }
  }

  def readAll(): Future[Seq[Station]] = {
    db.run[Seq[Station]](Tables.stations.result)
  }
}