import AssemblyKeys._

name := "honeycomb"

version := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
   "com.typesafe.akka" % "akka-actor" % "2.0.4",
   "com.typesafe.akka" % "akka-slf4j" % "2.0.4",
   "com.typesafe.akka" % "akka-remote" % "2.0.4"
)

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

assemblySettings

test in assembly := {}
