import com.typesafe.sbt.SbtAspectj.{ Aspectj, aspectjSettings }
import com.typesafe.sbt.SbtAspectj.AspectjKeys._

name := "handy-tracing"

organization := "com.teambytes.handy"

scalaVersion := "2.11.5"

version in ThisBuild := "git describe --tags --always --dirty".!!.trim.replaceFirst("^v", "")

aspectjSettings

compileOnly in Aspectj :=  true

lintProperties in Aspectj += "invalidAbsoluteTypeName = ignore"

ivyConfigurations += Aspectj

licenses := Seq("Apache License 2.0" -> url("http://opensource.org/licenses/Apache-2.0"))

homepage := Some(url("https://github.com/grahamar/handy-tracing"))

pomExtra := <scm>
    <url>git@github.com:grahamar/handy-dynamo.git</url>
    <connection>scm:git:git@github.com:grahamar/handy-dynamo.git</connection>
  </scm>
  <developers>
    <developer>
      <id>grhodes</id>
      <name>Graham Rhodes</name>
      <url>https://github.com/grahamar</url>
    </developer>
  </developers>

libraryDependencies ++= Seq(
  "org.aspectj" % "aspectjtools" % (aspectjVersion in Aspectj).value % Aspectj.name,
  "org.aspectj" % "aspectjweaver" % (aspectjVersion in Aspectj).value % Aspectj.name,
  "org.aspectj" % "aspectjrt" % (aspectjVersion in Aspectj).value % Aspectj.name,
  "com.gilt" %% "gfc-logging" % "0.0.2",
  "com.gilt" %% "gfc-concurrent" % "0.1.0",
  "com.typesafe.play" %% "play" % "2.3.9" % "provided",
  "ch.qos.logback" % "logback-classic" % "1.1.1" % "provided"
)

