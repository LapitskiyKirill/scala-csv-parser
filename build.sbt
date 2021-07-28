import sbt.Keys.libraryDependencies

name := "scala-csv-reader"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies += "com.typesafe" % "config" % "1.4.1"
libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.10.3"
libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.0"
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.3"
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.23"
libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.31"
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"