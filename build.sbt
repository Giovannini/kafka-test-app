
lazy val commonSettings = Seq(
  name := "kafkapp",
  version := "1.0",
  scalaVersion := "2.11.7",
  scalacOptions in Compile ++= Seq(
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xlint",
    "-Xfuture",
    "-Ywarn-dead-code",
    "-Ywarn-unused-import",
    "-Ywarn-unused",
    "-Ywarn-nullary-unit"
  ),
  scalacOptions in(Compile, doc) ++= Seq("-groups", "-implicits"),
  javacOptions in(Compile, doc) ++= Seq("-notimestamp", "-linksource"),
  parallelExecution in Test := false,
  parallelExecution in IntegrationTest := true,
  unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")
)

lazy val `kafkapp` = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.1",
      "org.apache.kafka" % "kafka-clients" % "0.10.2.0",
      "org.slf4j" % "slf4j-api" % "1.7.24",
      "com.typesafe.akka" %% "akka-actor" % "2.4.17",
      "com.typesafe.akka" %% "akka-stream-kafka" % "0.15",
      //Test deps
      "org.slf4j" % "log4j-over-slf4j" % "1.7.24" % "test",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "ch.qos.logback" % "logback-classic" % "1.1.3" % "test",
      "com.typesafe.akka" %% "akka-testkit" % "2.4.17" % "test",
      "com.typesafe.akka" %% "akka-slf4j" % "2.4.17" % "test"
    )
  )
