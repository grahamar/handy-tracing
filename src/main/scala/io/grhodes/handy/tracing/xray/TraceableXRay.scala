package io.grhodes.handy.tracing.xray

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.entities.Entity
import io.grhodes.handy.tracing.Traceable

case class TraceableXRay(xrayLocal: Entity) extends Traceable {
  override def useThis(): Unit = AWSXRay.injectThreadLocal(xrayLocal)
}
