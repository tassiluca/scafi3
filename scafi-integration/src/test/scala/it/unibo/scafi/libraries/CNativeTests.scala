package it.unibo.scafi.libraries

import scala.concurrent.Future.sequence

import it.unibo.scafi.integration.CTests
import it.unibo.scafi.integration.PlatformTest.ProgramOutput
import it.unibo.scafi.runtime.network.sockets.InetTypes.Port

import org.scalatest.flatspec.AnyFlatSpec

class CNativeTests extends NativeTests with CTests:

  it should behave like neighborsDiscoveryTest()

  "Protobuf exchange aggregate program" should "correctly exchange protobuf messages" in:
    def cSensor(id: Int): String = s"Sensor(id=#$id, temp=${id * 10}.00)"
    sequence:
      aggregateResult("protobuf-exchange", rows = 2, cols = 2)
    .futureValue should contain theSameElementsAs Seq(
      0 -> fieldRepr(default = cSensor(0), neighbors = Map(1 -> cSensor(1), 2 -> cSensor(2))),
      1 -> fieldRepr(default = cSensor(1), neighbors = Map(0 -> cSensor(0), 3 -> cSensor(3))),
      2 -> fieldRepr(default = cSensor(2), neighbors = Map(0 -> cSensor(0), 3 -> cSensor(3))),
      3 -> fieldRepr(default = cSensor(3), neighbors = Map(1 -> cSensor(1), 2 -> cSensor(2))),
    )

  override def neighborsAsCode(id: ID, neighbors: Set[ID], ports: Seq[Port]): ProgramOutput = neighbors
    .map(nid => s"""
      |BinaryCodable* neighbor_$nid = codable_int($nid);
      |struct Endpoint device_endpoint_$nid = { "localhost", ${ports(nid)} };
      |Neighborhood_put(neighbors, neighbor_$nid, &device_endpoint_$nid);
      |""".stripMargin)
    .mkString(sep = "\n")

  override def fieldRepr[Value](default: Value, neighbors: Map[ID, Value]): ProgramOutput =
    s"Field($default, ${neighbors.mkString("[", ", ", "]")})"
end CNativeTests
