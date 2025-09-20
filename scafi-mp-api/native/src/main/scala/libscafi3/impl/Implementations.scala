package libscafi3.impl

import scala.scalanative.unsafe.{ exported, Ptr }

import it.unibo.scafi.runtime.NativeScafiRuntime
import it.unibo.scafi.types.CMap

import libscafi3.all.Serializable

object ExportedNativeBindings:

  @exported("socket_network")
  def socketNetwork(
      deviceId: Ptr[Serializable],
      port: Int,
      neighbors: CMap,
  ) = try NativeScafiRuntime.NativeApi.socketNetwork(deviceId, port, neighbors)
  catch
    case e =>
      scribe.error("Error in native socket_network", e)
      throw e
