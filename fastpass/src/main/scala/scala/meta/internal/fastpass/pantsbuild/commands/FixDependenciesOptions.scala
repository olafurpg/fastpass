package scala.meta.internal.fastpass.pantsbuild.commands

import metaconfig.generic
import metaconfig.annotation._
import metaconfig.ConfCodec

case class FixDependenciesOptions(
    @Description("The name of the project to print out information about.")
    @ExtraName("remainingArgs")
    @Hidden()
    projects: List[String] = Nil,
    @Inline common: SharedOptions = SharedOptions()
)
object FixDependenciesOptions {
  val default: FixDependenciesOptions = FixDependenciesOptions()
  implicit lazy val surface: generic.Surface[FixDependenciesOptions] =
    generic.deriveSurface[FixDependenciesOptions]
  implicit lazy val codec: ConfCodec[FixDependenciesOptions] =
    generic.deriveCodec[FixDependenciesOptions](default)
}
