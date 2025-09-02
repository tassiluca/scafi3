package it.unibo.scafi.runtime

import scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("fs", JSImport.Namespace)
object FS extends FS

@js.native
trait FS extends js.Object:
  def readFileSync(path: String, encoding: String): String = js.native
  def writeFileSync(path: String, data: String): Unit = js.native
  def existsSync(path: String): Boolean = js.native
  def copyFileSync(src: String, dest: String): Unit = js.native
  def rmSync(path: String, options: js.UndefOr[js.Object] = js.undefined): Unit = js.native
  def mkdirSync(path: String): Unit = js.native

object FSOptions:
  def recursive: js.Object = js.Dynamic.literal(recursive = true).asInstanceOf[js.Object]
  def recursiveForce: js.Object = js.Dynamic.literal(recursive = true, force = true).asInstanceOf[js.Object]

@js.native
@JSImport("path", JSImport.Namespace)
object Path extends Path

@js.native
trait Path extends js.Object:
  def basename(path: String): String = js.native
  def resolve(paths: String*): String = js.native
  def join(paths: String*): String = js.native

@js.native
@JSImport("os", JSImport.Namespace)
object OS extends OS

@js.native
trait OS extends js.Object:
  def tmpdir(): String = js.native

@js.native
@JSImport("child_process", JSImport.Namespace)
object ChildProcess extends ChildProcess

@js.native
trait ChildProcess extends js.Object:
  def execSync(command: String, options: js.Dictionary[Any] = js.Dictionary.empty): js.Any = js.native

@js.native
trait ProcessEnv extends js.Object:
  val NODE_ENV: js.UndefOr[String] = js.native
  val TMPDIR: js.UndefOr[String] = js.native

@js.native
trait Process extends js.Object:
  val cwd: js.Function0[String] = js.native
  val env: ProcessEnv = js.native

@js.native
@JSImport("process", JSImport.Namespace)
object NodeProcess extends Process
