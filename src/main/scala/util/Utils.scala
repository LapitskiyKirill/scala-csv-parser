package util

import entity.{DataBase, Drive, DriveInfo, Station, Tables}
import repository.StationRepository

import java.time.LocalDateTime
import scala.collection.immutable.{List, Seq}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object Utils {
  def getStatisticsWithNoErrorLines(list: Future[List[Option[DriveInfo]]]): Future[List[Option[DriveInfo]]] = {
    list.map(_.filterNot(_.isEmpty))
  }

  def isDateBetweenTwoAnother(date: LocalDateTime, startDate: LocalDateTime, endDate: LocalDateTime): Boolean = {
    (date.isAfter(startDate) && date.isBefore(endDate)) ||
      (date.isAfter(startDate) && date.isBefore(endDate))
  }

  def mapToDriveInfo(drives: Future[Seq[Drive]], stations: Future[Seq[Station]]): Future[List[Option[DriveInfo]]] = {
    stations.map(stations =>
      drives.map(_.map(drive => {
        Option(DriveInfo(
          drive.duration,
          drive.startDate,
          drive.endDate,
          drive.startStation,
          stations.filter(_.stationNumber == drive.startStation).head.stationName,
          drive.endStation,
          stations.filter(_.stationNumber == drive.startStation).head.stationName,
          drive.bikeNumber,
          drive.memberType
        ))
      }).toList
      )
    ).flatten
  }

  def mapToDrive(lines: List[Option[DriveInfo]]): List[Drive] = {
    lines.flatMap(a => Option.option2Iterable(a)
      .map(line =>
        Drive(
          line.duration,
          line.startDate,
          line.endDate,
          line.startStationNumber,
          line.endStationNumber,
          line.bikeNumber,
          line.memberType
        ),
      )
    )
  }

  def mapToStation(lines: List[Option[DriveInfo]]): List[Station] = {
    lines.flatMap(a => Option.option2Iterable(a)
      .map(line => List(
        Station(line.startStationNumber, line.startStation),
        Station(line.endStationNumber, line.endStation)
      )
      ).toList.flatten
    ).distinctBy(_.stationNumber)
  }
}
