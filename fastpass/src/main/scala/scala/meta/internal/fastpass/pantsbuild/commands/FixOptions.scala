package scala.meta.internal.fastpass.pantsbuild.commands

import metaconfig.generic
import metaconfig.annotation._
import metaconfig.ConfCodec

case class FixOptions(
    @Inline dependencies: FixDependenciesOptions =
      FixDependenciesOptions.default,
    @Inline common: SharedOptions = SharedOptions()
)
object FixOptions {
  val default: FixOptions = FixOptions()
  implicit lazy val surface: generic.Surface[FixOptions] =
    generic.deriveSurface[FixOptions]
  implicit lazy val codec: ConfCodec[FixOptions] =
    generic.deriveCodec[FixOptions](default)
}
