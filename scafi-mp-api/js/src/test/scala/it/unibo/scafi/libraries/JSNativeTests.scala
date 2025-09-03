// package it.unibo.scafi.libraries

// import scala.concurrent.Future
// import scala.concurrent.ExecutionContext

// import it.unibo.scafi.test.AsyncSpec
// import it.unibo.scafi.runtime.JSPlatformTest
// import it.unibo.scafi.test.SimpleGrids.vonNeumannGrid
// import it.unibo.scafi.runtime.FreePortFinder

// class JSNativeTests extends AsyncSpec with JSPlatformTest:

//   override given executionContext: ExecutionContext = scalajs.concurrent.JSExecutionContext.Implicits.queue

//   "First joke" should "be funny" in:
//     for
//       portsPool <- FreePortFinder.findFreePorts(2).map(_.toList)
//       _ = scribe.info(s"Allocated ports: ${portsPool.mkString(", ")}")
//       _ <- Future.sequence:
//         vonNeumannGrid(rows = 2, cols = 1): (id, neighbors) =>
//           val neighborsAsJsEntries = neighbors
//             .map(nid => s"[$nid, Runtime.Endpoint('localhost', ${portsPool(nid)})]")
//             .mkString("[", ", ", "]")
//           Future:
//             testProgram("simple-exchange"):
//               "{{ deviceId }}" -> id.toString
//               "{{ port }}" -> portsPool(id).toString
//               "{{ neighbors }}" -> neighborsAsJsEntries
//     yield succeed
