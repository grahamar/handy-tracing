# handy-tracing
Handy classes for instrumented request tracing

AspectjWeaver is needed at runtime for this so we can instrument scala.concurrent.Future and pass the ThreadLocal context to the thread running the Future.

Example Usage in a Play! application:

Global.scala

    override def doFilter(next: EssentialAction): EssentialAction = {
      Filters(super.doFilter(next), RequestIdTraceFilter)
    }

    override def onRouteRequest(request : RequestHeader): Option[Handler] = {
      Tracer.setCurrentContext(RequestContext(Guid.randomGuid().toString()))
      super.onRouteRequest(request)
    }


Logback Logging

    <conversionRule conversionWord="traceToken" converterClass="com.teambytes.handy.tracing.logback.LogbackTraceTokenConverter"/>
    
    ...
    
    <pattern>%d{HH:mm:ss.SSS} [%traceToken] [%thread] %level %logger{36} - %message%n%ex</pattern>
    
    ...
    
build.sbt
    
    import com.typesafe.sbt.SbtAspectj.{ Aspectj, aspectjSettings }
    import com.typesafe.sbt.SbtAspectj.AspectjKeys._
    
    enablePlugins(AspectJWeaver)
    
    aspectjSettings
    
    aspectJWeaverVersion := (aspectjVersion in Aspectj).value
    
    libraryDependencies += "com.teambytes.handy" %% "handy-tracing" % "0.0.1"
    
    fork in run := true
    
    fork in Test := true
    
    javaOptions in Test  <++=  weaverOptions in Aspectj
    
    javaOptions in run  <++=  weaverOptions in Aspectj
    
project/plugins.sbt

    addSbtPlugin("com.typesafe.sbt" % "sbt-aspectj" % "0.10.4")

    addSbtPlugin("com.gilt.sbt" % "sbt-aspectjweaver" % "0.0.1")
