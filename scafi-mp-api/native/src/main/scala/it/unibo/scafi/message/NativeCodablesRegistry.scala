package it.unibo.scafi.message

import scala.scalanative.unsafe.{ fromCString, Ptr }

import it.unibo.scafi.message.CBinaryCodable.typeName
import it.unibo.scafi.message.CodablesRegistry

trait NativeCodablesRegistry extends CodablesRegistry[NativeCodablesRegistry]:
  override type Codable = Ptr[CBinaryCodable]

object NativeCodablesRegistry:

  /** @return an empty registry for JS codables with string identifiers. */
  def forStringId(): NativeCodablesRegistry & { type CodableId = String } = StringIdentifiedCodablesRegistry(Map.empty)

  private class StringIdentifiedCodablesRegistry(mappings: Map[String, Ptr[CBinaryCodable]])
      extends NativeCodablesRegistry:

    override type CodableId = String

    override def register(codable: Codable): NativeCodablesRegistry & { type CodableId = String } =
      val name = fromCString(codable.typeName)
      StringIdentifiedCodablesRegistry(mappings + (name -> codable))

    override def apply(id: CodableId): Codable throws NotRegisteredCodableException =
      mappings.getOrElse(id, throw NotRegisteredCodableException(id))
