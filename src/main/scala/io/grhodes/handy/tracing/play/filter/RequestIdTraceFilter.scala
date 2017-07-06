package io.grhodes.handy.tracing.play.filter

import akka.util.ByteString
import com.gilt.gfc.concurrent.SameThreadExecutionContext
import com.gilt.gfc.logging.Loggable
import io.grhodes.handy.tracing.Tracer
import play.api.libs.streams.Accumulator
import play.api.mvc.{EssentialAction, EssentialFilter, RequestHeader, Result}

object RequestIdTraceFilter extends EssentialFilter with Loggable {
  def apply(next: EssentialAction): EssentialAction = new EssentialAction {
    def apply(requestHeader: RequestHeader): Accumulator[ByteString, Result] = {
      def onResult(result: Result): Result = {
        Tracer.currentContext.collect { ctx =>
          result.withHeaders("X-Request-Id" -> ctx.token)
        }.getOrElse(result)
      }

      next(requestHeader).map(onResult)(SameThreadExecutionContext)
    }
  }
}
