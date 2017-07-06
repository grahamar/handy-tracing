package io.grhodes.handy.tracing

trait Traceable {
  def save(): Unit
  def clear(): Unit
}
