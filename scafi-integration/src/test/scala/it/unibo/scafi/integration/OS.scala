package it.unibo.scafi.integration

trait OS:

  /** @return true if the operating system is Windows, false otherwise. */
  def isWindows: Boolean = System.getProperty("os.name").toLowerCase.contains("windows")
