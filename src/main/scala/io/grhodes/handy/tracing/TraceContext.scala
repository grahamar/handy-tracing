package io.grhodes.handy.tracing

import java.io.ObjectStreamException

trait TraceContext {
  def token: String
  def isEmpty: Boolean
  def nonEmpty: Boolean = !isEmpty
  def collect[T](f: TraceContext => T): Option[T] =
    if (nonEmpty)
      Some(f(this))
    else None
}
case object EmptyTraceContext extends TraceContext {
  override val token: String = ""
  override val isEmpty = true
}

case class RequestContext(override val token: String) extends TraceContext {
  override val isEmpty = false
}

trait TraceContextAware extends Serializable {
  def traceContext: TraceContext
}

object TraceContextAware {
  def default: TraceContextAware = new DefaultTraceContextAware()

  class DefaultTraceContextAware extends TraceContextAware {
    @transient val traceContext = Tracer.currentContext

    @throws[ObjectStreamException]
    private def readResolve: AnyRef = new DefaultTraceContextAware()
  }
}

object Tracer {
  private val _traceContextStorage = new ThreadLocal[TraceContext] {
    override def initialValue(): TraceContext = EmptyTraceContext
  }

  def currentContext: TraceContext = _traceContextStorage.get()

  def setCurrentContext(context: TraceContext): Unit = _traceContextStorage.set(context)

  def clearCurrentContext(): Unit = _traceContextStorage.remove()

  def withContext[T](context: TraceContext)(code: => T): T = {
    val oldContext = _traceContextStorage.get()
    _traceContextStorage.set(context)

    try code finally _traceContextStorage.set(oldContext)
  }
}
