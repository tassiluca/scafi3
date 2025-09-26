package it.unibo.scafi.presentation

import scala.scalanative.unsafe.{ fromCString, Ptr }

import it.unibo.scafi.message.CodablesRegistry

import libscafi3.structs.BinaryCodable

trait NativeCodablesRegistry extends CodablesRegistry[NativeCodablesRegistry]:
  override type Codable = Ptr[BinaryCodable]

object NativeCodablesRegistry:

  /** @return an empty registry for JS codables with string identifiers. */
  def forStringId(): NativeCodablesRegistry & { type CodableId = String } = StringIdentifiedCodablesRegistry(Map.empty)

  private class StringIdentifiedCodablesRegistry(mappings: Map[String, Ptr[BinaryCodable]])
      extends NativeCodablesRegistry:
    override type CodableId = String
    override def register(codable: Codable): NativeCodablesRegistry & { type CodableId = String } =
      val name = fromCString((!codable).type_name)
      StringIdentifiedCodablesRegistry(mappings + (name -> codable))
    override def apply(id: CodableId): Codable throws NotRegisteredCodableException =
      mappings.getOrElse(id, throw NotRegisteredCodableException(id))
