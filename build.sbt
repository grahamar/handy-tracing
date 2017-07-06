organization := "io.grhodes.handy"
name := "handy-tracing"
version := "git describe --tags --dirty --always".!!.stripPrefix("v").trim

scalaVersion := "2.11.11"

crossScalaVersions := Seq(scalaVersion.value, "2.10.6")

libraryDependencies ++= Seq(
  "com.gilt" %% "gfc-logging" % "0.0.8",
  "com.gilt" %% "gfc-concurrent" % "0.3.6",
  "com.typesafe.play" %% "play" % "2.5.15" % Provided,
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Provided,
  "com.amazonaws" % "aws-xray-recorder-sdk-aws-sdk" % "1.1.2" % Provided
)

javacOptions in doc := Seq("-encoding", "UTF-8")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
homepage := Some(url("https://github.com/grahamar/handy-tracing"))
pomExtra := <scm>
    <url>git@github.com:grahamar/handy-tracing.git</url>
    <connection>scm:git:git@github.com:grahamar/handy-tracing.git</connection>
  </scm>
  <developers>
    <developer>
      <id>grhodes</id>
      <name>Graham Rhodes</name>
      <url>https://github.com/grahamar</url>
    </developer>
  </developers>
