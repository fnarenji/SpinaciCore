name := "wow"
version := "0.1"
organizationName := "SpinaciCore"
organizationHomepage := Some(url("https://github.com/SKNZ/SpinaciCore"))
licenses += "MIT License" -> url("http://opensource.org/licenses/MIT")

// Disable log buffering for faster output
logBuffered in Test := false

run := run in Compile in core

lazy val macros = (project in file("macros"))
  .settings(
    Common.Settings,
    libraryDependencies ++= Dependencies.Core
  )

lazy val core = (project in file("core"))
  .settings(
    Common.Settings,
    libraryDependencies ++= Dependencies.Core
  )
  .dependsOn(macros)

