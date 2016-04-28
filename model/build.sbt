name := "model"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Spray" at "http://repo.spray.io"

val akkaV = "2.3.9"
val sprayV = "1.2.0"

libraryDependencies ++= {
Seq(
  "io.spray"            %%  "spray-json"     % "1.3.2",
  "io.spray"            %%  "spray-can"      % "1.3.3",
  "io.reactivex"        %%  "rxscala"        % "0.24.1",
  "org.scalatest"       %%  "scalatest"      % "2.2.4" % "test",
  "com.wandoulabs.akka" %%  "spray-websocket" % "0.1.3",
  "com.google.code.gson" % "gson" % "2.3",
  "joda-time" % "joda-time" % "2.9"
)}