package it.unibo.scafi.runtime.network

import it.unibo.scafi.message.ValueTree

/**
 * Policy defining the received message expiration policy.
 */
trait ExpirationPolicy:

  /** The type of the neighbor device identifier. */
  type DeviceId

  /** Metadata associated with received neighbor message. */
  type Metadata

  /** Data structure to hold neighbor message along with its metadata. */
  protected case class Message(metadata: Metadata, valueTree: ValueTree)

  /** Provides the current metadata context for newly received messages. */
  protected def current: Metadata

  extension (neighborMsg: (DeviceId, Message))
    /**
     * Determines whether the neighbor data should be dropped based on its metadata.
     * @return
     *   true if the neighbor data should be dropped, false otherwise.
     */
    protected def shouldBeDropped: Boolean
end ExpirationPolicy
