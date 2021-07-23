package entity

import java.time.LocalDateTime

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