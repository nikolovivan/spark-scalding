import sbt._
import sbt.Keys._
import net.virtualvoid.sbt.graph.Plugin._
import sbtassembly.Plugin._
import sbtassembly.Plugin.AssemblyKeys._

object ProjectBuild extends Build {
  val sharedSettings = Project.defaultSettings ++ graphSettings ++ Seq(
    organization := "com.tresata",
    version := "0.2-SNAPSHOT",
    scalaVersion := "2.10.4",
    javacOptions ++= Seq("-Xlint:unchecked", "-source", "1.6", "-target", "1.6"),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-target:jvm-1.6"),
    resolvers += "Concurrent Maven Repo" at "http://conjars.org/repo",
    publishMavenStyle := true,
    pomIncludeRepository := { x => false },
    publishArtifact in Test := false,
    publishTo <<= version { (v: String) =>
      if (v.trim.endsWith("SNAPSHOT"))
        Some("tresata-snapshots" at "http://server01:8080/archiva/repository/snapshots")
      else
        Some("tresata-releases"  at "http://server01:8080/archiva/repository/internal")
    },
    credentials += Credentials(Path.userHome / ".m2" / "credentials_internal"),
    credentials += Credentials(Path.userHome / ".m2" / "credentials_snapshots"),
    credentials += Credentials(Path.userHome / ".m2" / "credentials_proxy")
  )

  lazy val project = Project(
    id = "root",
    base = file("."),
    settings = sharedSettings ++ Seq(
      name := "spark-scalding",
      libraryDependencies ++= Seq(
        "com.typesafe" % "config" % "1.2.1" % "compile",
        "org.slf4j" % "slf4j-api" % "1.6.6" % "compile",
        "com.twitter" %% "scalding-core" % "0.11.1" % "compile",
        "org.apache.spark" %% "spark-core" % "1.1.0" % "provided",
        "org.slf4j" % "slf4j-log4j12" % "1.6.6" % "test",
        "org.scalatest" %% "scalatest" % "2.2.0" % "test"
      )
    )
  )

  lazy val demo = Project(
    id = "demo",
    base = file("demo"),
    settings = sharedSettings ++ assemblySettings ++ Seq(
      name := "spark-scalding-demo",
      libraryDependencies ++= Seq(
        "org.apache.spark" %% "spark-core" % "1.1.0" % "provided"
      ),
      mergeStrategy in assembly <<= (mergeStrategy in assembly) {
        (old) => {
          case s if s.endsWith(".class") => MergeStrategy.last
          case x => old(x)
        }
      },
      publish := { }
    )
  ).dependsOn(project)
}
