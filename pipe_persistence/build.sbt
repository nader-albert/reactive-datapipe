name := "pipe_source"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Spray" at "http://repo.spray.io"

val akkaV = "2.3.9"
val sprayV = "1.2.0"

libraryDependencies ++= {
Seq(
  "io.reactivex"        %%  "rxscala"        % "0.24.1",
  "com.wandoulabs.akka" %%  "spray-websocket" % "0.1.3",
  "org.reactivemongo"   %% "reactivemongo" % "0.11.7",
  "com.typesafe.akka"   %%  "akka-actor"      % akkaV,
  "com.typesafe.akka"   %%  "akka-cluster"    % akkaV,
  "com.typesafe.akka"   %%  "akka-testkit"    % akkaV   % "test",
  "org.scalatest"       %%  "scalatest"      % "2.2.4" % "test") //double % means get me the source files suffixed with the correct scala version
}
