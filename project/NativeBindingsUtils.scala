import sbt.*
import sbt.Keys.{ baseDirectory, resourceDirectory, sourceGenerators, sourceManaged, streams, thisProjectRef }
import scala.sys.process.Process
import scala.util.Try

object NativeBindingsUtils extends AutoPlugin {

  override def trigger = noTrigger
  override def requires: Plugins = bindgen.plugin.BindgenPlugin

  object autoImport {
    val generateBindings = taskKey[Seq[File]]("Generate bindings using Docker")
    val bindgenDockerfile = settingKey[File]("Path to the Dockerfile for bindgen")
    val nativeBindings = settingKey[Seq[bindgen.interface.Binding]]("Native bindings configuration (used instead of bindgenBindings)")
  }

  import autoImport.*
  
  private val dockerImageName = "sn-bindgen-builder"
  private val envVarName = "IN_BINDGEN_DOCKER"

  private def isCI: Boolean = sys.env.get("CI").contains("true") || sys.env.contains("GITHUB_ACTIONS")

  private def isDockerAvailable: Boolean = Try(Process("docker --version").! == 0).getOrElse(false)

  private def imageExists(imageName: String): Boolean = Process(s"docker image inspect $imageName").! == 0

  private def buildImage(imageName: String, dockerfile: File)(implicit log: Logger): Unit = {
    log.info(s"Building Docker image '$imageName'. This may take a while but will be done only once...")
    val exitCode = Process(
      command = s"docker build -f ${dockerfile.absolutePath} -t $imageName ${dockerfile.getParentFile.absolutePath}"
    ).!
    if (exitCode != 0) sys.error("Docker image build failed")
  }

  private def runBindgenInDocker(
    imageName: String,
    projectId: String,
    rootDir: File,
    envVar: String
  )(implicit log: Logger): Unit = {
    log.info(s"Generating bindings for $projectId...")
    // Run Docker with current user's UID:GID to avoid permission issues on Linux
    val userId = sys.env.get("UID").orElse(Try(Process("id -u").!!.trim).toOption).getOrElse("1000")
    val groupId = sys.env.get("GID").orElse(Try(Process("id -g").!!.trim).toOption).getOrElse("1000")

    // Create a temporary directory for SBT cache to avoid permission issues
    val tempHome = rootDir / "target" / "docker-home"
    IO.createDirectory(tempHome)

    val command = s"""docker run --rm --user $userId:$groupId -v ${rootDir.absolutePath}:/project -v ${tempHome.absolutePath}:/home/sbtuser -e HOME=/home/sbtuser -w /project -e $envVar=true $imageName sbt $projectId/bindgenGenerateScalaSources"""
    val exitCode = Process(
      command = Seq("sh", "-c", command),
      cwd = rootDir
    ).!
    if (exitCode != 0) sys.error("Binding generation failed")
  }

  private def generatedFiles(managedDir: File): Seq[File] = (managedDir ** "*.scala").get

  override lazy val projectSettings = Seq(
    bindgenDockerfile := (Compile / resourceDirectory).value / "Dockerfile.bindgen",
    nativeBindings := Seq.empty,
    // Override bindgenBindings based on environment
    bindgen.plugin.BindgenPlugin.autoImport.bindgenBindings := {
      if (sys.env.contains(envVarName)) {
        // Inside Docker: use the bindings from nativeBindings
        nativeBindings.value
      } else {
        // On host: set to empty to prevent bindgen from running any tasks
        Seq.empty
      }
    },
    generateBindings := {
      implicit val log: Logger = streams.value.log
      val isInDocker = sys.env.contains(envVarName)
      log.info(s"generateBindings task running. Inside Docker: $isInDocker")

      val bindings = nativeBindings.value
      log.info(s"nativeBindings contains ${bindings.size} binding(s)")

      val headerFiles: Set[File] = bindings.map(_.headerFile).toSet
      val cacheDir = streams.value.cacheDirectory
      val projectId = thisProjectRef.value.project
      val rootDir = (ThisBuild / baseDirectory).value
      val dockerfile = bindgenDockerfile.value
      val managedDir = (Compile / sourceManaged).value

      if (headerFiles.isEmpty) {
        log.warn("No header files specified in bindgenBindings")
        Seq.empty
      } else {
        // In CI: check if bindings already exist (e.g., downloaded from CI artifacts)
        // Locally: always use Docker with proper cache based on header files
        val existingBindings = generatedFiles(managedDir)
        if (isCI && existingBindings.nonEmpty) {
          log.info(s"CI environment: Found ${existingBindings.size} existing binding files, skipping generation")
          existingBindings
        } else {
          // Local development: use Docker with cache invalidation based on header files
          val missingHeaders = headerFiles.filterNot(_.exists())
          if (missingHeaders.nonEmpty) {
            log.warn(s"Header files not found.")
            Seq.empty
          } else {
            FileFunction.cached(cacheDir / "bindgen") { _ =>
              if (!isDockerAvailable) sys.error("Docker is needed to generate native bindings but is not available")
              if (!imageExists(dockerImageName)) buildImage(dockerImageName, dockerfile)
              runBindgenInDocker(dockerImageName, projectId, rootDir, envVarName)
              generatedFiles(managedDir).toSet
            }(headerFiles).toSeq
          }
        }
      }
    },
    // Modify sourceGenerators based on environment
    Compile / sourceGenerators := {
      if (sys.env.contains(envVarName)) {
        // Inside Docker: keep bindgen's generators as-is
        (Compile / sourceGenerators).value
      } else {
        // On host: use only our custom Docker-based generator
        Seq(generateBindings.taskValue)
      }
    }
  )
}