package akka.actor

import akka.util.Timeout
import io.grhodes.handy.tracing.TracingContextLocal
import io.grhodes.handy.tracing.TracingContextLocal.Context

import scala.concurrent.Future

trait TraceAwareActor extends Actor {
  import TraceAwareActor._

  // This is why this needs to be in package akka.actor
  override protected[akka] def aroundReceive(receive: Actor.Receive, msg: Any): Unit = {
    val context: Context = TracingContextLocal.save()
    try {
      msg match {
        case TraceMsg(ctx, origMsg) =>
          if (ctx != null)
            TracingContextLocal.restore(ctx)
          else
            TracingContextLocal.clear()
          super.aroundReceive(receive, origMsg)
        case _ =>
          super.aroundReceive(receive, msg)
      }
    } finally {
      if (context != null)
        TracingContextLocal.restore(context)
      else
        TracingContextLocal.clear()
    }
  }
}

object TraceAwareActor {
  private case class TraceMsg(trace: Context, msg: Any)

  object Implicits {

    /**
      * Add two new methods that allow trace info to be passed to TraceAwareActor actors.
      *
      * Do NOT use these methods to send to actors that are not TraceAwareActor.
      */
    implicit class TraceAwareActorRef(val ref: ActorRef) extends AnyVal {

      import akka.pattern.ask

      /**
        * Send a message to an actor that is TraceAwareActor - it will propagate
        * the current trace context.
        */
      def !>(msg: Any): Unit =
        ref ! TraceMsg(TracingContextLocal.save(), msg)

      /**
        * "Ask" an actor that is TraceAwareActor for something - it will propagate
        * the current trace context
        */
      def ?>(msg: Any)(implicit timeout: Timeout): Future[Any] =
        ref ? TraceMsg(TracingContextLocal.save(), msg)
    }
  }

}
