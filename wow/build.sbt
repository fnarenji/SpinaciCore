name := "wow"

version := "1.0"

scalaVersion := "2.12.1"

scalacOptions += "-feature"
scalacOptions += "-deprecation"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.4"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.4"
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.0.4"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

libraryDependencies += "org.scodec" % "scodec-bits_2.12" % "1.1.4"
libraryDependencies += "org.scodec" % "scodec-core_2.12" % "1.10.3"
libraryDependencies += "org.scodec" % "scodec-akka_2.12" % "0.3.0"

libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.56"

libraryDependencies += "org.clapper" %% "classutil" % "1.1.2"

logBuffered in Test := false
