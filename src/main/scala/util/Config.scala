package util

import com.typesafe.config.{Config, ConfigFactory}

object Config {
  val config: Config = ConfigFactory.load
  val path: String = config.getString("filename.path")
  val sourceFilenames: String = config.getString("filename.source")
  val sourceFilenamesList: List[String] = sourceFilenames.split(",").toList
  val sourceFilenamesListWithPath: List[String] = sourceFilenamesList.map(path + _)
  val bikeFilename: String = config.getString("filename.bike")
  val generalFilename: String = config.getString("filename.general")
  val usageFilename: String = config.getString("filename.usage")
}
