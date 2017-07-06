package io.grhodes.handy.tracing.xray

import javax.inject.Inject

import akka.util.ByteString
import com.amazonaws.xray.entities.TraceHeader.SampleDecision
import com.amazonaws.xray.entities.{Segment, TraceHeader, TraceID}
import com.amazonaws.xray.{AWSXRay, ThreadLocalStorage}
import com.gilt.gfc.concurrent.SameThreadExecutionContext
import com.gilt.gfc.logging.Loggable
import io.grhodes.handy.tracing.TracingContextLocal
import play.api.libs.streams.Accumulator
import play.api.mvc._

import scala.collection.JavaConverters._
import scala.util.Try
import scala.util.control.NonFatal

case class PreFilterSegment(segment: Segment, responseHeader: Option[TraceHeader] = None)

class XRayTraceFilter @Inject() () extends EssentialFilter with Loggable {

  private val segmentNamingStrategy = DynamicPlaySegmentNamingStrategy(null)
  private val recorder = AWSXRay.getGlobalRecorder
  private val traceCtx = new TracingContextLocal[TraceableXRay]()

  def apply(next: EssentialAction): EssentialAction = new EssentialAction {
    def apply(requestHeader: RequestHeader): Accumulator[ByteString, Result] = {
      val segment = preFilter(requestHeader)
      traceCtx.set(Some(TraceableXRay(AWSXRay.getThreadLocal)))
      next(
        // TODO Play 2.6 supports attribute objects, remove serialization
        requestHeader.withTag("com.amazonaws.xray.entities.Entity", segment.segment.serialize())
      ).map { result =>
        try {
          val resultWithHeaders = segment match {
            case PreFilterSegment(_, Some(responseHeader)) => result.withHeaders("X-Amzn-Trace-Id" -> responseHeader.toString)
            case _ => result
          }
          postFilter(requestHeader, resultWithHeaders, segment)
        } finally {
          traceCtx.clear()
        }
      }(SameThreadExecutionContext)
    }
  }

  private def preFilter(requestHeader: RequestHeader): PreFilterSegment = {
    val incomingHeader = requestHeader.headers.get("X-Amzn-Trace-Id").map(TraceHeader.fromString)
    val samplingStrategy = recorder.getSamplingStrategy

    debug(s"Incoming trace header received: $incomingHeader")

    val sampleDecision = incomingHeader.map(_.getSampled).getOrElse(fromSamplingStrategy(requestHeader)) match {
      case SampleDecision.REQUESTED | SampleDecision.UNKNOWN => fromSamplingStrategy(requestHeader)
      case decision => decision
    }

    val traceId = incomingHeader.map(_.getRootTraceId).getOrElse(new TraceID())
    val parentId = incomingHeader.map(_.getParentId).orNull

    val created = sampleDecision match {
      case SampleDecision.SAMPLED => recorder.beginSegment(getSegmentName(requestHeader), traceId, parentId)

      case _ if samplingStrategy.isForcedSamplingSupported =>
        val createdSegment = this.recorder.beginSegment(getSegmentName(requestHeader), traceId, parentId)
        createdSegment.setSampled(false)
        createdSegment

      case _ => recorder.beginDummySegment(traceId)
    }

    val requestAttributes = Map("url" -> requestHeader.uri, "method" -> requestHeader.method) ++
      userAgent(requestHeader) ++
      xForwardedFor(requestHeader)

    created.putHttp("request", requestAttributes.asJava)

    incomingHeader.map { inHeader =>
      val responseHeader = if(SampleDecision.REQUESTED == inHeader.getSampled) {
        val respHead = new TraceHeader(created.getTraceId)
        respHead.setSampled(if(created.isSampled) SampleDecision.SAMPLED else SampleDecision.NOT_SAMPLED)
        respHead
      } else {
        new TraceHeader(created.getTraceId)
      }
      PreFilterSegment(created, Some(responseHeader))
    }.getOrElse(PreFilterSegment(created))
  }

  private def postFilter(requestHeader: RequestHeader, result: Result, segment: PreFilterSegment): Result = {
    result.header.status / 100 match {
      case 5 => segment.segment.setFault(true)
      case 4 =>
        segment.segment.setError(true)
        if(result.header.status == 429) {
          segment.segment.setThrottle(true)
        }
      case _ => // Do nothing, all good in the hood!
    }
    val responseAttributes = Map(
      "status" -> java.lang.Integer.valueOf(result.header.status)
    ) ++ contentLength(result.header).map("content_length" -> _).toMap

    segment.segment.putHttp("response", responseAttributes)

    debug(s"Ending segment [${segment.segment.getName}-${segment.segment.getReferenceCount}]")
    // it's easy
    Try(recorder.endSegment()).recover {
      case NonFatal(_) =>
        segment.segment.end()
        recorder.sendSegment(segment.segment)
        ThreadLocalStorage.clear()
    }
    result
  }

  private def userAgent(requestHeader: RequestHeader): Map[String, String] = {
    requestHeader.headers.get("User-Agent").map("user_agent" -> _).toMap
  }

  private def xForwardedFor(requestHeader: RequestHeader): Map[String, Any] = {
    requestHeader.headers.get("X-Forwarded-For").map(_.split(",").head.trim).map { forwardedFor =>
      Map("client_ip" -> forwardedFor, "x_forwarded_for" -> java.lang.Boolean.TRUE)
    }.getOrElse(Map(
      "client_ip" -> requestHeader.remoteAddress
    ))
  }

  private def contentLength(header: ResponseHeader): Option[java.lang.Integer] = {
    header.headers.get("Content-Length").filter(_.nonEmpty).flatMap { contentLengthString =>
      Try(java.lang.Integer.valueOf(java.lang.Integer.parseInt(contentLengthString))).recover {
        case e: NumberFormatException =>
          debug("Unable to parse Content-Length header from HttpServletResponse.", e)
          throw e
      }.toOption
    }
  }

  private def fromSamplingStrategy(requestHeader: RequestHeader): SampleDecision = {
    val host = requestHeader.headers.get("Host").orNull
    if(recorder.getSamplingStrategy.shouldTrace(host, requestHeader.path, requestHeader.method)) {
      debug("Sampling strategy decided SAMPLED.")
      SampleDecision.SAMPLED
    } else {
      debug("Sampling strategy decided NOT_SAMPLED.")
      SampleDecision.NOT_SAMPLED
    }
  }

  private def getSegmentName(requestHeader: RequestHeader) = {
    try {
      segmentNamingStrategy.nameForRequest(requestHeader)
    } catch {
      case e: NullPointerException =>
        throw new RuntimeException("AWSXRayServletFilter requires either a fixedName init-param or a SegmentNamingStrategy be provided. Please change your constructor call as necessary.", e)
    }
  }

}
