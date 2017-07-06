package io.grhodes.handy.tracing

import io.grhodes.handy.tracing.TracingContextLocal.Context

import scala.concurrent.ExecutionContext

trait TracingPropagatingExecutionContext extends ExecutionContext {
  self =>

  override def prepare(): ExecutionContext = new ExecutionContext {
    val context: Context = TracingContextLocal.save()
    def execute(r: Runnable): Unit = self.execute(new Runnable {
      def run(): Unit = {
        TracingContextLocal.let(context) {
          r.run()
        }
      }
    })

    def reportFailure(t: Throwable): Unit = self.reportFailure(t)
  }

}

object TracingPropagatingExecutionContext {
  object Implicits {
    // Convenience wrapper around the Scala global ExecutionContext so you can just do:
    // import TracingPropagatingExecutionContext.Implicits.global
    implicit lazy val global = TracingPropagatingExecutionContextWrapper(ExecutionContext.Implicits.global)
  }
}

/**
  * Wrapper around an existing ExecutionContext that makes it propagate tracer information.
  */
class TracingPropagatingExecutionContextWrapper(wrapped: ExecutionContext)
  extends ExecutionContext with TracingPropagatingExecutionContext {

  override def execute(r: Runnable): Unit = wrapped.execute(r)

  override def reportFailure(t: Throwable): Unit = wrapped.reportFailure(t)
}

object TracingPropagatingExecutionContextWrapper {
  def apply(wrapped: ExecutionContext): TracingPropagatingExecutionContextWrapper = {
    new TracingPropagatingExecutionContextWrapper(wrapped)
  }
}
