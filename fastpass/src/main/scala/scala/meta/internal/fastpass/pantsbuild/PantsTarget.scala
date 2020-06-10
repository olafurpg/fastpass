package scala.meta.internal.fastpass.pantsbuild

import java.nio.file.Path
import java.nio.file.Files
import scala.meta.io.AbsolutePath

// NOTE(olafur): intentionally not a case class to use reference equality.
class PantsTarget(
    val name: String,
    val id: String,
    val dependencies: collection.Seq[String],
    val javaSources: collection.Seq[String],
    val excludes: collection.Set[String],
    val platform: Option[String],
    val libraries: collection.Seq[String],
    val isPantsTargetRoot: Boolean,
    val targetType: TargetType,
    val pantsTargetType: PantsTargetType,
    val globs: PantsGlobs,
    val roots: PantsRoots,
    val scalacOptions: List[String],
    val javacOptions: List[String],
    val extraJvmOptions: List[String],
    val directoryName: String,
    val classesDir: Path,
    val strictDeps: Boolean,
    val exports: List[String],
    val scope: PantsScope
) {
  require(!classesDir.getFileName().toString().endsWith(".json"), classesDir)
  def isGeneratedTarget: Boolean = name.startsWith(".pants.d")
  private val prefixedId = id.stripPrefix(".")
  def dependencyName: String =
    if (isGeneratedTarget) prefixedId
    else name

  def isTargetRoot: Boolean =
    isPantsTargetRoot &&
      pantsTargetType.isSupported
  // TODO(olafur): turn into val
  def baseDirectory(workspace: Path): Path =
    PantsConfiguration
      .baseDirectory(AbsolutePath(workspace), name)
      .toNIO
}
