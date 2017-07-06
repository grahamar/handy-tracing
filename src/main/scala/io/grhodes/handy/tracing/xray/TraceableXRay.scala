package io.grhodes.handy.tracing.xray

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.entities.Entity
import io.grhodes.handy.tracing.Traceable

case class TraceableXRay(xrayLocal: Entity) extends Traceable {
  override def save(): Unit = AWSXRay.injectThreadLocal(xrayLocal)
  override def clear(): Unit = AWSXRay.clearThreadLocal()
}
