import sbt._
import Keys._

object Dependencies {
  lazy val Core = Seq(
    "org.scala-lang"       %   "scala-reflect"           %  "2.12.2",
    "com.typesafe.akka"    %%  "akka-actor"              %  "2.4.17",
    "com.typesafe.akka"    %%  "akka-slf4j"              %  "2.4.17",
    "com.typesafe.akka"    %%  "akka-testkit"            %  "2.4.17",
    "com.typesafe.akka"    %%  "akka-http"               %  "10.0.6",
    "com.typesafe.akka"    %%  "akka-http-spray-json"    %  "10.0.6",
    "com.typesafe.akka"    %%  "akka-http-testkit"       %  "10.0.6",
    "org.scalactic"        %%  "scalactic"               %  "3.0.3",
    "org.scalatest"        %%  "scalatest"               %  "3.0.3"    %  "test",
    "org.scodec"           %%  "scodec-bits"             %  "1.1.4",
    "org.scodec"           %%  "scodec-core"             %  "1.10.3",
    "org.scodec"           %%  "scodec-akka"             %  "0.3.0",
    "org.bouncycastle"     %   "bcprov-jdk15on"          %  "1.56",
    "io.github.lukehutch"  %   "fast-classpath-scanner"  %  "2.0.19"
  )
}

