import AssemblyKeys._

scalaVersion := "2.9.2"

name := "honeycomb"

version := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
   "com.typesafe.akka" % "akka-actor" % "2.0.4",
   "com.typesafe.akka" % "akka-slf4j" % "2.0.4",
   "com.typesafe.akka" % "akka-remote" % "2.0.4",
   "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime",
   "net.databinder" %% "unfiltered-netty-server" % "0.6.4",
   "net.databinder" %% "unfiltered-json" % "0.6.4",
   "net.databinder" %% "dispatch-nio" % "0.8.8"
)

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

assemblySettings

test in assembly := {}
