import sbt.Keys._
import sbt._

object Common {
  val _scalaVersion = "2.12.4"

  val _scalacOptions = Seq(
    "-feature",
    "-deprecation",
    "-language:postfixOps",
    "-language:implicitConversions",
    // TODO: re-enable this on scalac fixes this issue
//    "-Xcheckinit", // Should be removed for production use
    // Found this list on tpolecat's github site
    "-deprecation",
    "-encoding", "UTF-8", // yes, this is 2 args
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
//    "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
//    "-Ywarn-numeric-widen", // This is annoying af
//    "-Ywarn-value-discard",
    "-Xfuture",
    "-Ywarn-unused-import" // 2.11 only

  )

  val Settings = Seq(
    version := "0.1",
    scalacOptions ++= _scalacOptions,
    scalaVersion := _scalaVersion
  )
}
