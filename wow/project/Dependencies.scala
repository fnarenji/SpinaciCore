import sbt._

object Dependencies {
  lazy val Core = Seq(
    // Actors
    "com.typesafe.akka" %% "akka-actor" % "2.5.8",
    "com.typesafe.akka" %% "akka-slf4j" % "2.5.8",
    "com.typesafe.akka" %% "akka-testkit" % "2.5.8",
    "com.typesafe.akka" %% "akka-http" % "10.0.11",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.11",
    "com.typesafe.akka" %% "akka-http-testkit" % "10.0.11",

    // Unit tests
    "org.scalactic" %% "scalactic" % "3.0.4",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test",

    // Serialization
    "org.scodec" %% "scodec-bits" % "1.1.5",
    "org.scodec" %% "scodec-core" % "1.10.3",
    "org.scodec" %% "scodec-akka" % "0.3.0",

    // Cryptography
    "org.bouncycastle" % "bcprov-jdk15on" % "1.58",

    // Reflection (service discovery)
    "io.github.lukehutch" % "fast-classpath-scanner" % "2.9.4",
    "org.scala-lang" % "scala-reflect" % "2.12.4",

    // Database interface
    "org.scalikejdbc" %% "scalikejdbc" % "3.1.0",
    "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "3.1.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.postgresql" % "postgresql" % "42.1.4",

    // Database migration
    "org.flywaydb" % "flyway-core" % "5.0.3",

    // Configuration loading
    "com.github.pureconfig" %% "pureconfig" % "0.8.0",

    // Shapeless
    "com.chuusai" %% "shapeless" % "2.3.2"
  )
}

