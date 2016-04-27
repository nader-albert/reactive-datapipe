name := "pipe_processor"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Spray" at "http://repo.spray.io"

val akkaV = "2.3.9"
val sprayV = "1.2.0"

libraryDependencies ++= {
Seq(
  "io.spray"            %%  "spray-json"      % "1.3.2",
  "io.spray"            %%  "spray-can"       % "1.3.3",
  "io.spray"            %%  "spray-routing"   % "1.3.3",
  "io.reactivex"        %%  "rxscala"         % "0.24.1",
  "org.reactivemongo"   %%  "reactivemongo"   % "0.11.7",
  "com.wandoulabs.akka" %%  "spray-websocket" % "0.1.3",
  "org.apache.spark"    %%  "spark-core"      % "1.5.0",
  "org.apache.spark"    %%  "spark-streaming" % "1.5.0",
  "com.typesafe.akka"   %%  "akka-actor"      % akkaV,
  "com.typesafe.akka"   %%  "akka-cluster"    % akkaV,
  "com.typesafe.akka"   %%  "akka-testkit"    % akkaV   % "test",
  "com.typesafe.akka"   %%  "akka-testkit"    % akkaV,
  "com.typesafe.akka"   %%  "akka-persistence-experimental" % akkaV,
  "org.scalatest"       %%  "scalatest"       % "2.2.4" % "test")
}