import sbt._

object Dependencies {
  lazy val Core = Seq(
    // Actors
    "com.typesafe.akka" %% "akka-actor" % "2.4.17",
    "com.typesafe.akka" %% "akka-slf4j" % "2.4.17",
    "com.typesafe.akka" %% "akka-testkit" % "2.4.17",
    "com.typesafe.akka" %% "akka-http" % "10.0.6",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.6",
    "com.typesafe.akka" %% "akka-http-testkit" % "10.0.6",

    // Unit tests
    "org.scalactic" %% "scalactic" % "3.0.3",
    "org.scalatest" %% "scalatest" % "3.0.3" % "test",

    // Serialization
    "org.scodec" %% "scodec-bits" % "1.1.4",
    "org.scodec" %% "scodec-core" % "1.10.3",
    "org.scodec" %% "scodec-akka" % "0.3.0",

    // Cryptography
    "org.bouncycastle" % "bcprov-jdk15on" % "1.56",

    // Reflection (service discovery)
    "io.github.lukehutch" % "fast-classpath-scanner" % "2.0.19",
    "org.scala-lang" % "scala-reflect" % "2.12.2",

    // Database interface
    "org.scalikejdbc" %% "scalikejdbc" % "2.5.2",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.postgresql" % "postgresql" % "42.0.0",
    "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "2.5.2",

    // Database migration
    "org.flywaydb" % "flyway-core" % "4.2.0",

    // Configuration loading
    "com.github.pureconfig" %% "pureconfig" % "0.7.0",

    // Shapeless
    "com.chuusai" %% "shapeless" % "2.3.2"
  )
}

