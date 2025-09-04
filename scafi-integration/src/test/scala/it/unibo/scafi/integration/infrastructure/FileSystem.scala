package it.unibo.scafi.integration.infrastructure

import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Path, StandardOpenOption }

trait FileSystem:

  def copy(origin: Path, destination: Path): Unit = Files.copy(origin, destination): Unit

  def baseName(path: Path): String = path.getFileName().toString.replaceFirst("\\.[^.]+$", "")

  def read(path: Path): String = Files.readString(path, StandardCharsets.UTF_8)

  def write(path: Path, content: String): Unit = Files.writeString(
    path,
    content,
    StandardCharsets.UTF_8,
    StandardOpenOption.CREATE,
    StandardOpenOption.TRUNCATE_EXISTING,
  ): Unit
