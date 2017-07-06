package io.grhodes.handy.tracing.xray

import com.amazonaws.xray.entities.SearchPattern
import com.gilt.gfc.logging.Loggable
import play.api.mvc.RequestHeader

case class DynamicPlaySegmentNamingStrategy(fallbackName: String, recognizedHosts: String = "*") extends Loggable {

  private lazy val overrideName: Option[String] = {
    val environmentNameOverrideValue = System.getenv("AWS_XRAY_TRACING_NAME")
    val systemNameOverrideValue = System.getProperty("com.amazonaws.xray.strategy.tracingName")

    Option(environmentNameOverrideValue).filter(_.nonEmpty).orElse(
      Option(systemNameOverrideValue).filter(_.nonEmpty)
    )
  }

  private lazy val actualFallbackName = {
    overrideName.map { on =>
      info(s"""
              |Environment variable AWS_XRAY_TRACING_NAME or system property com.amazonaws.xray.strategy.tracingName set.
              |Overriding DynamicSegmentNamingStrategy constructor argument.
              |Segments generated with this strategy will be named: $on when the host header is
              |unavilable or does not match the provided recognizedHosts pattern.
           """.stripMargin)
      on
    }.getOrElse(fallbackName)
  }

  def nameForRequest(requestHeader: RequestHeader): String = {
    requestHeader.headers.get("Host").filter(matchesRecognizedHosts).getOrElse(actualFallbackName)
  }

  private def matchesRecognizedHosts(host: String) = {
    !(recognizedHosts != null && !SearchPattern.wildcardMatch(recognizedHosts, host))
  }

}
