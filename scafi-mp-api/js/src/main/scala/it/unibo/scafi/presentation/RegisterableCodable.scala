package it.unibo.scafi.presentation

import it.unibo.scafi.message.Codable

trait RegisterableCodable[Value, Format] extends Codable[Value, Format]:
  def register(value: Value): Unit
