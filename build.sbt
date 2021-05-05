name := "generic-bot-scala"

version := "0.1"
scalaVersion := "2.13.5"
organization := "work.arudenko"

val circeVersion = "0.12.3"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)


libraryDependencies ++= Seq(
  "work.arudenko" %% "markov-chain" % "0.1-SNAPSHOT",
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.0",
  "org.augustjune" %% "canoe" % "0.5.0",
  "com.github.pathikrit" %% "better-files" % "3.9.1",
  "com.chuusai" %% "shapeless" % "2.3.3",
  "com.github.scopt" %% "scopt" % "4.0.0-RC2",
  "ch.qos.logback" % "logback-classic" % "1.2.3" ,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "com.github.pureconfig" %% "pureconfig" % "0.14.0",
  "org.scalatest" %% "scalatest" % "3.2.0" % Test,
)


assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

assembly / mainClass := Some("work.arudenko.bot.ConsoleRunner")


