package scala.meta.internal.fastpass.pantsbuild.commands

import metaconfig.generic
import metaconfig.annotation._
import metaconfig.ConfCodec
import java.nio.file.Path

case class ExportOptions(
    @Description(
      "How to download and index dependency sources. " +
        "Use '--sources=off' to disable all downloading of dependency sources. " +
        "Use '--sources=on' to eagerly download dependency sources and eagerly index the sources in the IDE. " +
        "Use '--sources=on-demand' to eagerly download dependency sources and lazily index the sources in the IDE."
    )
    sources: SourcesMode = SourcesMode.Default,
    @Description(
      "The path to the coursier binary." +
        "If unspecified, coursier will be downloaded automatically."
    )
    coursierBinary: Option[Path] = None,
    @Hidden()
    @Description("When enabled, Fastpass will not exit the Bloop server.")
    noBloopExit: Boolean = false,
    @Hidden()
    mergeTargetsInSameDirectory: Boolean = false
) {
  def canBloopExit: Boolean = !noBloopExit
}

object ExportOptions {
  val default: ExportOptions = ExportOptions()
  implicit lazy val surface: generic.Surface[ExportOptions] =
    generic.deriveSurface[ExportOptions]
  implicit lazy val codec: ConfCodec[ExportOptions] =
    generic.deriveCodec[ExportOptions](default)
}
