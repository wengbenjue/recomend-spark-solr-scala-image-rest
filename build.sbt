import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

assemblySettings

jarName in assembly := "rest_recomend.jar"

test in assembly := {}

mainClass in assembly := Some("com.soledede.recomend.boot.Boot")

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case PathList("about.html") => MergeStrategy.rename
  case PathList("overview.html") => MergeStrategy.rename
  case "application.conf" => MergeStrategy.concat
  case x => old(x)
}
}

name := "rest_recomend"

version := "1.0"

scalaVersion := "2.10.4"

net.virtualvoid.sbt.graph.Plugin.graphSettings

libraryDependencies ++= Seq(
  "io.spray" % "spray-can" % "1.1-M8",
  "io.spray" % "spray-http" % "1.1-M8",
  "io.spray" % "spray-routing" % "1.1-M8",
  "net.liftweb" %% "lift-json" % "2.5.1",
  "com.typesafe.slick" %% "slick" % "1.0.1",
  "mysql" % "mysql-connector-java" % "5.1.25",
  "com.typesafe.akka" %% "akka-actor" % "2.1.4",
  "com.typesafe.akka" %% "akka-slf4j" % "2.1.4",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "org.apache.hbase" % "hbase-client" % "1.0.0-cdh5.4.7",
  ("org.apache.hbase" % "hbase-common" % "1.0.0-cdh5.4.7").
    exclude("commons-collections", "commons-collections"),
  ("org.apache.hadoop" % "hadoop-common" % "2.6.0-cdh5.4.7").
    exclude("commons-beanutils", "commons-beanutils").
    exclude("commons-beanutils", "commons-beanutils-core").
    exclude("org.slf4j", "slf4j-log4j12"),
  ("org.apache.hbase" % "hbase-server" % "1.0.0-cdh5.4.7").
    exclude("org.mortbay.jetty", "servlet-api-2.5").
    exclude("commons-collections", "commons-collections").
    exclude("org.mortbay.jetty", "jsp-2.1").
    exclude("org.mortbay.jetty", "jsp-api-2.1"),
  "com.typesafe.play" %% "play-json" % "2.3.0"
)

resolvers ++= Seq(
  "Spray repository" at "http://repo.spray.io",
  "cloudera-repo-releases" at "https://repository.cloudera.com/artifactory/repo/",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
)




