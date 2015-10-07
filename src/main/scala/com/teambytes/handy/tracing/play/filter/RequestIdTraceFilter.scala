package com.teambytes.handy.tracing.play.filter

import com.gilt.gfc.concurrent.SameThreadExecutionContext
import com.gilt.gfc.logging.Loggable
import com.teambytes.handy.tracing.Tracer
import play.api.libs.iteratee.Iteratee
import play.api.mvc.{EssentialAction, EssentialFilter, RequestHeader, Result}

object RequestIdTraceFilter extends EssentialFilter with Loggable {
  def apply(next: EssentialAction): EssentialAction = new EssentialAction {
    def apply(requestHeader: RequestHeader): Iteratee[Array[Byte], Result] = {
      def onResult(result: Result): Result = {
        Tracer.currentContext.collect { ctx =>
          result.withHeaders("Request-Id" -> ctx.token)
        }.getOrElse(result)
      }

      next(requestHeader).map(onResult)(SameThreadExecutionContext)
    }
  }
}
