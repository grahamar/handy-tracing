package io.grhodes.handy.tracing.concurrent

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.entities.Entity

import scala.concurrent.ExecutionContext

trait XRayPropagatingExecutionContext extends ExecutionContext {
  self =>

  override def prepare(): ExecutionContext = new ExecutionContext {
    val contextEntity: Entity = AWSXRay.getThreadLocal
    def execute(r: Runnable): Unit = self.execute(new Runnable {
      def run(): Unit = {
        if (contextEntity != null ) {
          AWSXRay.injectThreadLocal(contextEntity)
        }
        r.run()
      }
    })

    def reportFailure(t: Throwable): Unit = self.reportFailure(t)
  }

}

object XRayPropagatingExecutionContext {
  object Implicits {
    // Convenience wrapper around the Scala global ExecutionContext so you can just do:
    // import XRayPropagatingExecutionContext.Implicits.global
    implicit lazy val global = XRayPropagatingExecutionContextWrapper(ExecutionContext.Implicits.global)
  }
}

/**
  * Wrapper around an existing ExecutionContext that makes it propagate tracer information.
  */
class XRayPropagatingExecutionContextWrapper(wrapped: ExecutionContext)
  extends ExecutionContext with XRayPropagatingExecutionContext {

  override def execute(r: Runnable): Unit = wrapped.execute(r)

  override def reportFailure(t: Throwable): Unit = wrapped.reportFailure(t)
}

object XRayPropagatingExecutionContextWrapper {
  def apply(wrapped: ExecutionContext): XRayPropagatingExecutionContextWrapper = {
    new XRayPropagatingExecutionContextWrapper(wrapped)
  }
}
