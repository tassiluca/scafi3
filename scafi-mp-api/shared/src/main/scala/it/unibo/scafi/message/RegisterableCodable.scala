package it.unibo.scafi.message

trait RegisterableCodable[Value, Format] extends Codable[Value, Format]:
  def register(value: Value): Unit
