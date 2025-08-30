package it.unibo.scafi.presentation

import scalajs.js

/**
 * A registry for JavaScript codables.
 *
 * This registry allows to register and retrieve codables by their identifier. The identifier is expected to be unique
 * across all registered codables.
 */
trait JSCodablesRegistry:

  /** The type used to identify codables in the registry. */
  type CodableId

  /**
   * TODO: turn it into functional style
   *
   * Register a codable in the registry.
   * @param codable
   *   the codable to registers
   */
  def register(codable: JSCodable): Unit

  /**
   * Retrieve a codable by its identifier.
   * @param id
   *   the identifier of the codable to retrieve
   * @return
   *   the codable associated with the provided identifier
   * @throws NotRegisteredCodableException
   *   if no codable is registered with the provided identifier
   */
  def apply(codableId: CodableId): JSCodable throws NotRegisteredCodableException

  /**
   * Safely retrieve a codable by its identifier.
   * @param id
   *   the identifier of the codable to retrieve
   * @return
   *   the codable associated with the provided identifier, wrapped in an `Some`, or `None` if no codable is registered
   */
  def get(id: CodableId): Option[JSCodable] =
    try Some(apply(id))
    catch case _: NotRegisteredCodableException => None

  /** Exception thrown when trying to retrieve a codable that is not registered. */
  case class NotRegisteredCodableException(id: CodableId) extends Exception(s"Type named $id is not registered.")
end JSCodablesRegistry

object JSCodablesRegistry:

  def apply(): JSCodablesRegistry & { type CodableId = String } = new JSCodablesRegistry:
    override type CodableId = String
    private val registry = js.Map.empty[CodableId, JSCodable]
    override def register(codable: JSCodable): Unit = registry += (codable.typeName -> codable)
    override def apply(id: CodableId): JSCodable throws NotRegisteredCodableException =
      registry.getOrElse(id, throw NotRegisteredCodableException(id))
