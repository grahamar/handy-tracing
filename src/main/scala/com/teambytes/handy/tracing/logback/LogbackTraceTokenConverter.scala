package com.teambytes.handy.tracing.logback

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import com.teambytes.handy.tracing.Tracer

class LogbackTraceTokenConverter extends ClassicConverter {
  def convert(e: ILoggingEvent): String = Tracer.currentContext.collect(_.token).getOrElse("undefined")
}
