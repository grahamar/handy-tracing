import com.typesafe.sbt.SbtAspectj.{ Aspectj, aspectjSettings }
import com.typesafe.sbt.SbtAspectj.AspectjKeys._

organization := "io.grhodes.handy"
name := "handy-tracing"
version := "git describe --tags --dirty --always".!!.stripPrefix("v").trim

scalaVersion := "2.11.11"

crossScalaVersions := Seq(scalaVersion.value, "2.10.6")

libraryDependencies ++= Seq(
  "org.aspectj" % "aspectjtools" % (aspectjVersion in Aspectj).value % Aspectj.name,
  "org.aspectj" % "aspectjweaver" % (aspectjVersion in Aspectj).value % Aspectj.name,
  "org.aspectj" % "aspectjrt" % (aspectjVersion in Aspectj).value % Aspectj.name,
  "com.gilt" %% "gfc-logging" % "0.0.8",
  "com.gilt" %% "gfc-concurrent" % "0.3.6",
  "com.typesafe.play" %% "play" % "2.5.15" % "provided",
  "ch.qos.logback" % "logback-classic" % "1.1.1" % "provided"
)

aspectjSettings
ivyConfigurations += Aspectj
compileOnly in Aspectj :=  true
lintProperties in Aspectj += "invalidAbsoluteTypeName = ignore"
javacOptions in doc := Seq("-encoding", "UTF-8")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
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
