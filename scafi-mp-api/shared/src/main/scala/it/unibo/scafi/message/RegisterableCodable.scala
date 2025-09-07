package it.unibo.scafi.message

/**
 * A [[Codable]] that allows to register values
 * @tparam Value
 * @tparam Format
 *   the type of the encoded format.
 */
trait RegisterableCodable[Value, Format] extends Codable[Value, Format]:
  def register(value: Value): Unit
