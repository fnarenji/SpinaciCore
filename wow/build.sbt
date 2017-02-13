name := "wow"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.4.16"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.4.16"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

libraryDependencies += "org.scodec" % "scodec-bits_2.12" % "1.1.4"
libraryDependencies += "org.scodec" % "scodec-core_2.12" % "1.10.3"
libraryDependencies += "org.scodec" % "scodec-akka_2.12" % "0.3.0"

logBuffered in Test := false
