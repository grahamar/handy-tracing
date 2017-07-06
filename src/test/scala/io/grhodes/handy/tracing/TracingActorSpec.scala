package io.grhodes.handy.tracing

import akka.actor.{Actor, ActorRef, ActorSystem, Props, TraceAwareActor}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import io.grhodes.handy.tracing.mdc.TraceableMDC
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.slf4j.MDC

class TracingActorSpec extends TestKit(ActorSystem("TracingActorSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  import akka.actor.TraceAwareActor.Implicits._

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A trace aware actor" must {

    "pass trace context" in {
      MDC.put("hello", "world")

      val trace = new TracingContextLocal[TraceableMDC]()
      trace.set(Some(TraceableMDC(MDC.getCopyOfContextMap)))

      val probe = TestProbe()
      val echo = system.actorOf(TestActors.echoActorProps(probe.ref))
      echo !> "hello"
      probe.expectMsg("world")
    }

  }

}

object TestActors {
  class EchoMDCActor(target: ActorRef) extends Actor with TraceAwareActor {
    override def receive = {
      case message => target ! MDC.get(message.toString)
    }
  }
  val echoActorProps = { target: ActorRef => Props(classOf[EchoMDCActor], target) }
}
