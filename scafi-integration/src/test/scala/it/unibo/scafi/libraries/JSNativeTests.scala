package it.unibo.scafi.libraries

import scala.concurrent.Future.sequence

import it.unibo.scafi.integration.JSTests
import it.unibo.scafi.integration.PlatformTest.ProgramOutput
import it.unibo.scafi.runtime.network.sockets.InetTypes.Port

import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec

class JSNativeTests extends NativeTests with JSTests:

  it should behave like neighborsDiscoveryTest()

  "Exchange aggregate program with branch restriction" should "correctly spread local values to aligned neighbors" in:
    sequence:
      aggregateResult("restricted-exchange", rows = 2, cols = 2)
    .futureValue should contain theSameElementsAs Seq(
      0 -> fieldRepr(default = true, neighbors = Map(2 -> true)),
      1 -> fieldRepr(default = false, neighbors = Map(3 -> false)),
      2 -> fieldRepr(default = true, neighbors = Map(0 -> true)),
      3 -> fieldRepr(default = false, neighbors = Map(1 -> false)),
    )

  "Protobuf exchange aggregate program" should "correctly exchange protobuf messages" in sensorTestWith("protobuf")

  "JSON exchange aggregate program" should "correctly exchange JSON messages" in sensorTestWith("json")

  inline def sensorTestWith(format: String): Assertion =
    def jsSensor(id: Int): String = s"Sensor(id=#$id, temp=${id * 10})"
    sequence:
      aggregateResult(s"${format}-exchange", rows = 2, cols = 2)
    .futureValue should contain theSameElementsAs Seq(
      0 -> fieldRepr(default = jsSensor(0), neighbors = Map(1 -> jsSensor(1), 2 -> jsSensor(2))),
      1 -> fieldRepr(default = jsSensor(1), neighbors = Map(0 -> jsSensor(0), 3 -> jsSensor(3))),
      2 -> fieldRepr(default = jsSensor(2), neighbors = Map(0 -> jsSensor(0), 3 -> jsSensor(3))),
      3 -> fieldRepr(default = jsSensor(3), neighbors = Map(1 -> jsSensor(1), 2 -> jsSensor(2))),
    )

  override def neighborsAsCode(id: ID, neighbors: Set[ID], ports: Seq[Port]): ProgramOutput = neighbors
    .map(nid => s"[$nid, Runtime.Endpoint('localhost', ${ports(nid)})]")
    .mkString("[", ", ", "]")

  override def fieldRepr[Value](default: Value, neighbors: Map[ID, Value]): ProgramOutput =
    s"Field($default, $neighbors)"
end JSNativeTests
