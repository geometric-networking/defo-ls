ThisBuild / name := "Defo-LS"
ThisBuild / version := "0.0.1"
ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file ("defo")).settings(
    assembly / mainClass := Some("main.Main")
)