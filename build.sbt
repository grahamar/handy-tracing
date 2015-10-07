import com.typesafe.sbt.SbtAspectj.{ Aspectj, aspectjSettings }
import com.typesafe.sbt.SbtAspectj.AspectjKeys._
import sbtrelease._
import ReleaseStateTransformations._

name := "handy-tracing"

organization := "com.teambytes.handy"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.11.5", "2.10.4")

ReleaseKeys.crossBuild := true

aspectjSettings

compileOnly in Aspectj :=  true

lintProperties in Aspectj += "invalidAbsoluteTypeName = ignore"

ivyConfigurations += Aspectj

publishArtifact in Test := false

publishMavenStyle := true

pomIncludeRepository := { _ => false }

licenses := Seq("Apache License 2.0" -> url("http://opensource.org/licenses/Apache-2.0"))

homepage := Some(url("https://github.com/grahamar/handy-tracing"))

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

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

sbtrelease.ReleasePlugin.ReleaseKeys.releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts.copy(action = publishSignedAction),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

lazy val publishSignedAction = { st: State =>
  val extracted = Project.extract(st)
  val ref = extracted.get(thisProjectRef)
  extracted.runAggregated(com.typesafe.sbt.pgp.PgpKeys.publishSigned in Global in ref, st)
}

libraryDependencies ++= Seq(
  "org.aspectj" % "aspectjtools" % (aspectjVersion in Aspectj).value % Aspectj.name,
  "org.aspectj" % "aspectjweaver" % (aspectjVersion in Aspectj).value % Aspectj.name,
  "org.aspectj" % "aspectjrt" % (aspectjVersion in Aspectj).value % Aspectj.name,
  "com.gilt" %% "gfc-logging" % "0.0.2",
  "com.gilt" %% "gfc-concurrent" % "0.1.0",
  "com.typesafe.play" %% "play" % "2.3.9" % "provided",
  "ch.qos.logback" % "logback-classic" % "1.1.1" % "provided"
)

