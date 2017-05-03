import sbt._
import Keys._

object Common {
  val _scalaVersion = "2.12.2"

  val _scalacOptions = Seq(
    "-feature",
    "-deprecation",
    "-language:postfixOps",
    "-language:implicitConversions"
  )
  
  val Settings = Seq(
    version := "0.1", 
    scalacOptions ++= _scalacOptions,
    scalaVersion := _scalaVersion
  )
}
