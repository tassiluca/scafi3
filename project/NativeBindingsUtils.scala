import bindgen.plugin.BindgenPlugin
import bindgen.interface.Binding
import sbt.*
import sbt.Keys.{ baseDirectory, resourceDirectory, sourceGenerators, sourceManaged, streams, thisProjectRef }
import scala.sys.process.Process
import scala.util.Try

/**
 * A utility SBT plugin to generate native bindings using Scala Native Bindgen plugin inside a Docker container.
 */
object NativeBindingsUtils extends AutoPlugin {

  override def trigger = noTrigger
  override def requires: Plugins = BindgenPlugin

  object autoImport {
    val generateBindings = taskKey[Seq[File]]("Generate native bindings using Docker")
    val bindgenDockerfile = settingKey[File]("Path to the Dockerfile for bindgen")
    val nativeBindings = settingKey[Seq[Binding]]("Native bindings configuration")
  }

  import autoImport.*
  
  private val dockerImageName = "sn-bindgen-builder"
  private val envVarName = "IN_BINDGEN_DOCKER"

  override lazy val projectSettings = Seq(
    bindgenDockerfile := (Compile / resourceDirectory).value / "Dockerfile.bindgen",
    nativeBindings := Seq.empty,
    // Override bindgenBindings based on environment
    BindgenPlugin.autoImport.bindgenBindings := {
      // Inside Docker use the bindings from nativeBinding, while on host set to empty to prevent bindgen from running
      if (sys.env.contains(envVarName)) nativeBindings.value else Seq.empty
    },
    generateBindings := {
      implicit val log: Logger = streams.value.log
      val headerFiles = nativeBindings.value.map(_.headerFile).toSet
      val managedDir = (Compile / sourceManaged).value
      val existingBindings = generatedFiles(managedDir)
      val cacheDirectory = streams.value.cacheDirectory
      if (isCI && existingBindings.nonEmpty) { // reuse existing bindings if available in CI
        log.info(s"CI environment: Found ${existingBindings.size} existing binding files.")
        existingBindings
      } else { // local development: use Docker with cache invalidation based on header files
        val missingHeaders = headerFiles.filterNot(_.exists())
        if (missingHeaders.nonEmpty) {
          log.warn(s"Header files not found.")
          Seq.empty
        } else {
          FileFunction.cached(cacheDirectory / "bindgen") { _ =>
            if (!isDockerAvailable) sys.error("Docker is needed to generate native bindings but is not available")
            if (!imageExists(dockerImageName)) buildImage(dockerImageName, bindgenDockerfile.value)
            runBindgen(dockerImageName, thisProjectRef.value.project, (ThisBuild / baseDirectory).value, envVarName)
            generatedFiles(managedDir).toSet
          }(headerFiles).toSeq
        }
      }
    },
    Compile / sourceGenerators := {
      // Inside Docker: keep bindgen's generators as-is, on host use only custom Docker-based generator
      if (sys.env.contains(envVarName)) (Compile / sourceGenerators).value else Seq(generateBindings.taskValue)
    }
  )

  private def isCI: Boolean = sys.env.get("CI").contains("true")

  private def isDockerAvailable: Boolean = Try(Process("docker --version").! == 0).getOrElse(false)

  private def imageExists(imageName: String): Boolean = Process(s"docker image inspect $imageName").! == 0

  private def buildImage(imageName: String, dockerfile: File)(implicit log: Logger): Unit = {
    log.info(s"Building Docker image '$imageName'. This may take a while but will be done only once...")
    val command = s"docker build -f ${dockerfile.absolutePath} -t $imageName ${dockerfile.getParentFile.absolutePath}"
    val exitCode = Process(command).!
    if (exitCode != 0) sys.error("Docker image build failed")
  }

  private def runBindgen(imageName: String, projectId: String, rootDir: File, envVar: String)(implicit log: Logger): Unit = {
    log.info(s"Generating bindings for $projectId...")
    val userId = sys.env.get("UID").orElse(Try(Process("id -u").!!.trim).toOption).getOrElse("1000")
    val groupId = sys.env.get("GID").orElse(Try(Process("id -g").!!.trim).toOption).getOrElse("1000")
    // Create a temporary directory for SBT cache to avoid permission issues and speed up subsequent builds
    val tempHome = rootDir / "target" / "docker-home"
    IO.createDirectory(tempHome)
    val command = s"docker run --rm --user $userId:$groupId -v ${rootDir.absolutePath}:/project " +
      s"-v ${tempHome.absolutePath}:/home/sbtuser -e HOME=/home/sbtuser -e $envVar=true $imageName " +
      s"sbt $projectId/bindgenGenerateScalaSources"
    val exitCode = Process(command = Seq("sh", "-c", command), cwd = rootDir).!
    if (exitCode != 0) sys.error("Binding generation failed")
  }

  private def generatedFiles(managedDir: File): Seq[File] = (managedDir ** "*.scala").get

}