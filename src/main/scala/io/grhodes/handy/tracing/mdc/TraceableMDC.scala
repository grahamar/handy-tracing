package io.grhodes.handy.tracing.mdc

import java.util

import io.grhodes.handy.tracing.Traceable
import org.slf4j.MDC

case class TraceableMDC(contextMap: util.Map[String, String]) extends Traceable {
  override def save(): Unit = MDC.setContextMap(contextMap)
  override def clear(): Unit = MDC.clear()
}
