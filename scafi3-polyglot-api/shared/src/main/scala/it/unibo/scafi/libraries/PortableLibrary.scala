package it.unibo.scafi.libraries

import it.unibo.scafi.types.PortableTypes

/**
 * The root base trait for all portable libraries.
 */
trait PortableLibrary:
  self: PortableTypes =>
  export it.unibo.scafi.language.AggregateFoundation
  export it.unibo.scafi.message.Codable

  /**
   * The language type comprising all the needed syntaxes needed to implement the library functionalities.
   */
  type Language <: AggregateFoundation

  /**
   * The [[Language]] instance used by the library to which delegate the syntax operations.
   */
  val language: Language

  /**
   * A portable, semantically equivalent definition of the [[language.SharedData]] data structure.
   */
  type SharedData[Value]

  /**
   * [[SharedData]] is isomorphic to [[language.SharedData]].
   */
  given [Value]: Iso[SharedData[Value], language.SharedData[Value]] = compiletime.deferred

  /**
   * A codec defining how to convert a value of type `Value` to/from a codable representation in format `Format`.
   */
  type Codec[Value, Format]

  /**
   * Values subtyping [[Codec]] can be automatically converted to [[Codable]] values.
   */
  given codecOf[Format, Value <: Codec[Value, Format]]: Conversion[Value, Codable[Value, Format]] = compiletime.deferred
end PortableLibrary
