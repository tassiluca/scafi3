package libscafi3.impl

import scala.scalanative.unsafe.{ CString, Ptr }

import libscafi3.ExportedFunctions
import libscafi3.structs.{ SharedData, Neighborhood, Serializable }

/**
 * Bindings for exported C functions.
 */
object Implementations extends ExportedFunctions:

  override def shared_data_to_string(field: Ptr[SharedData]): CString = ???

  override def neighborhood_get(neighborhood: Ptr[Neighborhood], key: Ptr[Serializable]): Ptr[Serializable] = ???
