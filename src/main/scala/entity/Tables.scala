package entity

import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery

object Tables {
  val stations = TableQuery[StationTable]
  val drives = TableQuery[DriveTable]
}

object DataBase {
  val db = Database.forConfig("postgres")
}
