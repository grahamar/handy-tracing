package io.grhodes.handy.tracing.xray

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import com.amazonaws.xray.AWSXRay

class LogbackXRayTokenConverter extends ClassicConverter {
  def convert(e: ILoggingEvent): String = AWSXRay.getCurrentSegment.getTraceId.toString
}
