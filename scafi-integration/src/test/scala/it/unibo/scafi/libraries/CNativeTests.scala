package it.unibo.scafi.libraries

import it.unibo.scafi.integration.CTests
import it.unibo.scafi.integration.PlatformTest.ProgramOutput
import it.unibo.scafi.runtime.network.sockets.InetTypes.Port

import org.scalatest.flatspec.AnyFlatSpec

class CNativeTests extends NativeTests with CTests:

  it should behave like neighborsDiscoveryTest()

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
