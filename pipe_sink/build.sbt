name := "pipe_sink"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Spray" at "http://repo.spray.io"

val akkaV = "2.3.9"
val sprayV = "1.2.0"

libraryDependencies ++= {
Seq(
  //"org.java-websocket"  %   "Java-WebSocket" % "1.3.1",
  "io.spray"            %%  "spray-json"     % "1.3.2",
  "io.spray"            %%  "spray-can"      % "1.3.3",
  "io.spray"            %%  "spray-routing"  % "1.3.3",
  "com.typesafe.akka"   %%  "akka-actor"      % akkaV,
  "com.typesafe.akka"   %%  "akka-cluster"    % akkaV,
//"com.typesafe.akka"   %%  "akka-persistence"  % akkaV,
  //"com.typesafe.akka"   %%  "akka-testkit"    % akkaV   % "test",
  "com.typesafe.akka"   %%  "akka-camel"    % "2.3.12",
  "io.reactivex"        %%  "rxscala"        % "0.24.1",
  "org.scalatest"       %%  "scalatest"      % "2.2.4" % "test",
  "com.wandoulabs.akka" %%  "spray-websocket" % "0.1.3",
  "com.twitter"   % "hbc-core"  % "2.2.0") //double % means get me the source files suffixed with the correct scala version
}