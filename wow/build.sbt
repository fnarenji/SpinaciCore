name := "wow"
version := "0.1"

// Disable log buffering for faster output
logBuffered in Test := false

run := run in Compile in core

lazy val macros = (project in file("macros"))
    .settings(Common.Settings)
    .settings(libraryDependencies ++= Dependencies.Core)

lazy val core = (project in file("core"))
    .settings(Common.Settings)
    .settings(libraryDependencies ++= Dependencies.Core)
    .dependsOn(macros)

