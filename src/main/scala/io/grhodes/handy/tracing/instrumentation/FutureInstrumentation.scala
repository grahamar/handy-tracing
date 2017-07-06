package io.grhodes.handy.tracing.instrumentation

import io.grhodes.handy.tracing.{Tracer, TraceContextAware}
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation._

@Aspect
class FutureInstrumentation {

  @DeclareMixin("scala.concurrent.impl.CallbackRunnable || scala.concurrent.impl.Future.PromiseCompletingRunnable")
  def mixinTraceContextAwareToFutureRelatedRunnable: TraceContextAware = TraceContextAware.default

  @Pointcut("execution((scala.concurrent.impl.CallbackRunnable || scala.concurrent.impl.Future.PromiseCompletingRunnable).new(..)) && this(runnable)")
  def futureRelatedRunnableCreation(runnable: TraceContextAware): Unit = {}

  @After("futureRelatedRunnableCreation(runnable)")
  def afterCreation(runnable: TraceContextAware): Unit = {
    runnable.traceContext // Force traceContext initialization.
  }

  @Pointcut("execution(* (scala.concurrent.impl.CallbackRunnable || scala.concurrent.impl.Future.PromiseCompletingRunnable).run()) && this(runnable)")
  def futureRelatedRunnableExecution(runnable: TraceContextAware) = {}

  @Around("futureRelatedRunnableExecution(runnable)")
  def aroundExecution(pjp: ProceedingJoinPoint, runnable: TraceContextAware): Any = {
    Tracer.withContext(runnable.traceContext) {
      pjp.proceed()
    }
  }

}
