name := "reactive-datapipe"

version := "1.0"

scalaVersion := "2.11.7"

lazy val pipe_source = project.in(file("pipe_source")).dependsOn(pipe_transformer)

lazy val pipe_transformer = project.in(file("pipe_transformer")).dependsOn(pipe_processor)

lazy val pipe_processor = project.in(file("pipe_processor")).dependsOn(pipe_sink)

lazy val pipe_persistence = project.in(file("pipe_persistence")).dependsOn(pipe_sink)

lazy val pipe_sink = project.in(file("pipe_sink")).dependsOn(model)

lazy val model = project.in(file("model"))

lazy val root = (project in file(".")).aggregate(pipe_source, pipe_transformer, pipe_processor, pipe_sink)