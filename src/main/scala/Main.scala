object Main {
  def main(args: Array[String]): Unit = {
    val fileName = "2010-capitalbikeshare-tripdata.csv"
    val path = "src/main/resources/"
    val reader = new Reader
    val reportGenerator = new ReportGenerator(path, "bike-stats.csv", "general-stats.csv", "usage-stats.csv")
    val writer = new Writer
    val list = reader.readFile(path + fileName)
    writer.write(reportGenerator.generateReports("2010-09-20 12:26:08", "2010-10-26 12:26:08", list))
  }
}
