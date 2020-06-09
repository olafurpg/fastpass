package scala.meta.internal.fastpass.pantsbuild

final case class TargetDependencyContext(
    target: PantsTarget,
    isRoot: Boolean
)
