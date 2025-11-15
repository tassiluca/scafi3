package it.unibo.scafi.libraries

import it.unibo.scafi.types.{ MemorySafeContext, PortableTypes }

/**
 * The portable library providing common utility functions that are often used in programs and libraries.
 */
trait PortableCommonLibrary extends PortableLibrary:
  self: PortableTypes & MemorySafeContext =>

  /**
   * @return
   *   the device id of the current device
   */
  @JSExport
  def localId: language.DeviceId = language.localId
