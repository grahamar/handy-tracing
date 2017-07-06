package io.grhodes.handy.tracing

import java.util.concurrent.Executors

import io.grhodes.handy.tracing.mdc.TraceableMDC
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, WordSpec}
import org.slf4j.MDC

import scala.concurrent.{ExecutionContext, Future}

class TracingSpec extends WordSpec with MustMatchers with ScalaFutures {

  "Tracing" must {
    "propagate tracing context across futures" in {
      val ec = TracingPropagatingExecutionContextWrapper(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2)))
      MDC.put("hello", "world")
      MDC.get("hello") mustEqual "world"

      val trace = new TracingContextLocal[TraceableMDC]()
      trace.set(Some(TraceableMDC(MDC.getCopyOfContextMap)))

      Future(MDC.get("hello"))(ExecutionContext.Implicits.global).futureValue mustEqual null
      Future(MDC.get("hello"))(ec).futureValue mustEqual "world"
      Future(MDC.get("hello"))(ExecutionContext.Implicits.global).futureValue mustEqual null
    }
  }

}
