package scala.meta.internal.fastpass.pantsbuild

import java.nio.file.Path
import java.nio.file.Files
import scala.meta.io.AbsolutePath

case class PantsTarget(
    name: String,
    id: String,
    dependencies: collection.Seq[String],
    excludes: collection.Set[String],
    platform: Option[String],
    libraries: collection.Seq[String],
    isPantsTargetRoot: Boolean,
    targetType: TargetType,
    pantsTargetType: PantsTargetType,
    globs: PantsGlobs,
    roots: PantsRoots,
    scalacOptions: List[String],
    javacOptions: List[String],
    extraJvmOptions: List[String],
    directoryName: String,
    classesDir: Path,
    strictDeps: Option[Boolean],
    exports: List[String]
) {
  def isGeneratedTarget: Boolean = name.startsWith(".pants.d")
  private val prefixedId = id.stripPrefix(".")
  def dependencyName: String =
    if (isGeneratedTarget) prefixedId
    else name

  def isTargetRoot: Boolean =
    isPantsTargetRoot &&
      pantsTargetType.isSupported
  def baseDirectory(workspace: Path): Path =
    PantsConfiguration
      .baseDirectory(AbsolutePath(workspace), name)
      .toNIO
}
