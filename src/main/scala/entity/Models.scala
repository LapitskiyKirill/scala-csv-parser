package entity

import java.time.LocalDateTime

case class Report(filename: String, report: String)

case class DriveInfo(
                      duration: Int,
                      startDate: LocalDateTime,
                      endDate: LocalDateTime,
                      startStationNumber: Int,
                      startStation: String,
                      endStationNumber: Int,
                      endStation: String,
                      bikeNumber: String,
                      memberType: String
                    )

case class DateRange(
                      startDate: String,
                      endDate: String
                    )

case class BikeReport(
                       bikeNumber: String,
                       countOfDrives: Int,
                       totalDuration: Int
                     )

case class GeneralReport(
                          countOfDrives: Int,
                          countOfParseErrors: Int,
                          countOfUsagesBetweenDates: Int,
                          countOfBicyclesUsedBetweenDates: Int,
                          longestDrive: Int
                        )

case class UsageReport(
                        monthName: String,
                        countOfDrives: Int
                      )