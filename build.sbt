import snapshot4s.BuildInfo.snapshot4sVersion

val scala3Version = "3.5.2"

lazy val root = project
  .in(file("."))
  .enablePlugins(Snapshot4sPlugin)
  .settings(
    name := "conduktor-ta",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    Compile / run / fork := true,
    libraryDependencies ++= List(l.kafkaClients, l.catsEffect3, l.avro) ++ l.munit ++ l.http4s ++ l.circe ++ l.logging
  )

lazy val l =
  new {
    val kafkaClients = "org.apache.kafka" % "kafka-clients" % v.Kafka
    val catsEffect3 = "org.typelevel" %% "cats-effect" % v.CatsEffect3
    val avro = "org.apache.avro" % "avro" % v.Avro

    val munit = Seq(
      "org.scalameta" %% "munit" % v.Munit % Test,
      "org.typelevel" %% "munit-cats-effect"  % v.MunitCatsEffect % Test,
      "com.siriusxm" %% "snapshot4s-munit" % snapshot4sVersion % Test,
    )
    val http4s = Seq(
      "org.http4s" %% "http4s-ember-server" % v.Https4sVersion,
      "org.http4s" %% "http4s-ember-client" % v.Https4sVersion,
      "org.http4s" %% "http4s-circe"        % v.Https4sVersion,
      "org.http4s" %% "http4s-dsl"          % v.Https4sVersion,
    )
    val circe = Seq(
      "io.circe" %% "circe-core"    % v.Circe,
      "io.circe" %% "circe-parser"  % v.Circe,
      "io.circe" %% "circe-generic" % v.Circe,
    )
    val logging = Seq(
      "ch.qos.logback"              % "logback-classic" % "1.5.12",
      "org.slf4j"                   % "slf4j-api"       % "2.0.16",
    )
  }

lazy val v =
  new {
    val Avro = "1.11.4"
    val Kafka = "3.9.0"
    val CatsEffect3 = "3.5.6"
    val Munit = "1.0.2"
    val MunitCatsEffect = "2.0.0"
    val Https4sVersion = "0.23.27"
    val Circe = "0.14.7"
  }