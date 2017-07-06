package io.grhodes.handy.tracing.xray

import com.amazonaws.xray.{AWSXRay, ThreadLocalStorage}
import com.amazonaws.xray.entities.{Segment, Subsegment}
import com.gilt.gfc.logging.Loggable

import scala.util.Try
import scala.util.control.NonFatal

trait WithXRay extends Loggable {
  def withSegment[T](name: String)(block: => T): T = {
    val segment = Try(AWSXRay.getCurrentSegment).toOption.map { _ =>
      Try(AWSXRay.beginSubsegment(name)).recover {
        case NonFatal(_) => AWSXRay.beginSegment(name)
      }.get
    }.getOrElse {
      AWSXRay.beginSegment(name)
    }

    try {
      debug(s"Created segment [${segment.getId}]...")
      block
    } catch {
      case NonFatal(e) =>
        segment.addException(e)
        throw e
    } finally {
      segment match {
        case s: Segment =>
          Try(AWSXRay.endSegment()).recover {
            case NonFatal(_) =>
              s.end()
              AWSXRay.getGlobalRecorder.sendSegment(s)
              ThreadLocalStorage.clear()
          }
        case s: Subsegment =>
          Try(AWSXRay.endSubsegment()).recover {
            case NonFatal(_) =>
              s.end()
              AWSXRay.getGlobalRecorder.sendSubsegment(s)
              ThreadLocalStorage.clear()
          }
      }
    }
  }
}
